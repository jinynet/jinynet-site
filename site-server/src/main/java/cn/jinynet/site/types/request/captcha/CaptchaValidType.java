package cn.jinynet.site.types.request.captcha;

import cn.jinynet.starter.common.helper.SpringHelper;
import cn.jinynet.site.handler.SliderCaptchaHandler;
import cn.jinynet.starter.captcha.core.AbstractImgCaptchaService;
import cn.jinynet.starter.captcha.core.EmailCaptchaService;

/**
 * 验证码验证类型
 */
public enum CaptchaValidType {
    /**
     * 文本验证码
     */
    IMAGE {
        @Override
        public void valid(CaptchaValidRequest request) {
            SpringHelper.getBean(AbstractImgCaptchaService.class).validate(request.getCaptchaToken(), request.getCaptcha());
        }
    },

    /**
     * 滑块验证码
     */
    SLIDING {
        @Override
        public void valid(CaptchaValidRequest request) {
            String privateKey = SpringHelper.getBean(SliderCaptchaHandler.class).validateVerifyToken(request.getCaptchaToken());
            // 验证成功，将私钥设置到请求中，以便后续解密密码
            request.setCaptchaToken(privateKey);
        }
    },

    /**
     * 邮箱验证码
     */
    EMAIL {
        @Override
        public void valid(CaptchaValidRequest request) {
            SpringHelper.getBean(EmailCaptchaService.class).validate(request.getCaptchaToken(), request.getCaptcha());
        }
    },
    /**
     * 手机验证码
     */
    PHONE {
        @Override
        public void valid(CaptchaValidRequest request) {
            // TODO 待实现
        }
    }
    ;

    /**
     * 验证
     *
     * @param request 验证码验证请求
     */
    public abstract void valid(CaptchaValidRequest request);
}