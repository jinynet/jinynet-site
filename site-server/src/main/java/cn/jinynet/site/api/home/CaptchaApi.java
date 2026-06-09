package cn.jinynet.site.api.home;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.handler.SliderCaptchaHandler;
import cn.jinynet.starter.captcha.types.captcha.SliderCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 滑动验证码接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CaptchaApi {
    private final SliderCaptchaHandler sliderCaptchaHandler;

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

}