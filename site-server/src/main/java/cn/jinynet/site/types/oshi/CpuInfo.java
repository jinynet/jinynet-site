package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CPU信息
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CpuInfo {
    
    /**
     * 逻辑核心数
     */
    private Integer cores;
    
    /**
     * 物理核心数
     */
    private Integer physicalCores;
    
    /**
     * CPU使用率（百分比）
     */
    private BigDecimal usage;
    
    /**
     * CPU名称
     */
    private String name;
    
    /**
     * 供应商
     */
    private String vendor;
}
