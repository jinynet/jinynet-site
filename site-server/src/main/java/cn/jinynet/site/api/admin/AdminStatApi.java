package cn.jinynet.site.api.admin;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.types.oshi.SystemMonitorData;
import cn.jinynet.site.service.StatService;
import cn.jinynet.site.service.SystemMonitorService;
import cn.jinynet.site.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminStatApi {
    private final StatService statService;
    private final SystemMonitorService systemMonitorService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(statService.getDashboardStats());
    }

    /**
     * 获取最新文章
     */
    @GetMapping("/latest-articles")
    public Result<List<Article>> getLatestArticles() {
        return Result.success(statService.getLatestArticles(5));
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot-articles")
    public Result<List<Article>> getHotArticles() {
        return Result.success(statService.getHotArticles(5));
    }

    /**
     * 获取系统监控数据
     */
    @GetMapping("/system-monitor")
    public Result<SystemMonitorData> getSystemMonitor() {
        return Result.success(systemMonitorService.getSystemMonitorData());
    }
}
