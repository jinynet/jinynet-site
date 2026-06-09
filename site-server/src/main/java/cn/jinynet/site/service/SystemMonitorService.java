package cn.jinynet.site.service;

import cn.hutool.core.util.NumberUtil;
import cn.jinynet.site.types.oshi.*;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统监控服务
 * 使用oshi收集系统监控指标
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
public class SystemMonitorService {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    private static final long[] PREV_CPU_TICKS = new long[4];
    private static long PREV_CPU_TIME = System.currentTimeMillis();

    /**
     * 获取完整系统监控数据
     */
    public SystemMonitorData getSystemMonitorData() {
        return SystemMonitorData.builder()
                .cpu(getCpuInfo())
                .memory(getMemoryInfo())
                .disk(getDiskInfo())
                .network(getNetworkInfo())
                .system(getSystemInfo())
                .build();
    }

    /**
     * 获取CPU信息
     */
    private CpuInfo getCpuInfo() {
        CentralProcessor processor = HARDWARE.getProcessor();
        double cpuUsage = calculateCpuUsage(processor);

        return CpuInfo.builder()
                .cores(processor.getLogicalProcessorCount())
                .physicalCores(processor.getPhysicalProcessorCount())
                .usage(NumberUtil.round(cpuUsage * 100, 2))
                .name(processor.getProcessorIdentifier().getName().trim())
                .vendor(processor.getProcessorIdentifier().getVendor())
                .build();
    }

    /**
     * 计算CPU使用率
     */
    private double calculateCpuUsage(CentralProcessor processor) {
        long[] ticks = processor.getSystemCpuLoadTicks();
        long now = System.currentTimeMillis();
        long elapsed = now - PREV_CPU_TIME;

        long user = ticks[0] - PREV_CPU_TICKS[0];
        long nice = ticks[1] - PREV_CPU_TICKS[1];
        long system = ticks[2] - PREV_CPU_TICKS[2];
        long idle = ticks[3] - PREV_CPU_TICKS[3];

        double total = user + nice + system + idle;
        double usage = total > 0 ? (user + nice + system) / total : 0;

        PREV_CPU_TICKS[0] = ticks[0];
        PREV_CPU_TICKS[1] = ticks[1];
        PREV_CPU_TICKS[2] = ticks[2];
        PREV_CPU_TICKS[3] = ticks[3];
        PREV_CPU_TIME = now;

        return usage;
    }

    /**
     * 获取内存信息
     */
    private MemoryInfo getMemoryInfo() {
        GlobalMemory memory = HARDWARE.getMemory();

        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        return MemoryInfo.builder()
                .total(formatBytes(total))
                .totalBytes(total)
                .used(formatBytes(used))
                .usedBytes(used)
                .available(formatBytes(available))
                .availableBytes(available)
                .usagePercent(NumberUtil.round((double) used / total * 100, 2))
                .build();
    }

    /**
     * 获取磁盘信息
     */
    private DiskInfo getDiskInfo() {
        List<DiskInfo.DiskStore> diskStores = new ArrayList<>();
        for (HWDiskStore disk : HARDWARE.getDiskStores()) {
            diskStores.add(DiskInfo.DiskStore.builder()
                    .name(disk.getName())
                    .model(disk.getModel())
                    .size(formatBytes(disk.getSize()))
                    .sizeBytes(disk.getSize())
                    .build());
        }

        List<DiskInfo.FileSystem> fileSystems = new ArrayList<>();
        long totalBytes = 0;
        long usedBytes = 0;

        for (var fs : OS.getFileSystem().getFileStores()) {
            long fsTotal = fs.getTotalSpace();
            long fsUsed = fsTotal - fs.getUsableSpace();
            totalBytes += fsTotal;
            usedBytes += fsUsed;

            fileSystems.add(DiskInfo.FileSystem.builder()
                    .name(fs.getName())
                    .mount(fs.getMount())
                    .type(fs.getType())
                    .total(formatBytes(fsTotal))
                    .totalBytes(fsTotal)
                    .used(formatBytes(fsUsed))
                    .usedBytes(fsUsed)
                    .free(formatBytes(fs.getUsableSpace()))
                    .freeBytes(fs.getUsableSpace())
                    .usagePercent(fsTotal > 0 ? NumberUtil.round((double) fsUsed / fsTotal * 100, 2) : BigDecimal.ZERO)
                    .build());
        }

        return DiskInfo.builder()
                .disks(diskStores)
                .fileSystems(fileSystems)
                .total(formatBytes(totalBytes))
                .totalBytes(totalBytes)
                .used(formatBytes(usedBytes))
                .usedBytes(usedBytes)
                .usagePercent(totalBytes > 0 ? NumberUtil.round((double) usedBytes / totalBytes * 100, 2) : BigDecimal.ZERO)
                .build();
    }

