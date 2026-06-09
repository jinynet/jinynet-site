package cn.jinynet.site.types.request.captcha;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


/**
 * 验证码校验请求
 *
 * @author jinty
 */
@Data
@Schema
public abstract class CaptchaValidRequest {
    /**
     * 验证码ID
     */
    @NotEmpty(message = "验证码ID不能为空")
    public String captchaToken;

    /**
     * 验证码
     */
//    @NotEmpty(message = "验证码不能为空")
    public String captcha;

    /**
     * 验证码验证方式
     *
     * @return 验证码类型
     */
    @JsonIgnore
    public abstract CaptchaValidType captchaValidType();

}

