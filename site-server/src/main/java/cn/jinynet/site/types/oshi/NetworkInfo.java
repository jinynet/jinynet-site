package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网络信息
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkInfo {
    
    /**
     * 网络接口列表
     */
    private List<NetworkInterface> interfaces;
    
    /**
     * 总上传量（格式化字符串）
     */
    private String totalSent;
    
    /**
     * 总上传量（字节）
     */
    private Long totalSentBytes;
    
    /**
     * 总下载量（格式化字符串）
     */
    private String totalReceived;
    
    /**
     * 总下载量（字节）
     */
    private Long totalReceivedBytes;
    
    /**
     * 网络接口信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkInterface {
        private String name;
        private String displayName;
        private String mac;
        private List<String> ipv4;
        private List<String> ipv6;
        private String bytesSent;
        private Long bytesSentBytes;
        private String bytesReceived;
        private Long bytesReceivedBytes;
        private String speed;
        private Long speedBytes;
    }
}
