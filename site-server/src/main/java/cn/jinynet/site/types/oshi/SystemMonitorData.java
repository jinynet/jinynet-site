package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统监控数据
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMonitorData {
    
    /**
     * CPU信息
     */
    private CpuInfo cpu;
    
    /**
     * 内存信息
     */
    private MemoryInfo memory;
    
    /**
     * 磁盘信息
     */
    private DiskInfo disk;
    
    /**
     * 网络信息
     */
    private NetworkInfo network;
    
    /**
     * 系统信息
     */
    private SystemInfo system;
}
