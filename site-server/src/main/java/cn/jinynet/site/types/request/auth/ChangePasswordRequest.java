package cn.jinynet.site.types.request.auth;

import cn.jinynet.site.types.request.captcha.CaptchaValidRequest;
import cn.jinynet.site.types.request.captcha.CaptchaValidType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改密码请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChangePasswordRequest extends CaptchaValidRequest {

    @NotEmpty(message = "管理员用户名不能为空")
    private String username;

    @NotEmpty(message = "旧密码不能为空")
    private String oldPassword;

    @NotEmpty(message = "新密码不能为空")
    private String newPassword;

    @JsonIgnore
    @Override
    public CaptchaValidType captchaValidType() {
        return CaptchaValidType.SLIDING;
    }
}