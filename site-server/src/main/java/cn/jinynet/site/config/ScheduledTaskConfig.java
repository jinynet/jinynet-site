package cn.jinynet.site.config;

import cn.jinynet.site.service.LargeFileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 *
 * @author jinty
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final LargeFileUploadService largeFileUploadService;

    /**
     * 每天凌晨 3 点清理 7 天前未完成的上传任务及关联分片
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanStaleUploadTasks() {
        log.info("开始清理过期上传任务...");
        int cleaned = largeFileUploadService.cleanStaleTasks(7);
        log.info("清理完成: {} 个过期任务", cleaned);
    }
}
