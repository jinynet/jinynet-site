package cn.jinynet.site.handler;

import cn.jinynet.site.service.SettingsService;
import cn.jinynet.starter.captcha.cache.CaptchaCache;
import cn.jinynet.starter.captcha.cache.EmailCaptchaLimitCache;
import cn.jinynet.starter.captcha.core.EmailCaptchaService;
import cn.jinynet.starter.captcha.types.captcha.EmailCaptcha;
import cn.jinynet.starter.mail.core.EmailSendService;
import cn.jinynet.starter.captcha.types.properties.EmailCaptchaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件验证码处理器
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Component
public class EmailCaptchaHandler extends EmailCaptchaService {

    private final SettingsService settingsService;
    private final EmailSendService emailSendService;

    @Value("${jinynet.site.url:http://localhost:8080}")
    private String siteUrl;
    @Value("${jinynet.site.logo-text:JinyNet}")
    private String siteLogoText;

    public EmailCaptchaHandler(EmailCaptchaProperties properties,
                               CaptchaCache captchaCache,
                               EmailCaptchaLimitCache emailCaptchaLimitCache,
                               SettingsService settingsService,
                               EmailSendService emailSendService) {
        super(properties, captchaCache, emailCaptchaLimitCache);
        this.settingsService = settingsService;
        this.emailSendService = emailSendService;
    }

    @Override
    public void validate(String id, String code) {
        if (settingsService.isEnabledCaptcha()) {
            super.validate(id, code);
        }
    }

    /**
     * 生成并发送邮件验证码
     *
     * @param email 收件邮箱
     * @return 验证码ID
     */
    public String sendCaptcha(String email, String subject) {
        // 检查发送间隔
        checkInterval(email);

        // 生成验证码
        EmailCaptcha emailCaptcha = create();

        // 构建模板参数
        Map<String, Object> params = new HashMap<>();
        params.put("code", emailCaptcha.getCode());
        params.put("minutes", getProperties().getTimeout() / 60);
        params.put("siteUrl", siteUrl);
        params.put("logoText", siteLogoText);

        // 发送邮件
        emailSendService.sendHtmlTemplate(
                email,
                subject,
                "captcha.ftl",
                params
        );

        log.info("邮件验证码发送成功，邮箱: {}, 验证码ID: {}", email, emailCaptcha.getId());

        return emailCaptcha.getId();
    }
}
