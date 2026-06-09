package cn.jinynet.site.types.request.auth;

import cn.jinynet.site.types.request.captcha.CaptchaValidRequest;
import cn.jinynet.site.types.request.captcha.CaptchaValidType;
import cn.jinynet.starter.rbac.types.LoginRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 账号密码登录请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PwdLoginRequest extends CaptchaValidRequest implements LoginRequest {

    @NotEmpty(message = "登录账号不能为空")
    private String account;

    @NotEmpty(message = "登录密码不能为空")
    private String password;

    @JsonIgnore
    @Override
    public String loginType() {
        return LoginType.PASSWORD.name();
    }

    @JsonIgnore
    @Override
    public CaptchaValidType captchaValidType() {
        return CaptchaValidType.SLIDING;
    }
}
