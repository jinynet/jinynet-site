package cn.jinynet.site.handler;

import cn.jinynet.site.service.SettingsService;
import cn.jinynet.starter.captcha.cache.CaptchaCache;
import cn.jinynet.starter.captcha.cache.CaptchaMetadata;
import cn.jinynet.starter.captcha.core.SimpleSliderCaptchaService;
import cn.jinynet.starter.captcha.types.exception.CaptchaBizCode;
import cn.jinynet.starter.captcha.types.exception.CaptchaException;
import cn.jinynet.starter.captcha.types.properties.SliderCaptchaProperties;
import cn.jinynet.starter.crypto.types.KeyPair;
import cn.jinynet.starter.crypto.utils.Sm2Utils;
import org.springframework.stereotype.Component;

/**
 * 滑块验证码处理器
 *
 * @author jinty
 * @since 1.0
 */
@Component
public class SliderCaptchaHandler extends SimpleSliderCaptchaService {

    private final SettingsService settingsService;
    private final CaptchaCache captchaCache;
    private final SliderCaptchaProperties properties;

    public SliderCaptchaHandler(SliderCaptchaProperties properties, CaptchaCache captchaCache, SettingsService settingsService) {
        super(properties, captchaCache);
        this.settingsService = settingsService;
        this.captchaCache = captchaCache;
        this.properties = properties;
    }

    @Override
    public void validate(String id, String code) {
        if (settingsService.isEnabledCaptcha()) {
            super.validate(id, code);
        }
    }

    /**
     * 验证滑块验证码并返回一次性验证token
     *
     * @param id   验证码ID
     * @param code 滑块位置
     * @return 一次性验证token
     */
    public String validateAndGetToken(String id, String code) {
        // 先进行滑块位置验证
        validate(id, code);
        
        // 生成一次性验证token
        KeyPair keyPair = Sm2Utils.createKeyPair();
        String verifyToken = keyPair.getPublicKey();
        
        // 将验证token缓存，用于后续登录验证（1分钟有效期）
        CaptchaMetadata metadata = CaptchaMetadata.of(id, keyPair.getPrivateKey(), 60);
        captchaCache.set(properties.getPrefix() + ":verify", verifyToken, metadata);
        
        return verifyToken;
    }

    /**
     * 验证一次性验证token
     *
     * @param verifyToken 一次性验证token
     */
    public String validateVerifyToken(String verifyToken) {
        if (!settingsService.isEnabledCaptcha()) {
            return "";
        }

        CaptchaMetadata metadata = captchaCache.get(properties.getPrefix() + ":verify", verifyToken);
        if (metadata == null || metadata.isExpired()) {
            throw new CaptchaException(CaptchaBizCode.EXPIRED);
        }
        
        // 验证通过后立即删除，确保一次性使用
        captchaCache.delete(properties.getPrefix() + ":verify", verifyToken);
        return metadata.getCode();
    }
}
