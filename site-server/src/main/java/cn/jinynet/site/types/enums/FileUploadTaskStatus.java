package cn.jinynet.site.types.enums;

import org.babyfish.jimmer.sql.EnumType;

/**
 * 大文件上传任务状态枚举。
 *
 * <h3>状态流转</h3>
 * <pre>
 * INIT
 *  │
 *  ▼
 * UPLOADING ←────────┐
 *  │                  │
 *  ├──▶ PAUSED ──────┘  (resume)
 *  │
 *  ├──▶ CANCELLED       (cancel)
 *  │
 *  └──▶ COMPLETED ──▶  (uploadChunk 检测全部分片完成)
 *        │
 *        ▼
 *      MERGING ──▶ (mergeChunks 完成后仍置 COMPLETED)
 *
 * 任意状态 → FAILED     (异常兜底)
 * </pre>
 *
 * @author jinty
 * @since 2.0
 */
@EnumType(value = EnumType.Strategy.NAME)
public enum FileUploadTaskStatus {

    /** 已初始化，等待上传分片（{@code initUpload} 创建后） */
    INIT,

    /**
     * 分片上传中。
     *
     * <p>{@code initUpload} 新任务默认为此状态，或 {@code PAUSED → resumeUpload} 恢复至此。</p>
     */
    UPLOADING,

    /**
     * 已暂停。
     *
     * <p>前端调用 {@code pauseUpload} 置此状态。已上传分片保留在 {@code file_chunk} 表，
     * 恢复时通过 {@code initUpload(fileMd5)} 断点续传。</p>
     */
    PAUSED,

    /**
     * 分片合并中。
     *
     * <p>{@code mergeChunks} 调用后短暂处于此状态，合并完成后回到 {@code COMPLETED}。
     * 用于防止重复合并。</p>
     */
    MERGING,

    /**
     * 上传完成。
     *
     * <p>全部分片上传完成或合并完成后置此状态。
     * 已完成的任务可通过 MD5 实现秒传。</p>
     */
    COMPLETED,

    /**
     * 已取消。
     *
     * <p>调用 {@code cancelUpload} 后置此状态。
     * 同时清理存储分片、DB 分片记录和 Redis 缓存。</p>
     */
    CANCELLED,

    /**
     * 失败。
     *
     * <p>上传或合并过程中发生不可恢复的错误后置此状态。
     * 错误详情记录在 {@code FileUploadTask.errorMessage} 中。</p>
     */
    FAILED
}
