package cn.jinynet.site.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.jinynet.site.handler.SliderCaptchaHandler;
import cn.jinynet.site.types.AdminUser;
import cn.jinynet.site.types.request.auth.ChangePasswordRequest;
import cn.jinynet.site.types.request.auth.LoginType;

import cn.jinynet.starter.crypto.utils.BCryptUtils;
import cn.jinynet.starter.crypto.utils.Sm2Utils;
import cn.jinynet.starter.rbac.handler.LoginHandler;
import cn.jinynet.starter.rbac.types.LoginRequest;
import cn.jinynet.starter.rbac.types.TokenUser;
import cn.jinynet.starter.rbac.types.exception.AuthBizCode;
import cn.jinynet.starter.rbac.types.exception.AuthBizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
public class AuthService {
    /**
     * 登录处理器集合
     */
    private final Map<LoginType, LoginHandler<LoginRequest>> loginHandlers = new HashMap<>();

    private final SettingsService settingsService;

    private final SliderCaptchaHandler sliderCaptchaHandler;

    /**
     * 构造函数
     *
     * @param context        spring上下文
     * @param settingsService 设置服务
     * @param sliderCaptchaHandler 滑动验证码处理器
     */
    public AuthService(ApplicationContext context, SettingsService settingsService, SliderCaptchaHandler sliderCaptchaHandler) {
        this.settingsService = settingsService;
        this.sliderCaptchaHandler = sliderCaptchaHandler;
        // 获取所有登录处理器
        Map<String, LoginHandler> beansOfType = context.getBeansOfType(LoginHandler.class);
        // 添加到登录处理器集合中
        beansOfType.forEach((k, v) -> loginHandlers.put(LoginType.valueOf(v.loginTypeKey()), v));
    }

    /**
     * 登录
     *
     * @param loginRequest 登录请求
     * @return 登录用户信息
     */
    public TokenUser<AdminUser> login(LoginRequest loginRequest) {
        // 根据登录方式获取对应的登录处理器
        LoginHandler<LoginRequest> loginHandler = loginHandlers.get(LoginType.valueOf(loginRequest.loginType()));
        // 无对应处理器 登录方式不支持
        AuthBizException.throwIf(loginHandler == null, AuthBizCode.NOT_SUPPORT);
        try {
            // 登录
            return loginHandler.login(loginRequest);
        } catch (AuthBizException e) {
            log.error("用户登录失败", e);
            throw e;
        }

    }

    /**
     * 登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 修改密码
     *
     * @param changePasswordRequest 修改密码请求（包含旧密码和新密码，可选验证码token）
     */
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        // 验证验证码
        validCaptcha(changePasswordRequest);


        // 解密
        String decryptedUsername = decryptPassword(changePasswordRequest.getUsername(), changePasswordRequest.getCaptchaToken());
        String decryptedOldPassword = decryptPassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getCaptchaToken());
        String decryptedNewPassword = decryptPassword(changePasswordRequest.getNewPassword(), changePasswordRequest.getCaptchaToken());

        // 获取当前管理员用户
        TokenUser user = (TokenUser) StpUtil.getSession().get("user");
        AdminUser adminUser = settingsService.getAdminUser(user.getUser().username());
        AuthBizException.throwIf(adminUser == null || !adminUser.username().equals(decryptedUsername), AuthBizCode.USER_NOT_FOUND);

        // 验证旧密码
        if (BCryptUtils.isNotMatch(decryptedOldPassword, adminUser.password())) {
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR, "旧密码错误");
        }

        // 验证新旧密码不能相同
        if (decryptedOldPassword.equals(decryptedNewPassword)) {
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR, "新密码不能与旧密码相同");
        }

        // 更新密码
        settingsService.updateSetting("admin_password", decryptedNewPassword, "security");
        log.info("管理员密码修改成功");
    }

    /**
     * 验证验证码
     *
     * @param changePasswordRequest 修改密码请求
     */
    private void validCaptcha(ChangePasswordRequest changePasswordRequest) {
        // 验证并获取私钥
        String privateKey = sliderCaptchaHandler.validateVerifyToken(changePasswordRequest.getCaptchaToken());
        // 将私钥设置到请求中，以便后续解密
        changePasswordRequest.setCaptchaToken(privateKey);
    }

    /**
     * 解密密码
     *
     * @param encryptedPassword 加密后的密码
     * @param privateKey SM2私钥
     * @return 解密后的密码
     */
    private String decryptPassword(String encryptedPassword, String privateKey) {
        boolean captchaEnabled = settingsService.isEnabledCaptcha();
        if (!captchaEnabled || privateKey == null) {
            return encryptedPassword;
        }

        try {
            return Sm2Utils.decryptByPrivateKey(privateKey, encryptedPassword);
        } catch (Exception e) {
            log.error("密码解密失败", e);
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR, "密码解密失败");
        }
    }
}
