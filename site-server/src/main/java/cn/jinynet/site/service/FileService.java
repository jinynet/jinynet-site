package cn.jinynet.site.service;

import cn.hutool.core.io.file.FileNameUtil;
import cn.jinynet.site.config.file.DatabaseFileRecorder;
import cn.jinynet.site.entity.*;
import cn.jinynet.site.entity.dto.*;
import cn.jinynet.starter.common.types.request.PageRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件服务
 * <p>
 * 提供文件上传、下载、管理等功能，基于 Dromara x-file-storage 实现文件存储。
 * 采用双重保存机制：
 * 1. DatabaseFileRecorder 自动保存文件基本元数据（path, url, filename, size, ext）
 * 2. FileService 保存业务扩展字段（categoryId, description, isPublic）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService xFileStorageService;
    private final JSqlClient sqlClient;

    /**
     * 获取文件列表
     */
    public Page<FileMetadata> getFileList(FileMetadataSpecification sc, PageRequest pageRequest) {
        List<Order> orders = pageRequest.order() ?
                Order.makeOrders(FileMetadataTable.$, pageRequest.getOrderBy()) :
                Order.makeOrders(FileMetadataTable.$, "createdAt desc");
        return sqlClient.createQuery(FileMetadataTable.$)
                .where(sc)
                .orderBy(orders)
                .select(FileMetadataTable.$.fetch(FileList.METADATA.getFetcher()))
                .fetchPage(pageRequest.getPageIndex(), pageRequest.getPageSize());
    }

    /**
     * 普通上传文件
     * <p>
     * 1. 上传文件到 x-file-storage（DatabaseFileRecorder 会自动保存基本元数据）
     * 2. 根据 URL 查询记录，如果已存在则更新业务字段，如果不存在则创建新记录
     * </p>
     *
     * @param file        上传的文件
     * @param categoryId  分类ID（可选）
     * @param description 文件描述（可选）
     * @param isPublic    是否公开（默认true）
     * @return 上传结果，包含文件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMetadata upload(MultipartFile file, Long categoryId, String description, Boolean isPublic) {
        log.info("开始上传文件: {}, categoryId={}, description={}", file.getOriginalFilename(), categoryId, description);

        // 生成唯一文件名，避免重复
        final String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null) {
            ext = FileNameUtil.getSuffix(originalFilename);
        }

        final String extension = ext;

        // 上传到文件存储服务（DatabaseFileRecorder.save() 会自动保存基本元数据）
        FileInfo fileInfo = xFileStorageService.of(file)
                .upload();

        log.info("文件上传成功: {}, URL={}", originalFilename, fileInfo.getUrl());

        // 获取分类名称
        final String categoryName;
        if (categoryId != null) {
            FileCategory category = sqlClient.findById(FileCategory.class, categoryId);
            categoryName = category != null ? category.name() : null;
        } else {
            categoryName = null;
        }

        final String filePath = fileInfo.getPath();
        final String fileUrl = fileInfo.getUrl();
        // 根据 URL 查询是否已存在记录
        List<FileMetadata> existingList = sqlClient.createQuery(FileMetadataTable.$)
                .where(FileMetadataTable.$.url().eq(fileInfo.getUrl()))
                .select(FileMetadataTable.$)
                .execute();

        FileMetadata result;
        if (existingList != null && !existingList.isEmpty()) {
            // 记录已存在，更新业务字段
            log.info("文件记录已存在，更新业务字段: {}", fileUrl);
            FileMetadata existing = existingList.getFirst();
            FileMetadata updated = FileMetadataDraft.$.produce(existing, draft -> {
                // 更新 path 字段（可能 DatabaseFileRecorder.save() 时 path 为空）
                if (filePath != null && !filePath.isEmpty()) {
                    draft.setPath(filePath);
                }
                if (categoryId != null) {
                    draft.setCategoryId(categoryId);
                    draft.setCategoryName(categoryName);
                }
                if (description != null) {
                    draft.setDescription(description);
                }
                if (isPublic != null) {
                    draft.setIsPublic(isPublic);
                }
                draft.setUpdatedAt(LocalDateTime.now());
            });
            result = sqlClient.saveCommand(updated)
                    .setMode(SaveMode.UPSERT)
                    .execute()
                    .getModifiedEntity();
        } else {
            // 记录不存在，创建新记录
            log.info("文件记录不存在，创建新记录: {}", fileUrl);
            FileMetadata metadata = FileMetadataDraft.$.produce(draft -> {
                draft.setFilename(originalFilename);
                draft.setOriginalFilename(originalFilename);
                draft.setFileExt(extension.startsWith(".") ? extension.substring(1) : extension);
                draft.setFileSize(file.getSize());
                draft.setPath(filePath);
                draft.setUrl(fileUrl);
                draft.setCategoryId(categoryId);
                draft.setCategoryName(categoryName);
                draft.setDescription(description);
                draft.setIsPublic(isPublic != null ? isPublic : true);
                draft.setFileType(detectFileType(file.getContentType()));
                draft.setCreatedAt(LocalDateTime.now());
                draft.setUpdatedAt(LocalDateTime.now());
            });
            result = sqlClient.saveCommand(metadata)
                    .setMode(SaveMode.INSERT_ONLY)
                    .execute()
                    .getModifiedEntity();
        }

        log.info("文件上传完成: id={}, url={}", result.id(), result.url());
        return result;
    }

    /**
     * 创建文件元数据（用于已存在文件的情况，可指定URL）
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMetadata createFileMetadata(String filePath, String fileName, long fileSize,
                                           Long categoryId, String description, Boolean isPublic, String url) {
        final String extension = FileNameUtil.getSuffix(fileName);

        final String fileUrl = (url != null && !url.isEmpty()) ? url : "/api/files/download/" + filePath;

        final String categoryName;
        if (categoryId != null) {
            FileCategory category = sqlClient.findById(FileCategory.class, categoryId);
            categoryName = category != null ? category.name() : null;
        } else {
            categoryName = null;
        }

        // 检查是否已存在
        List<FileMetadata> existingList = sqlClient.createQuery(FileMetadataTable.$)
                .where(FileMetadataTable.$.url().eq(fileUrl))
                .select(FileMetadataTable.$)
                .execute();

        if (existingList != null && !existingList.isEmpty()) {
            // 已存在，更新业务字段
            FileMetadata existing = existingList.getFirst();
            FileMetadata updated = FileMetadataDraft.$.produce(existing, draft -> {
                // 更新 path 字段（可能之前的记录 path 为空）
                if (filePath != null && !filePath.isEmpty()) {
                    draft.setPath(filePath);
                }
                if (categoryId != null) {
                    draft.setCategoryId(categoryId);
                    draft.setCategoryName(categoryName);
                }
                if (description != null) {
                    draft.setDescription(description);
                }
                if (isPublic != null) {
                    draft.setIsPublic(isPublic);
                }
                draft.setUpdatedAt(LocalDateTime.now());
            });
            return sqlClient.saveCommand(updated)
                    .setMode(SaveMode.UPSERT)
                    .execute()
                    .getModifiedEntity();
        }

        // 不存在，创建新记录
        FileMetadata metadata = FileMetadataDraft.$.produce(draft -> {
            draft.setFilename(fileName);
            draft.setOriginalFilename(fileName);
            draft.setFileExt(extension);
            draft.setFileSize(fileSize);
            draft.setPath(filePath);
            draft.setUrl(fileUrl);
            draft.setCategoryId(categoryId);
            draft.setCategoryName(categoryName);
            draft.setDescription(description);
            draft.setIsPublic(isPublic != null ? isPublic : true);
            draft.setFileType(detectFileTypeByExtension(extension));
            draft.setCreatedAt(LocalDateTime.now());
            draft.setUpdatedAt(LocalDateTime.now());
        });

        return sqlClient.saveCommand(metadata)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新文件元数据（用于分片上传合并后更新文件信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileMetadata(Long id, String filePath, String fileName, long fileSize,
                                   Long categoryId, String description, Boolean isPublic, String url) {
        FileMetadata existing = sqlClient.findById(FileMetadata.class, id);
        if (existing == null) {
            throw new RuntimeException("文件元数据不存在，ID: " + id);
        }

        final String extension;
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1);
        } else {
            extension = "";
        }

        final String fileUrl = (url != null && !url.isEmpty()) ? url : "/api/files/download/" + filePath;

        final String categoryName;
        if (categoryId != null) {
            FileCategory category = sqlClient.findById(FileCategory.class, categoryId);
            categoryName = category != null ? category.name() : null;
        } else {
            categoryName = null;
        }

        FileMetadata updated = FileMetadataDraft.$.produce(existing, draft -> {
            if (filePath != null && !filePath.isEmpty()) {
                draft.setPath(filePath);
            }
            if (!fileName.isEmpty()) {
                draft.setOriginalFilename(fileName);
                draft.setFileExt(extension);
            }
            draft.setFileSize(fileSize);
            draft.setUrl(fileUrl);
            if (categoryId != null) {
                draft.setCategoryId(categoryId);
                draft.setCategoryName(categoryName);
            }
            if (description != null) {
                draft.setDescription(description);
            }
            if (isPublic != null) {
                draft.setIsPublic(isPublic);
            }
            draft.setFileType(detectFileTypeByExtension(extension));
            draft.setUpdatedAt(LocalDateTime.now());
        });

        sqlClient.saveCommand(updated)
                .setMode(SaveMode.UPSERT)
                .execute();
    }

    /**
     * 根据ID获取文件元数据
     */
    public FileMetadata getFileById(long id) {
        return sqlClient.findById(FileMetadata.class, id);
    }

    /**
     * 删除文件
     * <p>
     * 先删除存储文件，然后删除数据库记录。
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(long id) {
        FileMetadata metadata = sqlClient.findById(FileMetadata.class, id);
        if (metadata == null) {
            return;
        }
        if (metadata.url() != null) {
            log.info("删除文件: id={}, path={}, url={}", id, metadata.path(), metadata.url());
            try {
                log.info("尝试通过 url 删除存储文件: {}", metadata.url());
                FileInfo fileInfo = DatabaseFileRecorder.createFileInfo(metadata);
                boolean deleted = xFileStorageService.delete(fileInfo);

                log.info("数据库记录删除: {}", deleted);
            } catch (Exception e) {
                log.error("删除文件失败: id={}", id, e);
                throw e;
            }
        } else {
            log.info("删除文件: id={}, path={}", id, metadata.path());
            sqlClient.deleteById(FileMetadata.class, id);
        }
    }

    /**
     * 更新文件元数据
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMetadata updateFile(long id, FileMetadataForm form) {
        FileMetadata existing = sqlClient.findById(FileMetadata.class, id);
        if (existing == null) {
            return null;
        }

        FileMetadata updated = FileMetadataDraft.$.produce(existing, draft -> {
            draft.setOriginalFilename(form.getOriginalFilename());
            if (form.getCategoryId() != null) {
                draft.setCategoryId(form.getCategoryId());
                FileCategory category = sqlClient.findById(FileCategory.class, form.getCategoryId());
                if (category != null) {
                    draft.setCategoryName(category.name());
                }
            }
            if (form.getDescription() != null) {
                draft.setDescription(form.getDescription());
            }
            if (form.getIsPublic() != null) {
                draft.setIsPublic(form.getIsPublic());
            }
            draft.setUpdatedAt(LocalDateTime.now());
        });

        return sqlClient.saveCommand(updated)
                .setMode(SaveMode.UPSERT)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 获取文件分类列表
     */
    public List<FileCategory> getCategories(FileCategorySpecification sc) {
        return sqlClient.createQuery(FileCategoryTable.$)
                .where(sc)
                .orderBy(FileCategoryTable.$.sortOrder())
                .select(FileCategoryTable.$)
                .execute();
    }

    /**
     * 创建文件分类
     */
    @Transactional(rollbackFor = Exception.class)
    public FileCategory createCategory(FileCategoryForm fileCategoryForm) {
        FileCategory category = FileCategoryDraft.$.produce(fileCategoryForm.toEntity(), draft -> {
            draft.setCreatedAt(LocalDateTime.now());
            draft.setUpdatedAt(LocalDateTime.now());
        });

        return sqlClient.saveCommand(category)
                .setMode(SaveMode.INSERT_ONLY)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 更新文件分类
     */
    @Transactional(rollbackFor = Exception.class)
    public FileCategory updateCategory(Long id, FileCategoryForm fileCategoryForm) {
        FileCategory existing = sqlClient.findById(FileCategory.class, id);
        if (existing == null) {
            return null;
        }

        FileCategory updated = FileCategoryDraft.$.produce(existing, draft -> {
            draft.setName(fileCategoryForm.getName());
            draft.setCode(fileCategoryForm.getCode());
            if (fileCategoryForm.getDescription() != null) {
                draft.setDescription(fileCategoryForm.getDescription());
            }
            if (fileCategoryForm.getIcon() != null) {
                draft.setIcon(fileCategoryForm.getIcon());
            }
            if (fileCategoryForm.getSortOrder() != null) {
                draft.setSortOrder(fileCategoryForm.getSortOrder());
            }
            draft.setUpdatedAt(LocalDateTime.now());
        });

        return sqlClient.saveCommand(updated)
                .setMode(SaveMode.UPSERT)
                .execute()
                .getModifiedEntity();
    }

    /**
     * 删除文件分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        sqlClient.deleteById(FileCategory.class, id);
    }

    /**
     * 根据 Content-Type 检测文件类型
     */
    private String detectFileType(String contentType) {
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else if (contentType.startsWith("text/") || contentType.contains("document") || contentType.contains("pdf")) {
            return "document";
        } else if (contentType.contains("zip") || contentType.contains("compressed")) {
            return "archive";
        } else {
            return "other";
        }
    }

    /**
     * 根据扩展名检测文件类型
     */
    private String detectFileTypeByExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "unknown";
        }
        String ext = extension.toLowerCase();
        if (List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg").contains(ext)) {
            return "image";
        } else if (List.of("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv").contains(ext)) {
            return "video";
        } else if (List.of("mp3", "wav", "ogg", "flac", "aac").contains(ext)) {
            return "audio";
        } else if (List.of("txt", "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx").contains(ext)) {
            return "document";
        } else if (List.of("zip", "rar", "7z", "tar", "gz").contains(ext)) {
            return "archive";
        } else {
            return "other";
        }
    }

    /**
     * 根据文件ID下载文件
     * <p>
     * 通过文件ID查询元数据，然后从存储服务获取文件内容并写入响应。
     * 由于 x-file-storage 已配置 enable-access: true，大多数情况下
     * 前端直接访问文件URL即可，此方法用于需要权限控制的场景。
     * </p>
     *
     * @param id       文件ID
     * @param response HTTP响应对象
     */
    public void downloadFile(long id, HttpServletResponse response) {
        FileMetadata metadata = getFileById(id);
        if (metadata == null) {
            throw new RuntimeException("文件不存在");
        }

        try {
            response.setContentType("application/octet-stream");
            response.setContentLengthLong(metadata.fileSize());
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    new String(metadata.originalFilename().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"");

            xFileStorageService.download(metadata.path());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
}