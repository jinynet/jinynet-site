package cn.jinynet.site.types.oshi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 磁盘信息
 *
 * @author jinty
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiskInfo {
    
    /**
     * 磁盘列表
     */
    private List<DiskStore> disks;
    
    /**
     * 文件系统列表
     */
    private List<FileSystem> fileSystems;
    
    /**
     * 总容量（格式化字符串）
     */
    private String total;
    
    /**
     * 总容量（字节）
     */
    private Long totalBytes;
    
    /**
     * 已使用（格式化字符串）
     */
    private String used;
    
    /**
     * 已使用（字节）
     */
    private Long usedBytes;
    
    /**
     * 使用率（百分比）
     */
    private BigDecimal usagePercent;
    
    /**
     * 磁盘存储信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskStore {
        private String name;
        private String model;
        private String size;
        private Long sizeBytes;
    }
    
    /**
     * 文件系统信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileSystem {
        private String name;
        private String mount;
        private String type;
        private String total;
        private Long totalBytes;
        private String used;
        private Long usedBytes;
        private String free;
        private Long freeBytes;
        private BigDecimal usagePercent;
    }
}
