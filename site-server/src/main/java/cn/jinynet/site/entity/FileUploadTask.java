package cn.jinynet.site.entity;

import cn.jinynet.site.types.enums.FileUploadTaskStatus;
import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

/**
 * 大文件分片上传任务实体。
 *
 * <p>替代原先 Redis Hash 存储的任务元数据，提供持久化、可查询、类型安全的
 * 任务管理。一个任务对应一次完整的大文件分片上传流程。</p>
 *
 * <h3>关联关系</h3>
 * <ul>
 *   <li>{@code businessId} → {@link FileMetadata#id()} — 视频草稿等业务关联</li>
 *   <li>{@code uploadId} — 与 {@code file_chunk} 表的关联键</li>
 *   <li>{@code fileInfoJson} — x-file-storage {@code FileInfo} 序列化，合并时反序列化使用</li>
 * </ul>
 *
 * <h3>与 Redis 关系</h3>
 * <p>本表是主存储。Redis 仅保留</p>
 * <pre>file:upload:md5_map:{md5} → uploadId (TTL 24h)</pre>
 * <p>用于秒传和断点续传的快速查找。过期后仍可通过本表的 {@code fileMd5} + {@code status} 索引找回。</p>
 *
 * @author jinty
 * @since 2.0
 */
@Entity
@Table(name = "file_upload_task")
public interface FileUploadTask extends EntityId, BaseEntity {

    /**
     * 上传任务 ID。
     *
     * <p>UUID 字符串（去横线），是前端与后端交互的主标识。
     * 独立于 {@code id}（自增主键），避免暴露内部 ID。</p>
     */
    @Key
    @Column(name = "upload_id")
    String uploadId();

    /**
     * 关联的业务 ID。
     *
     * <p>指向 {@link FileMetadata#id()}，用于视频草稿等场景。
     * 合并分片完成后，通过此 ID 更新对应的文件元数据。</p>
     */
    @Column(name = "business_id")
    @Nullable
    Long businessId();

    /** 原始文件名（含扩展名） */
    @Column(name = "file_name")
    String fileName();

    /** 文件总大小（字节），最大 10GB */
    @Column(name = "file_size")
    long fileSize();

    /**
     * 文件 MD5 哈希值。
     *
     * <p>用于秒传检测、断点续传恢复和最终完整性校验。</p>
     */
    @Column(name = "file_md5")
    String fileMd5();

    /** 分片大小（字节），默认 5MB */
    @Column(name = "chunk_size")
    long chunkSize();

    /** 分片总数（根据 fileSize / chunkSize 向上取整） */
    @Column(name = "total_chunks")
    int totalChunks();

    /**
     * 已上传分片数。
     *
     * <p>每完成一个分片 +1。当 {@code uploadedChunks == totalChunks} 时即可合并。</p>
     */
    @Column(name = "uploaded_chunks")
    int uploadedChunks();

    /**
     * 任务状态。
     *
     * <p>见 {@link FileUploadTaskStatus} 枚举的状态流转定义。</p>
     */
    @Column(name = "status")
    FileUploadTaskStatus status();

    /** x-file-storage 存储平台标识（如 {@code local-1}） */
    @Column(name = "platform")
    @Nullable
    String platform();

    /**
     * 错误信息。
     *
     * <p>上传或合并失败时记录具体原因。</p>
     */
    @Column(name = "error_message")
    @Nullable
    String errorMessage();

    /**
     * x-file-storage FileInfo 的 JSON 序列化。
     *
     * <p>存储 {@code initiateMultipartUpload} 返回的 FileInfo 对象。
     * 合并时反序列化后调用 {@code completeMultipartUpload(fileInfo, parts)}。</p>
     */
    @Column(name = "file_info_json")
    @Nullable
    String fileInfoJson();
}
