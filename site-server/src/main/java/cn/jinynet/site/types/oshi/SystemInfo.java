package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统信息
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfo {
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * 运行时间（格式化字符串）
     */
    private String uptime;
    
    /**
     * 运行时间（秒）
     */
    private Long uptimeSeconds;
    
    /**
     * 进程数
     */
    private Integer processCount;
    
    /**
     * 线程数
     */
    private Integer threadCount;
    
    /**
     * JVM版本
     */
    private String jvmVersion;
    
    /**
     * JVM供应商
     */
    private String jvmVendor;
    
    /**
     * JVM总内存（格式化字符串）
     */
    private String jvmTotalMemory;
    
    /**
     * JVM总内存（字节）
     */
    private Long jvmTotalMemoryBytes;
    
    /**
     * JVM最大内存（格式化字符串）
     */
    private String jvmMaxMemory;
    
    /**
     * JVM最大内存（字节）
     */
    private Long jvmMaxMemoryBytes;
    
    /**
     * JVM空闲内存（格式化字符串）
     */
    private String jvmFreeMemory;
    
    /**
     * JVM空闲内存（字节）
     */
    private Long jvmFreeMemoryBytes;
    
    /**
     * JVM已使用内存（格式化字符串）
     */
    private String jvmUsedMemory;
    
    /**
     * JVM已使用内存（字节）
     */
    private Long jvmUsedMemoryBytes;
}
