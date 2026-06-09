package cn.jinynet.site.handler;

import cn.jinynet.site.service.SettingsService;
import cn.jinynet.site.types.AdminUser;
import cn.jinynet.site.types.request.auth.EmailLoginRequest;
import cn.jinynet.site.types.request.auth.LoginType;
import cn.jinynet.site.types.request.captcha.CaptchaValidRequest;
import cn.jinynet.starter.rbac.handler.LoginHandler;
import cn.jinynet.starter.rbac.types.SysUser;
import cn.jinynet.starter.rbac.types.TokenUser;
import cn.jinynet.starter.rbac.types.exception.AuthBizCode;
import cn.jinynet.starter.rbac.types.exception.AuthBizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码登录处理器
 *
 * <p>使用邮箱 + 验证码的方式进行登录，通过验证码校验邮箱所有权。
 *
 * <h3>登录流程</h3>
 * <ol>
 *   <li>调用 {@code GET /auth/captcha/email?email=xxx} 发送验证码（需先通过滑块验证）</li>
 *   <li>获取 {@code captchaId}</li>
 *   <li>提交登录请求：邮箱 + captchaId + 验证码</li>
 *   <li>处理器校验验证码 → 查找用户 → 执行登录</li>
 * </ol>
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailLoginHandler extends LoginHandler<EmailLoginRequest> {

    private final SettingsService settingsService;

    @Override
    public String loginTypeKey() {
        return LoginType.EMAIL.name();
    }

    @Override
    protected void beforeLogin(EmailLoginRequest loginRequest) {
        validCode(loginRequest);
    }

    @Override
    protected AdminUser getMatchedUser(EmailLoginRequest loginRequest) {
        // 邮箱匹配管理员账号
        String adminEmail = settingsService.getSettingValue("admin_email");
        if (adminEmail == null || !adminEmail.equalsIgnoreCase(loginRequest.getEmail())) {
            throw new AuthBizException(AuthBizCode.USER_NOT_FOUND, "邮箱未绑定账号");
        }

        // 使用 admin_username 获取管理员信息
        String adminUsername = settingsService.getSettingValue("admin_username");
        if (adminUsername == null) {
            throw new AuthBizException(AuthBizCode.USER_NOT_FOUND, "管理员账号未配置");
        }
        return AdminUser.builder()
                .id(1L)
                .username(adminUsername)
                .password(settingsService.getSettingValue("admin_password"))
                .build();
    }

    @Override
    protected void validAccountStatus(SysUser sysUser) throws AuthBizException {
        super.validAccountStatus(sysUser);
    }

    @Override
    protected void afterLogin(SysUser sysUser, TokenUser<?> tokenUser) {
        // todo save log
    }

    /**
     * 邮箱验证码校验
     *
     * @param loginRequest 登录请求
     */
    private void validCode(EmailLoginRequest loginRequest) {
        if (loginRequest instanceof CaptchaValidRequest captchaValidRequest) {
            captchaValidRequest.captchaValidType().valid(captchaValidRequest);
        }
    }
}
