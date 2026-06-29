package cn.jinynet.site.config;

import cn.jinynet.starter.web.types.properties.CorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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

    private final CorsProperties corsProperties;

    public WebMvcConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

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
        if (!corsProperties.isEnable()) {
            return;
        }

        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }

        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
                .allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}
