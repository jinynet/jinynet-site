package cn.jinynet.site.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 配置静态资源映射、跨域、拦截器等
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 仅限内部访问（localhost、Docker 内网等）
        registry.addInterceptor(new InternalAccessInterceptor())
                .addPathPatterns(
                        "/api/health",
                        "/openapi.html",
                        "/openapi.yml",
                        "/static/**",
                        "/webjars/scalar/**",
                        "/favicon.ico",
                        "/favicon.svg"

                );
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射静态资源目录（包括 favicon、CSS、JS 等）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/webjars/scalar/")
                .setCachePeriod(3600);

        // 映射文件上传路径
        registry.addResourceHandler("/api/files/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
