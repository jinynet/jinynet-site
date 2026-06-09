package cn.jinynet.site.types.request.captcha;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证码获取请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCaptchaGetRequest extends CaptchaValidRequest {
    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @Override
    public CaptchaValidType captchaValidType() {
        return CaptchaValidType.SLIDING;
    }
}
