package cn.jinynet.site.api.admin;

import cn.dev33.satoken.stp.StpUtil;
import cn.jinynet.site.service.LargeFileUploadService;
import cn.jinynet.site.entity.FileUploadTask;
import cn.jinynet.starter.common.types.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 大文件分片上传 API 控制器。
 *
 * <h3>上传流程</h3>
 * <ol>
 *   <li>{@link #initUpload} — 初始化上传（含秒传/断点续传检测）</li>
 *   <li>{@link #uploadChunk} — 逐片上传（UPSERT 幂等）</li>
 *   <li>{@link #mergeChunks} — 合并分片为完整文件</li>
 * </ol>
 *
 * <h3>任务生命周期</h3>
 * <pre>
 * INIT → UPLOADING ←→ PAUSED → UPLOADING → COMPLETED
 *                 ↘                    ↘
 *               CANCELLED           CANCELLED
 * </pre>
 *
 * <h3>重新登录后恢复</h3>
 * <ol>
 *   <li>调用 {@link #getTaskList} 获取所有任务（含 PAUSED）</li>
 *   <li>选择任务 → 同一文件计算 MD5 → {@link #initUpload}（断点续传）</li>
 *   <li>仅上传未完成分片 → {@link #mergeChunks} 完成</li>
 * </ol>
 *
 * @author jinty
 * @since 2.0
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class LargeFileUploadApi {

    /**
     * 大文件上传服务（DB 主存储 + Redis MD5 缓存）
     */
    private final LargeFileUploadService largeFileUploadService;

    /**
     * 初始化分片上传。
     *
     * <p>调用 {@link LargeFileUploadService#initUpload} 完成：
     * <ul>
     *   <li>文件大小校验（≤ 10GB）</li>
     *   <li>x-file-storage {@code initiateMultipartUpload}</li>
     *   <li>INSERT {@link FileUploadTask}（DB 持久化）</li>
     *   <li>Redis 缓存 MD5 → uploadId 映射（24h TTL）</li>
     * </ul>
     *
     * <p>如果 {@code fileMd5} 已存在且任务未完成，返回已上传分片列表（断点续传/秒传）。</p>
     *
     * @param fileName       原始文件名
     * @param fileSize       文件总大小（字节）
     * @param chunkSize      分片大小（可选，默认 5MB）
     * @param fileMd5        文件 MD5（可选，用于秒传/断点续传）
     * @param createWithFile 是否同时创建 FileMetadata（视频草稿场景）
     * @param businessId     业务 ID（草稿记录 ID，用于关联业务数据）
     * @return {@code Map} 包含字段：
     * <ul>
     *   <li>{@code uploadId} — 上传任务 ID</li>
     *   <li>{@code chunkSize} — 实际分片大小</li>
     *   <li>{@code totalChunks} — 总分片数</li>
     *   <li>{@code uploadedChunks} — 已上传分片编号列表（断点续传时非空）</li>
     * </ul>
     */
    @PostMapping("/large/init")
    public Result<Map<String, Object>> initUpload(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") long fileSize,
            @RequestParam(value = "chunkSize", required = false) Long chunkSize,
            @RequestParam(value = "fileMd5", required = false) String fileMd5,
            @RequestParam(value = "createWithFile", required = false, defaultValue = "false") Boolean createWithFile,
            @RequestParam(value = "businessId", required = false) Long businessId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(largeFileUploadService.initUpload(fileName, fileSize, chunkSize, fileMd5, userId, createWithFile, businessId));
    }

    /**
     * 上传单个分片。
     *
     * <p>分片通过 x-file-storage {@code uploadPart} 上传，成功后写入 {@code file_chunk} 表并更新任务进度。
     * 重复分片由唯一约束 {@code uk_file_chunk_number(upload_id, chunk_number)} 自动忽略（UPSERT 幂等）。</p>
     *
     * @param uploadId    上传任务 ID（由 {@link #initUpload} 返回）
     * @param chunkNumber 分片序号（从 1 开始）
     * @param chunk       分片文件（multipart/form-data）
     * @param chunkMd5    分片 MD5（可选，用于校验）
     * @return {@code Map} 包含字段：
     * <ul>
     *   <li>{@code chunkNumber} — 当前分片序号</li>
     *   <li>{@code uploadedCount} — 已完成分片数</li>
     *   <li>{@code totalChunks} — 总分片数</li>
     *   <li>{@code isComplete} — 是否全部上传完成</li>
     *   <li>{@code progress} — 进度百分比</li>
     * </ul>
     */
    @PostMapping("/large/chunk")
    public Result<Map<String, Object>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam(value = "chunkMd5", required = false) String chunkMd5) {
        return Result.success(largeFileUploadService.uploadChunk(uploadId, chunkNumber, chunk, chunkMd5));
    }

    /**
     * 获取已上传的分片列表（断点续传用）。
     *
     * @param uploadId 上传任务 ID
     * @return {@code Map} 包含 {@code uploadedChunks}（已完成分片编号列表）和 {@code progress}
     */
    @GetMapping("/large/chunks")
    public Result<Map<String, Object>> getUploadedChunks(@RequestParam("uploadId") String uploadId) {
        return Result.success(largeFileUploadService.getUploadedChunks(uploadId));
    }

    /**
     * 合并所有分片为完整文件。
     *
     * <p>调用 x-file-storage {@code completeMultipartUpload}，合并后更新 {@code file_metadata} 表，
     * 清理分片记录，并将任务状态置为 {@code COMPLETED}。</p>
     *
     * @param uploadId    上传任务 ID
     * @param categoryId  分类 ID（可选）
     * @param description 文件描述（可选）
     * @return {@code Map} 包含 {@code fileName}、{@code fileSize}、{@code fileUrl}、{@code filePath}
     */
    @PostMapping("/large/merge")
    public Result<Map<String, Object>> mergeChunks(
            @RequestParam("uploadId") String uploadId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "originalFilename", required = false) String originalFilename,
            @RequestParam(value = "isPublic", required = false, defaultValue = "true") Boolean isPublic) {
        return Result.success(largeFileUploadService.mergeChunks(uploadId, categoryId, description, originalFilename, isPublic));
    }

    /**
     * 暂停上传任务。
     *
     * <p>将任务状态从 {@code UPLOADING} 置为 {@code PAUSED}。
     * 前端需停止分片循环；已上传分片保留，恢复时通过 {@link #initUpload} 断点续传。</p>
     *
     * @param uploadId 上传任务 ID（仅 {@code UPLOADING} 状态可暂停）
     */
    @PostMapping("/large/pause")
    public Result<Void> pauseUpload(@RequestParam("uploadId") String uploadId) {
        largeFileUploadService.pauseUpload(uploadId);
        return Result.success();
    }

    /**
     * 恢复上传任务。
     *
     * <p>将任务状态从 {@code PAUSED} 恢复为 {@code UPLOADING}。
     * 前端需重新调用 {@link #initUpload} 获取已完成分片列表，继续上传剩余分片。</p>
     *
     * @param uploadId 上传任务 ID（仅 {@code PAUSED} 状态可恢复）
     */
    @PostMapping("/large/resume")
    public Result<Void> resumeUpload(@RequestParam("uploadId") String uploadId) {
        largeFileUploadService.resumeUpload(uploadId);
        return Result.success();
    }

    /**
     * 取消上传任务。
     *
     * <p>清理存储分片 → 删除 DB 分片记录 → 任务状态置为 {@code CANCELLED} → 清除 Redis MD5 缓存。
     * 注意：不删除已生成的 {@code FileMetadata}（如有），由业务层决定是否清理。</p>
     *
     * @param uploadId 上传任务 ID
     */
    @DeleteMapping("/large/cancel")
    public Result<Void> cancelUpload(@RequestParam("uploadId") String uploadId) {
        largeFileUploadService.cancelUpload(uploadId);
        return Result.success();
    }

    /**
     * 获取上传进度。
     *
     * @param uploadId 上传任务 ID
     * @return 同 {@link #getUploadedChunks}
     */
    @GetMapping("/large/progress")
    public Result<Map<String, Object>> getUploadProgress(@RequestParam("uploadId") String uploadId) {
        return Result.success(largeFileUploadService.getUploadProgress(uploadId));
    }

    /**
     * 获取所有上传任务列表（最多 50 条，按更新时间倒序）。
     *
     * <p>用于重新登录后找回暂停/失败的任务，或侧边面板展示。</p>
     *
     * @return {@link FileUploadTask} 列表（含完整状态信息）
     */
    @GetMapping("/large/tasks")
    public Result<List<FileUploadTask>> getTaskList() {
        return Result.success(largeFileUploadService.getTaskList());
    }

    /**
     * 删除大文件上传任务记录
     *
     * <p>仅允许已终止状态：CANCELLED、FAILED、COMPLETED</p>
     *
     * @param uploadId id
     */
    @DeleteMapping("/large/tasks/{uploadId}")
    public Result<Void> deleteTask(@PathVariable("uploadId") String uploadId) {
        largeFileUploadService.deleteTask(uploadId);
        return Result.success("删除成功");
    }

}
