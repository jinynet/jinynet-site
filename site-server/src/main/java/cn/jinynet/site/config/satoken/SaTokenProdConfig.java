package cn.jinynet.site.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class SaTokenProdConfig implements WebMvcConfigurer {
    // 注册 Sa-Token 的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    StpUtil.checkLogin();
                }))
                // 需要登录的路径（使用 ** 匹配所有子路径）
                .addPathPatterns("/admin/**")
                .addPathPatterns("/auth/profile")
                .addPathPatterns("/auth/change-password")
                .addPathPatterns("/files")
//                // 排除公开路径
//                .excludePathPatterns("/openapi.html")
//                .excludePathPatterns("/static/**")
//                .excludePathPatterns("/openapi.yml")
//                .excludePathPatterns("/favicon.ico")
//                .excludePathPatterns("/api/auth/login")
//                .excludePathPatterns("/api/auth/captcha-config")
//                .excludePathPatterns("/api/captcha/**")
        ;
    }
}
