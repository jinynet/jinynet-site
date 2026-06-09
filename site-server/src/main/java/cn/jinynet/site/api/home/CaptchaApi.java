package cn.jinynet.site.api.home;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.handler.EmailCaptchaHandler;
import cn.jinynet.site.handler.SliderCaptchaHandler;
import cn.jinynet.site.types.request.captcha.EmailCaptchaGetRequest;
import cn.jinynet.starter.captcha.types.captcha.SliderCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class CaptchaApi {
    private final SliderCaptchaHandler sliderCaptchaHandler;
    private final EmailCaptchaHandler emailCaptchaHandler;

    /**
     * 生成滑动验证码
     *
     * @return 验证码数据（背景图和token）
     */
    @GetMapping("/captcha")
    public Result<SliderCaptcha> generateCaptcha() {
        try {
            SliderCaptcha sliderCaptcha = sliderCaptchaHandler.create();
            return Result.success(sliderCaptcha);
        } catch (Exception e) {
            log.error("生成滑动验证码失败", e);
            return Result.fail("生成验证码失败");
        }
    }

    /**
     * 验证滑动验证码
     *
     * @param token          验证码token
     * @param sliderPosition 滑块位置
     * @return 验证结果（包含一次性验证token）
     */
    @PostMapping("/captcha/verify")
    public Result<String> verifyCaptcha(
            @RequestParam String token,
            @RequestParam double sliderPosition) {
        String verifyToken = sliderCaptchaHandler.validateAndGetToken(token, String.valueOf((int) sliderPosition));
        return Result.success("验证成功", verifyToken);
    }

    /**
     * 发送邮件验证码（需先通过滑块验证）
     *
     * @param request 包含滑块验证码信息和邮箱地址
     * @return 验证码ID
     */
    @PostMapping("/captcha/email")
    public Result<String> sendEmailCaptcha(@RequestBody @Validated EmailCaptchaGetRequest request) {
        try {
            // 先进行滑块验证码校验
            request.captchaValidType().valid(request);
            // 发送邮件验证码
            String captchaId = emailCaptchaHandler.sendCaptcha(request.getEmail(), "登录验证码");
            return Result.success("验证码已发送", captchaId);
        } catch (Exception e) {
            log.error("发送邮件验证码失败，邮箱: {}", request.getEmail(), e);
            return Result.fail(e.getMessage() != null ? e.getMessage() : "验证码发送失败");
        }
    }

    /**
     * 验证邮件验证码
     *
     * @param captchaId 验证码ID
     * @param code      验证码
     * @return 验证结果
     */
    @PostMapping("/captcha/email/verify")
    public Result<Void> verifyEmailCaptcha(
            @RequestParam String captchaId,
            @RequestParam String code) {
        emailCaptchaHandler.validate(captchaId, code);
        return Result.success("验证成功");
    }

}