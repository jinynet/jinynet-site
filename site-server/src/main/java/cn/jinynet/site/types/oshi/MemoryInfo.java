package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 内存信息
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryInfo {
    
    /**
     * 总内存（格式化字符串）
     */
    private String total;
    
    /**
     * 总内存（字节）
     */
    private Long totalBytes;
    
    /**
     * 已使用内存（格式化字符串）
     */
    private String used;
    
    /**
     * 已使用内存（字节）
     */
    private Long usedBytes;
    
    /**
     * 可用内存（格式化字符串）
     */
    private String available;
    
    /**
     * 可用内存（字节）
     */
    private Long availableBytes;
    
    /**
     * 使用率（百分比）
     */
    private BigDecimal usagePercent;
}
