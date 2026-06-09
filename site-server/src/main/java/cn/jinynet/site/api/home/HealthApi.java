package cn.jinynet.site.api.home;

import cn.jinynet.starter.common.types.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口
 * <p>
 * 供 Docker healthcheck、systemd 启动脚本等运维工具探测服务可用性。
 * 无需认证，直接返回 HTTP 200 + 基本运行状态信息。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
public class HealthApi {

    /**
     * 服务健康检查
     * <p>
     * Docker Compose healthcheck: wget -qO- http://localhost:8080/{context-path}/health || exit 1
     * Systemd 启动脚本:       curl -sf http://localhost:/{context-path}/health
     * </p>
     *
     * @return 包含状态、时间戳等基础运行信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("application", "site-server");
        return Result.success(info);
    }
}
