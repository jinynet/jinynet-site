package cn.jinynet.site;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 个人技术平台启动类
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@SpringBootApplication
public class SiteApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SiteApplication.class, args);
        log.info("个人技术平台站点启动成功");
        // 获取接口文档地址
        String uiPath = context.getEnvironment().getProperty("jimmer.client.openapi.ui-path");
        String port = context.getEnvironment().getProperty("server.port");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        log.debug("接口文档地址: http://localhost:{}{}{}", port, StringUtils.defaultIfBlank(contextPath, ""), uiPath);

    }
}
