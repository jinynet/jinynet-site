package cn.jinynet.site.service;

import cn.jinynet.site.entity.*;
import cn.jinynet.site.types.enums.FileUploadTaskStatus;
import cn.jinynet.starter.file.types.exception.StorageBizCode;
import cn.jinynet.starter.file.types.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 大文件分片上传服务。
 *
 * <h3>存储策略</h3>
 * <ul>
 *   <li>任务元数据 → {@link FileUploadTask} 表（DB 持久化）</li>
 *   <li>分片记录 → {@link FileChunk} 表（唯一约束幂等）</li>
 *   <li>MD5 映射 → Redis {@code file:upload:md5_map:*}</li>
 *   <li>文件实体 → x-file-storage multipart upload</li>
 * </ul>
 *
 * <h3>核心流程</h3>
 * <ol>
 *   <li>{@link #initUpload} — 秒传/断点续传检测 + 新建任务</li>
 *   <li>{@link #uploadChunk} — 逐片上传（UPSERT 幂等）</li>
 *   <li>{@link #mergeChunks} — completeMultipartUpload + 更新 FileMetadata</li>
 * </ol>
 *
 * <h3>任务生命周期</h3>
 * <pre>
 * INIT → UPLOADING ←→ PAUSED → UPLOADING → COMPLETED → (mergeChunks)
 *                 ↘                    ↘
 *               CANCELLED           CANCELLED
 * </pre>
 *
 * @author jinty
 * @since 2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LargeFileUploadService {

    private final FileStorageService xFileStorageService;
    private final JSqlClient sqlClient;
    private final StringRedisTemplate redisTemplate;
    private final FileService fileService;
    private final FileUploadTaskService fileUploadTaskService;
    private final ObjectMapper objectMapper;

    private static final String MD5_TO_UPLOAD_ID_KEY = "file:upload:md5_map:";
    private static final Duration MD5_CACHE_TTL = Duration.ofHours(24);
    private static final long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024;

    /**
     * 初始化分片上传。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> initUpload(String fileName, long fileSize, Long chunkSize, String fileMd5,
                                          Long userId, Boolean createWithFile, Long businessId) {

        StorageException.throwIf(fileSize <= 0 || fileSize > MAX_FILE_SIZE, StorageBizCode.SIZE_EXCEED);
        StorageException.throwIf(fileName == null || fileName.trim().isEmpty(), StorageBizCode.NAME_EMPTY);

        validateFileType(fileName);

        long actualChunkSize = chunkSize != null && chunkSize > 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
        int totalChunks = (int) Math.ceil((double) fileSize / actualChunkSize);

        // 断点续传 / 秒传：通过 MD5 查找已有任务
        if (fileMd5 != null && !fileMd5.isEmpty()) {
            String existingId = redisTemplate.opsForValue().get(MD5_TO_UPLOAD_ID_KEY + fileMd5);
            if (existingId != null) {
                FileUploadTask existingTask = sqlClient.createQuery(FileUploadTaskTable.$)
                        .where(FileUploadTaskTable.$.uploadId().eq(existingId))
                        .where(FileUploadTaskTable.$.status().ne(FileUploadTaskStatus.COMPLETED))
                        .select(FileUploadTaskTable.$)
                        .fetchOptional().orElse(null);
                if (existingTask != null) {
                    return buildResumeResult(existingTask);
                }
            }
        }

        // 新任务
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        FileInfo fileInfo = xFileStorageService.initiateMultipartUpload()
                .setOriginalFilename(fileName).setSize(fileSize).init();

        // 写入 DB
        FileUploadTask task = FileUploadTaskDraft.$.produce(draft -> {
            draft.setUploadId(uploadId);
            draft.setBusinessId(businessId);
            draft.setFileName(fileName);
            draft.setFileSize(fileSize);
            draft.setChunkSize(actualChunkSize);
            draft.setTotalChunks(totalChunks);
            draft.setUploadedChunks(0);
            draft.setStatus(FileUploadTaskStatus.UPLOADING);
            draft.setPlatform(fileInfo.getPlatform());
            draft.setFileMd5(fileMd5 != null ? fileMd5 : "");
            try {
                draft.setFileInfoJson(objectMapper.writeValueAsString(fileInfo));
            } catch (Exception ignored) {
            }
        });
        sqlClient.saveCommand(task).setMode(SaveMode.INSERT_ONLY).execute();

        // 秒传：如果 createWithFile=true，创建 FileMetadata
        if (Boolean.TRUE.equals(createWithFile)) {
            fileService.createFileMetadata(fileInfo.getPath(), fileName, fileSize, null, "视频源文件", false, fileInfo.getUrl());
        }

        // Redis 仅缓存 MD5 映射
        if (fileMd5 != null && !fileMd5.isEmpty())
            redisTemplate.opsForValue().set(MD5_TO_UPLOAD_ID_KEY + fileMd5, uploadId, MD5_CACHE_TTL);

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("chunkSize", actualChunkSize);
        result.put("totalChunks", totalChunks);
        result.put("uploadedChunks", List.of());
        log.info("初始化上传, uploadId={}, fileName={}, totalChunks={}", uploadId, fileName, totalChunks);
        return result;
    }

    private Map<String, Object> buildResumeResult(FileUploadTask task) {
        List<Integer> chunks = sqlClient.createQuery(FileChunkTable.$)
                .where(FileChunkTable.$.uploadId().eq(task.uploadId()))
                .where(FileChunkTable.$.status().eq("completed"))
                .orderBy(FileChunkTable.$.chunkNumber().asc())
                .select(FileChunkTable.$.chunkNumber()).execute();
        Map<String, Object> r = new HashMap<>();
        r.put("uploadId", task.uploadId());
        r.put("chunkSize", task.chunkSize());
        r.put("totalChunks", task.totalChunks());
        r.put("uploadedChunks", chunks);
        r.put("uploadedCount", chunks.size());
        r.put("progress", task.totalChunks() > 0 ? (double) chunks.size() / task.totalChunks() * 100 : 0);
        return r;
    }

    /**
     * 上传单个分片。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadChunk(String uploadId, int chunkNumber, MultipartFile chunk, String chunkMd5) {
        FileUploadTask task = fileUploadTaskService.getTask(uploadId);

        if (chunkNumber < 1 || chunkNumber > task.totalChunks())
            throw new RuntimeException("分片序号无效");

        try {
            String actualMd5 = calculateMd5(chunk.getInputStream());
            if (chunkMd5 != null && !chunkMd5.equals(actualMd5))
                throw new RuntimeException("分片 MD5 校验失败");

            FileInfo fileInfo = objectMapper.readValue(task.fileInfoJson(), FileInfo.class);
            xFileStorageService.uploadPart(fileInfo, chunkNumber, chunk.getInputStream(), chunk.getSize()).upload();

            // 幂等：INSERT ... ON CONFLICT DO NOTHING
            FileChunk chunkRecord = FileChunkDraft.$.produce(draft -> {
                draft.setUploadId(uploadId);
                draft.setFilename(task.fileName());
                draft.setFileSize(task.fileSize());
                draft.setChunkNumber(chunkNumber);
                draft.setChunkSize(chunk.getSize());
                draft.setTotalChunks(task.totalChunks());
                draft.setChunkMd5(actualMd5);
                draft.setStatus("completed");
                draft.setCompletedAt(LocalDateTime.now());
            });
            sqlClient.saveCommand(chunkRecord).setMode(SaveMode.UPSERT).execute();

            // 统计已上传数
            long count = sqlClient.createQuery(FileChunkTable.$)
                    .where(FileChunkTable.$.uploadId().eq(uploadId))
                    .where(FileChunkTable.$.status().eq("completed"))
                    .select(FileChunkTable.$.count()).fetchOptional().orElse(0L);

            // 更新任务计数
            FileUploadTask updated = FileUploadTaskDraft.$.produce(task, d -> {
                d.setUploadedChunks((int) count);
                if (count >= task.totalChunks()) d.setStatus(FileUploadTaskStatus.COMPLETED);
            });
            sqlClient.saveCommand(updated).setMode(SaveMode.UPDATE_ONLY).execute();

            Map<String, Object> r = new HashMap<>();
            r.put("chunkNumber", chunkNumber);
            r.put("chunkMd5", actualMd5);
            r.put("uploadedCount", (int) count);
            r.put("totalChunks", task.totalChunks());
            r.put("isComplete", count >= task.totalChunks());
            r.put("progress", (double) count / task.totalChunks() * 100);
            return r;
        } catch (Exception e) {
            log.error("分片上传失败，uploadId={}, chunkNumber={}", uploadId, chunkNumber, e);
            throw new StorageException(StorageBizCode.UPLOAD_CHUNK_FAIL);
        }
    }

    /**
     * 获取已上传分片列表。
     */
    public Map<String, Object> getUploadedChunks(String uploadId) {
        FileUploadTask task = fileUploadTaskService.getTask(uploadId);
        List<Integer> chunks = sqlClient.createQuery(FileChunkTable.$)
                .where(FileChunkTable.$.uploadId().eq(uploadId))
                .where(FileChunkTable.$.status().eq("completed"))
                .orderBy(FileChunkTable.$.chunkNumber().asc())
                .select(FileChunkTable.$.chunkNumber()).execute();
        Map<String, Object> r = new HashMap<>();
        r.put("uploadId", uploadId);
        r.put("totalChunks", task.totalChunks());
        r.put("uploadedChunks", chunks);
        r.put("uploadedCount", chunks.size());
        r.put("progress", task.totalChunks() > 0 ? (double) chunks.size() / task.totalChunks() * 100 : 0);
        r.put("status", task.status().name());
        return r;
    }

    /**
     * 暂停上传。
     */
    @Transactional(rollbackFor = Exception.class)
    public void pauseUpload(String uploadId) {
        FileUploadTask task = fileUploadTaskService.getTask(uploadId);
        if (task.status() != FileUploadTaskStatus.UPLOADING)
            throw new RuntimeException("只有上传中的任务才能暂停");
        FileUploadTask updated = FileUploadTaskDraft.$.produce(task,
                d -> d.setStatus(FileUploadTaskStatus.PAUSED));
        sqlClient.saveCommand(updated).setMode(SaveMode.UPDATE_ONLY).execute();
    }

    /**
     * 恢复上传。
     */
    @Transactional(rollbackFor = Exception.class)
    public void resumeUpload(String uploadId) {
        FileUploadTask task = fileUploadTaskService.getTask(uploadId);
        if (task.status() != FileUploadTaskStatus.PAUSED)
            throw new RuntimeException("只有暂停中的任务才能恢复");
        FileUploadTask updated = FileUploadTaskDraft.$.produce(task,
                d -> d.setStatus(FileUploadTaskStatus.UPLOADING));
        sqlClient.saveCommand(updated).setMode(SaveMode.UPDATE_ONLY).execute();
    }

    /**
     * 获取任务列表。
     */
    public List<FileUploadTask> getTaskList() {
        return sqlClient.createQuery(FileUploadTaskTable.$)
                .orderBy(FileUploadTaskTable.$.updatedAt().desc())
                .select(FileUploadTaskTable.$)
                .limit(50).execute();
    }

    /**
     * 合并分片。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mergeChunks(String uploadId, Long categoryId, String description,
                                           String originalFilename, Boolean isPublic) {
        FileUploadTask task = fileUploadTaskService.getTask(uploadId);
        long count = sqlClient.createQuery(FileChunkTable.$)
                .where(FileChunkTable.$.uploadId().eq(uploadId))
                .where(FileChunkTable.$.status().eq("completed"))
                .select(FileChunkTable.$.count()).fetchOptional().orElse(0L);
        if (count < task.totalChunks()) throw new RuntimeException("还有分片未上传");
        try {
            FileInfo fileInfo = objectMapper.readValue(task.fileInfoJson(), FileInfo.class);
            List<FileChunk> chunks = sqlClient.createQuery(FileChunkTable.$)
                    .where(FileChunkTable.$.uploadId().eq(uploadId))
                    .where(FileChunkTable.$.status().eq("completed"))
                    .orderBy(FileChunkTable.$.chunkNumber().asc())
                    .select(FileChunkTable.$).execute();
            List<FilePartInfo> parts = new ArrayList<>();
            for (FileChunk c : chunks) {
                FilePartInfo p = new FilePartInfo();
                p.setPartNumber(c.chunkNumber());
                p.setETag(c.chunkMd5());
                parts.add(p);
            }
            FileInfo ci = xFileStorageService.completeMultipartUpload(fileInfo)
                    .setPartInfoList(parts).complete();
            String fileUrl = ci.getUrl();
            String path = ci.getPath();
            String displayName = (originalFilename != null && !originalFilename.isEmpty())
                    ? originalFilename : ci.getOriginalFilename();
            boolean pub = isPublic != null ? isPublic : true;
            if (task.businessId() != null)
                fileService.updateFileMetadata(task.businessId(), path, displayName,
                        task.fileSize(), categoryId, description, pub, fileUrl);
            else
                fileService.createFileMetadata(path, displayName, task.fileSize(),
                        categoryId, description, pub, fileUrl);
            sqlClient.createDelete(FileChunkTable.$).where(FileChunkTable.$.uploadId().eq(uploadId)).execute();
            FileUploadTask ut = FileUploadTaskDraft.$.produce(task, d -> d.setStatus(FileUploadTaskStatus.COMPLETED));
            sqlClient.saveCommand(ut).setMode(SaveMode.UPDATE_ONLY).execute();
            if (task.fileMd5() != null && !task.fileMd5().isEmpty())
                redisTemplate.delete(MD5_TO_UPLOAD_ID_KEY + task.fileMd5());
            Map<String, Object> r = new HashMap<>();
            r.put("fileName", task.fileName());
            r.put("fileSize", task.fileSize());
            r.put("fileUrl", fileUrl);
            r.put("filePath", path);
            return r;
        } catch (Exception e) {
            log.error("合并失败 uploadId={}", uploadId, e);
            throw new StorageException(StorageBizCode.MERGE_CHUNK_FAIL);
        }
    }

    /**
     * 取消上传。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpload(String uploadId) {
        FileUploadTask task;
        FileInfo fileInfo;
        try {
            task = fileUploadTaskService.getTask(uploadId);
            fileInfo = objectMapper.readValue(task.fileInfoJson(), FileInfo.class);
        } catch (Exception e) {
            log.error("取消上传失败 uploadId={}", uploadId, e);
            return;
        }
        xFileStorageService.abortMultipartUpload(fileInfo).abort() ;

        // 更新任务状态
        FileUploadTask updated = FileUploadTaskDraft.$.produce(task,
                d -> d.setStatus(FileUploadTaskStatus.CANCELLED));
        sqlClient.saveCommand(updated).setMode(SaveMode.UPDATE_ONLY).execute();

        if (task.fileMd5() != null && !task.fileMd5().isEmpty())
            redisTemplate.delete(MD5_TO_UPLOAD_ID_KEY + task.fileMd5());
        log.info("取消上传任务成功，uploadId={}", uploadId);
    }

    public Map<String, Object> getUploadProgress(String uploadId) {
        return getUploadedChunks(uploadId);
    }

    public void validateFileType(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isEmpty()) throw new StorageException(StorageBizCode.FORMAT_NOT_SUPPORT);
    }

    /**
     * 删除上传任务记录（仅允许已终止状态：CANCELLED、FAILED、COMPLETED）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(String uploadId) {
        fileUploadTaskService.deleteTask(uploadId, FileUploadTaskStatus.CANCELLED, FileUploadTaskStatus.FAILED,
                FileUploadTaskStatus.COMPLETED);
    }

    /**
     * 清理过期上传任务（超过指定天数未完成的任务及关联分片）
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanStaleTasks(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        List<String> staleIds = sqlClient.createQuery(FileUploadTaskTable.$)
                .where(FileUploadTaskTable.$.createdAt().lt(threshold))
                .where(FileUploadTaskTable.$.status().notIn(
                        Arrays.asList(FileUploadTaskStatus.COMPLETED, FileUploadTaskStatus.CANCELLED)))
                .select(FileUploadTaskTable.$.uploadId())
                .execute();
        if (staleIds.isEmpty()) return 0;

        int count = 0;
        for (String uid : staleIds) {
            try {
                cancelUpload(uid);
                count++;
            } catch (Exception e) {
                log.warn("清理过期任务失败 uploadId={}: {}", uid, e.getMessage());
            }
        }
        log.info("清理过期上传任务完成: {}/{} 个", count, staleIds.size());
        return count;
    }

    private String calculateMd5(InputStream is) {
        try {
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            return null;
        }
    }
}