    /**
     * 获取网络信息
     */
    private NetworkInfo getNetworkInfo() {
        List<NetworkInfo.NetworkInterface> interfaces = new ArrayList<>();
        long totalSent = 0;
        long totalReceived = 0;

        for (NetworkIF net : HARDWARE.getNetworkIFs()) {
            totalSent += net.getBytesSent();
            totalReceived += net.getBytesRecv();

            interfaces.add(NetworkInfo.NetworkInterface.builder()
                    .name(net.getName())
                    .displayName(net.getDisplayName())
                    .mac(net.getMacaddr())
                    .ipv4(Arrays.asList(net.getIPv4addr()))
                    .ipv6(Arrays.asList(net.getIPv6addr()))
                    .bytesSent(formatBytes(net.getBytesSent()))
                    .bytesSentBytes(net.getBytesSent())
                    .bytesReceived(formatBytes(net.getBytesRecv()))
                    .bytesReceivedBytes(net.getBytesRecv())
                    .speed(formatBytes(net.getSpeed()) + "/s")
                    .speedBytes(net.getSpeed())
                    .build());
        }

        return NetworkInfo.builder()
                .interfaces(interfaces)
                .totalSent(formatBytes(totalSent))
                .totalSentBytes(totalSent)
                .totalReceived(formatBytes(totalReceived))
                .totalReceivedBytes(totalReceived)
                .build();
    }

    /**
     * 获取系统信息
     */
    private cn.jinynet.site.types.oshi.SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long jvmTotal = runtime.totalMemory();
        long jvmFree = runtime.freeMemory();
        long jvmUsed = jvmTotal - jvmFree;

        return cn.jinynet.site.types.oshi.SystemInfo.builder()
                .os(OS.getFamily() + " " + OS.getVersionInfo().getVersion())
                .hostname(OS.getNetworkParams().getHostName())
                .uptime(formatUptime(OS.getSystemUptime()))
                .uptimeSeconds(OS.getSystemUptime())
                .processCount(OS.getProcessCount())
                .threadCount(OS.getThreadCount())
                .jvmVersion(System.getProperty("java.version"))
                .jvmVendor(System.getProperty("java.vendor"))
                .jvmTotalMemory(formatBytes(jvmTotal))
                .jvmTotalMemoryBytes(jvmTotal)
                .jvmMaxMemory(formatBytes(runtime.maxMemory()))
                .jvmMaxMemoryBytes(runtime.maxMemory())
                .jvmFreeMemory(formatBytes(jvmFree))
                .jvmFreeMemoryBytes(jvmFree)
                .jvmUsedMemory(formatBytes(jvmUsed))
                .jvmUsedMemoryBytes(jvmUsed)
                .build();
    }

    /**
     * 格式化字节
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return NumberUtil.round((double) bytes / 1024, 2) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return NumberUtil.round((double) bytes / (1024 * 1024), 2) + " MB";
        } else {
            return NumberUtil.round((double) bytes / (1024 * 1024 * 1024), 2) + " GB";
        }
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long seconds) {
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
        long secs = seconds - TimeUnit.DAYS.toSeconds(days) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        sb.append(secs).append("秒");
        return sb.toString().trim();
    }
}
