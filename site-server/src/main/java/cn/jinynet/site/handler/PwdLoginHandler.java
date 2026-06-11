package cn.jinynet.site.handler;


import cn.jinynet.site.cache.PasswdErrLimitCache;
import cn.jinynet.site.service.SettingsService;
import cn.jinynet.site.types.AdminUser;
import cn.jinynet.site.types.request.auth.LoginType;
import cn.jinynet.site.types.request.auth.PwdLoginRequest;
import cn.jinynet.site.types.request.captcha.CaptchaValidRequest;
import cn.jinynet.starter.captcha.types.exception.CaptchaBizCode;
import cn.jinynet.starter.captcha.types.exception.CaptchaException;
import cn.jinynet.starter.crypto.utils.BCryptUtils;
import cn.jinynet.starter.crypto.utils.Sm2Utils;
import cn.jinynet.starter.rbac.handler.LoginHandler;
import cn.jinynet.starter.rbac.types.SysUser;
import cn.jinynet.starter.rbac.types.TokenUser;
import cn.jinynet.starter.rbac.types.exception.AuthBizCode;
import cn.jinynet.starter.rbac.types.exception.AuthBizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 密码认证登录处理器
 */
@Component
@RequiredArgsConstructor
public class PwdLoginHandler extends LoginHandler<PwdLoginRequest> {

    private final PasswdErrLimitCache passwdErrLimitCache;
    private final SettingsService settingsService;

    /**
     * 时间限制提示
     *
     * @param leftTime 剩余时间
     * @return 时间限制提示
     */
    private String timeLimit(long leftTime) {
        if (leftTime > 1000 * 60) {
            return leftTime / 1000 / 60 + "分钟";
        }
        return leftTime / 1000 + "秒";
    }

    @Override
    public String loginTypeKey() {
        return LoginType.PASSWORD.name();
    }

    @Override
    protected void beforeLogin(PwdLoginRequest loginRequest) {
        validCode(loginRequest);
    }

    @Override
    protected AdminUser getMatchedUser(PwdLoginRequest loginRequest) {
        int maxLoginAttempts = 3;
        String loginAttempts = settingsService.getSettingValue("login_attempts");
        if (loginAttempts != null) {
            maxLoginAttempts = Integer.parseInt(loginAttempts);
        }
        String tip;
        // 密码错误限制验证
        long leftLimit = passwdErrLimitCache.limitLeftTime(loginRequest.getAccount(), maxLoginAttempts);
        if (leftLimit > 0) {
            tip = AuthBizCode.PASSWD_ERROR_LIMIT.getMsg() + "（" + timeLimit(leftLimit) + "）";
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR_LIMIT, tip);
        }

        // 获取匹配账号信息
        AdminUser adminUser = settingsService.getAdminUser(loginRequest.getAccount());
        AuthBizException.throwIf(adminUser == null, AuthBizCode.USER_NOT_FOUND);

        // 解密密码
        String decryptedPassword = decryptPassword(loginRequest);

        // 密码校验
        if (BCryptUtils.isNotMatch(decryptedPassword, adminUser.password())) {
            int errCount = passwdErrLimitCache.errCount(adminUser.username(), maxLoginAttempts);
            if (errCount < maxLoginAttempts) {
                tip = AuthBizCode.PASSWD_ERROR.getMsg() + "，你还剩" + (maxLoginAttempts - errCount) + "次机会";
                throw new AuthBizException(AuthBizCode.PASSWD_ERROR, tip);
            }
            tip = AuthBizCode.PASSWD_ERROR_LIMIT.getMsg() + "（" + timeLimit(PasswdErrLimitCache.LIMIT_EXPIRE) + "）";
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR_LIMIT, tip);
        }
        passwdErrLimitCache.remove(adminUser.username());
        return adminUser;
    }

    @Override
    protected void validAccountStatus(SysUser sysUser) throws AuthBizException {
        super.validAccountStatus(sysUser);
    }

    @Override
    protected void afterLogin(SysUser sysUser, TokenUser<?> tokenUser) {
        //  todo maybe save log
    }

    /**
     * 验证码校验
     *
     * @param loginRequest 登录请求
     */
    private void validCode(PwdLoginRequest loginRequest) {
        boolean captchaEnabled = settingsService.isEnabledCaptcha();
        if (captchaEnabled && loginRequest instanceof CaptchaValidRequest captchaValidRequest) {
            captchaValidRequest.captchaValidType().valid(captchaValidRequest);
        }
    }

    /**
     * 解密密码
     *
     * @param loginRequest 登录请求
     * @return 解密后的密码
     */
    private String decryptPassword(PwdLoginRequest loginRequest) {
        boolean captchaEnabled = settingsService.isEnabledCaptcha();
        if (!captchaEnabled) {
            // 验证码未开启，直接返回原始密码
            return loginRequest.getPassword();
        }

        // 验证码已开启，需要解密
        if (loginRequest.getCaptchaToken() == null || loginRequest.getCaptchaToken().isEmpty()) {
            throw new CaptchaException(CaptchaBizCode.EXPIRED);
        }

        // 获取私钥
        String privateKey = loginRequest.getCaptchaToken();

        // 使用私钥解密密码
        try {
            return Sm2Utils.decryptByPrivateKey(privateKey, loginRequest.getPassword());
        } catch (Exception e) {
            throw new AuthBizException(AuthBizCode.PASSWD_ERROR, "密码解密失败");
        }
    }
}
