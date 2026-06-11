package cn.jinynet.site.config;

import cn.jinynet.site.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动初始化检查器
 * 检查并初始化必要的系统配置
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemInitializer implements ApplicationRunner {

    private final SettingsService settingsService;

    /**
     * 默认管理员用户名
     */
    private static final String DEFAULT_ADMIN_USERNAME = "admin";

    /**
     * 默认管理员密码
     */
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始执行系统初始化检查...");
        initAdminAccount();
        log.info("系统初始化检查完成");
    }

    /**
     * 初始化管理员账户
     * 如果 admin_username 未配置，则设置默认值
     */
    private void initAdminAccount() {
        String adminUsername = settingsService.getSettingValue("admin_username");

        if (adminUsername == null || adminUsername.isEmpty()) {
            log.warn("未检测到管理员账户配置，正在初始化默认管理员账户...");
            settingsService.updateSetting("admin_username", DEFAULT_ADMIN_USERNAME, "security");
            settingsService.updateSetting("admin_password", DEFAULT_ADMIN_PASSWORD, "security");
            settingsService.updateSetting("login_attempts", "3", "security");
            log.warn("【安全提示】请登录后立即修改默认密码！");
        }
    }
}
