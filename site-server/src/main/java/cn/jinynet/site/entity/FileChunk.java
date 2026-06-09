package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 文件分片记录表
 * <p>
 * 用于大文件分片上传和断点续传，遵循 x-file-storage 推荐的分片表结构
 * </p>
 */
@Entity
@Table(name = "file_chunk")
public interface FileChunk extends EntityId, BaseEntity {

    /**
     * 上传任务ID（唯一标识一个上传任务）
     */
    @Key(group = "uk_file_chunk_upload_chunk")
    @Column(name = "upload_id")
    String uploadId();
    /**
     * 文件名（存储的文件名）
     */
    @Column(name = "filename")
    String filename();
    /**
     * 文件总大小（字节）
     */
    @Column(name = "file_size")
    long fileSize();
    /**
     * 分片序号（从1开始）
     */
    @Key(group = "uk_file_chunk_upload_chunk")
    @Column(name = "chunk_number")
    int chunkNumber();

    /**
     * 分片大小（字节）
     */
    @Column(name = "chunk_size")
    long chunkSize();

    /**
     * 总分片数
     */
    @Column(name = "total_chunks")
    int totalChunks();


    /**
     * 分片MD5值（用于校验）
     */
    @Column(name = "chunk_md5")
    @Nullable
    String chunkMd5();

    /**
     * 上传状态：pending/uploading/completed/failed
     */
    @Column(name = "status")
    String status();

//    /**
//     * 存储平台标识（如 local-1）
//     */
//    @Column(name = "platform")
//    @Nullable
//    String platform();
//
//    /**
//     * 基础路径
//     */
//    @Column(name = "base_path")
//    @Nullable
//    String basePath();

    /**
     * 分片文件路径（存储平台上的相对路径）
     */
    @Column(name = "path")
    @Nullable
    String path();

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    @Nullable
    Long userId();
    /**
     * 上传完成时间
     */
    @Column(name = "completed_at")
    @Nullable
    LocalDateTime completedAt();


}