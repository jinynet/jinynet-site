package cn.jinynet.site.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部访问拦截器
 * <p>
 * 仅允许来自 localhost、私有网络（Docker 内部网桥等）的请求通过。
 * 用于保护运维类接口（如 /api/health），避免暴露给公网。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
public class InternalAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String clientIp = getClientIp(request);

        if (isInternalAddress(clientIp)) {
            return true;
        }

        log.warn("拒绝外部访问 {} 来自 IP: {}", request.getRequestURI(), clientIp);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"仅限内部访问\"}");
        return false;
    }

    /**
     * 获取客户端真实 IP
     * <p>优先从反向代理头部获取，其次从连接远端地址获取。</p>
     */
    private String getClientIp(HttpServletRequest request) {
        // Nginx X-Forwarded-For 头部（取第一个，即原始客户端 IP）
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        // Nginx X-Real-IP 头部
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断是否为内部地址
     */
    static boolean isInternalAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        // --- IPv6 localhost ---
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }

        // --- IPv4 解析 ---
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) return false;
            int b1 = Integer.parseInt(parts[0]) & 0xFF;
            int b2 = Integer.parseInt(parts[1]) & 0xFF;

            // 127.0.0.0/8 — localhost
            if (b1 == 127) return true;

            // 10.0.0.0/8 — 私有网络
            if (b1 == 10) return true;

            // 172.16.0.0/12 — 私有网络（Docker 默认网桥在此范围）
            if (b1 == 172 && b2 >= 16 && b2 <= 31) return true;

            // 192.168.0.0/16 — 私有网络
            return b1 == 192 && b2 == 168;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
