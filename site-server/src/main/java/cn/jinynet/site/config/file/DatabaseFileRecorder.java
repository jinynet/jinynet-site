package cn.jinynet.site.config.file;

import cn.jinynet.site.entity.*;
import cn.jinynet.starter.file.utils.FileTypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据库文件记录器
 * <p>
 * 实现 Dromara x-file-storage 的 FileRecorder 接口，负责将文件元数据持久化到数据库。
 * 所有通过 x-file-storage 上传的文件都会自动触发 save/update/delete 方法，
 * 确保文件元数据与实际存储文件保持同步。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseFileRecorder implements FileRecorder {

    private final JSqlClient sqlClient;

    /**
     * 保存文件记录
     * <p>
     * 当文件通过 x-file-storage 上传成功后，会自动调用此方法。
     * 将文件元数据保存到数据库。
     * </p>
     *
     * @param info 文件信息
     * @return true-保存成功，false-保存失败
     */
    @Override
    public boolean save(FileInfo info) {
        log.info("保存文件记录: {}, URL={}, Filename={}, Path={}",
                info.getOriginalFilename(), info.getUrl(), info.getFilename(), info.getPath());
        try {
            FileMetadata metadata = FileMetadataDraft.$.produce(draft -> {
                draft.setUrl(info.getUrl());
                draft.setPath(info.getPath());
                draft.setFilename(info.getFilename());
                draft.setOriginalFilename(info.getOriginalFilename());
                draft.setFileExt(info.getExt() != null ? info.getExt().replace(".", "") : "");
                draft.setFileSize(info.getSize());
                draft.setContentType(info.getContentType());
                draft.setPlatform(info.getPlatform());
                draft.setBasePath(info.getBasePath());
                draft.setIsPublic(false);
                draft.setFileType(FileTypeUtils.detect(info.getExt()));
                draft.setCreatedAt(LocalDateTime.now());
                draft.setUpdatedAt(LocalDateTime.now());
            });

            sqlClient.saveCommand(metadata)
                    .setMode(SaveMode.UPSERT)
                    .execute();

            log.info("文件记录保存成功: {}", info.getUrl());
            return true;
        } catch (Exception e) {
            log.error("保存文件记录失败: {}", info.getUrl(), e);
            return false;
        }
    }

    /**
     * 更新文件记录
     * <p>
     * 当文件的元数据发生变化时调用（如修改文件名、路径等）。
     * </p>
     *
     * @param info 文件信息
     */
    @Override
    public void update(FileInfo info) {
        log.info("更新文件记录: {}", info.getUrl());
        try {
            FileMetadata existing = sqlClient.createQuery(FileMetadataTable.$)
                    .where(FileMetadataTable.$.url().eq(info.getUrl()))
                    .select(FileMetadataTable.$)
                    .fetchOneOrNull();

            if (existing != null) {
                FileMetadata updated = FileMetadataDraft.$.produce(existing, draft -> {
                    draft.setFilename(info.getFilename());
                    draft.setFileExt(info.getExt().replace(".", ""));
                    draft.setPath(info.getPath());
                    draft.setUrl(info.getUrl());
                    draft.setFileSize(info.getSize());
                    draft.setContentType(info.getContentType());
                    draft.setPlatform(info.getPlatform());
                    draft.setBasePath(info.getBasePath());
                    draft.setUpdatedAt(LocalDateTime.now());
                });

                sqlClient.saveCommand(updated)
                        .setMode(SaveMode.UPSERT)
                        .execute();

                log.info("文件记录更新成功: {}", info.getUrl());
            }
        } catch (Exception e) {
            log.error("更新文件记录失败: {}", info.getUrl(), e);
        }
    }

    /**
     * 根据URL获取文件记录
     * <p>
     * 用于删除文件时获取文件的存储路径信息。
     * x-file-storage 删除文件前会调用此方法获取文件路径。
     * </p>
     *
     * @param url 文件URL
     * @return 文件信息，如果未找到返回null
     */
    @Override
    public FileInfo getByUrl(String url) {
        log.info("根据URL获取文件记录: {}", url);
        try {
            if (url == null || url.isEmpty()) {
                return null;
            }

            // 优先查询文件元数据表（普通文件）
            FileInfo metadataFileInfo = getMetadataFileInfo(url);
            if (metadataFileInfo != null) {
                return metadataFileInfo;
            }
            // 都找不到，返回null
            log.warn("未找到URL对应的文件记录: {}", url);
            return null;
        } catch (Exception e) {
            log.error("查询文件记录失败: {}", url, e);
            return null;
        }
    }
    /**
     * 从文件元数据表获取文件信息
     */
    private FileInfo getMetadataFileInfo(String url) {
        FileMetadata metadata = sqlClient.createQuery(FileMetadataTable.$)
                .where(FileMetadataTable.$.url().eq(url))
                .select(FileMetadataTable.$)
                .fetchOneOrNull();

        if (metadata == null) {
            return null;
        }
        log.info("找到文件记录: path={}, url={}", metadata.path(), metadata.url());
        return createFileInfo(metadata);
    }

    /**
     * 根据URL删除文件记录
     * <p>
     * 当文件被删除时调用，同时删除数据库中的元数据记录。
     * x-file-storage 会先调用 getByUrl 获取文件路径删除存储文件，
     * 然后调用此方法删除数据库记录。
     * </p>
     *
     * @param url 文件URL
     * @return true-删除成功，false-删除失败
     */
    @Override
    public boolean delete(String url) {
        log.info("根据URL删除文件记录: {}", url);
        try {
            if (url == null || url.isEmpty()) {
                return false;
            }

            // 优先删除文件元数据记录（普通文件）
            return deleteMetadataRecord(url);
        } catch (Exception e) {
            log.error("删除文件记录失败: {}", url, e);
            return false;
        }
    }

    /**
     * 删除文件元数据记录
     */
    private boolean deleteMetadataRecord(String url) {
        int deleted = sqlClient.createDelete(FileMetadataTable.$)
                .where(FileMetadataTable.$.url().eq(url))
                .execute();

        log.info("通过URL删除文件元数据: {}, 受影响行数: {}", url, deleted);
        return deleted > 0;
    }

    /**
     * 保存分片信息
     * <p>
     * 用于大文件分片上传时记录每个分片的上传状态，
     * 支持断点续传和分片校验。
     * </p>
     *
     * @param filePartInfo 分片信息
     */
    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        try {
            FileChunk chunk = FileChunkDraft.$.produce(draft -> {
                draft.setUploadId(filePartInfo.getUploadId());
                draft.setChunkSize(filePartInfo.getPartSize());
                draft.setChunkNumber(filePartInfo.getPartNumber());
//                draft.setPlatform(filePartInfo.getPlatform());
                draft.setStatus("completed");
            });

            sqlClient.saveCommand(chunk)
                    .setMode(SaveMode.INSERT_ONLY)
                    .execute();

            log.debug("保存分片记录成功: uploadId={}, chunkNumber={}",
                    filePartInfo.getUploadId(), filePartInfo.getPartNumber());
        } catch (Exception e) {
            log.error("保存分片记录失败", e);
        }
    }

    /**
     * 删除分片记录
     * <p>
     * 当分片上传完成或取消时，删除所有相关的分片记录。
     * </p>
     *
     * @param uploadId 上传任务ID
     */
    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        try {
            int deleted = sqlClient.createDelete(FileChunkTable.$)
                    .where(FileChunkTable.$.uploadId().eq(uploadId))
                    .execute();

            log.info("删除分片记录: uploadId={}, 删除数量={}", uploadId, deleted);
        } catch (Exception e) {
            log.error("删除分片记录失败", e);
        }
    }

    /**
     * 根据文件元数据创建FileInfo对象
     */
    public static FileInfo createFileInfo(FileMetadata metadata) {
        return new FileInfo() {
            @Override
            public String getPath() {
                return metadata.path();
            }

            @Override
            public String getUrl() {
                return metadata.url();
            }

            @Override
            public String getFilename() {
                return metadata.filename();
            }

            @Override
            public String getOriginalFilename() {
                return metadata.originalFilename();
            }

            @Override
            public String getExt() {
                return metadata.fileExt();
            }

            @Override
            public Long getSize() {
                return metadata.fileSize();
            }

            @Override
            public String getContentType() {
                return metadata.contentType();
            }

            @Override
            public String getPlatform() {
                return metadata.platform();
            }

            @Override
            public String getBasePath() {
                return metadata.basePath();
            }

            @Override
            public String toString() {
                return "FileInfo{path=" + getPath() + ", url=" + getUrl() + "}";
            }
        };
    }
}