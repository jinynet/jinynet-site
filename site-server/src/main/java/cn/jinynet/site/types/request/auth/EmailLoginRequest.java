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
 * 邮箱认证登录请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginRequest extends CaptchaValidRequest implements LoginRequest {
    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @JsonIgnore
    @Override
    public String loginType() {
        return LoginType.EMAIL.name();
    }

    @JsonIgnore
    @Override
    public CaptchaValidType captchaValidType() {
        return CaptchaValidType.EMAIL;
    }
}
