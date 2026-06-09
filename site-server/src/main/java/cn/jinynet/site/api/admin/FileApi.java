package cn.jinynet.site.api.admin;

import cn.jinynet.site.service.FileService;
import cn.jinynet.site.entity.FileCategory;
import cn.jinynet.site.entity.FileMetadata;
import cn.jinynet.site.entity.dto.FileCategoryForm;
import cn.jinynet.site.entity.dto.FileCategorySpecification;
import cn.jinynet.site.entity.dto.FileMetadataForm;
import cn.jinynet.site.entity.dto.FileMetadataSpecification;
import cn.jinynet.starter.common.types.request.PageRequest;
import cn.jinynet.starter.common.types.result.Result;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理API控制器
 * <p>
 * 提供文件上传、下载、删除、查询等RESTful接口，支持文件分类管理。
 * 基于 Dromara x-file-storage 实现文件存储，支持本地存储和HTTP直接访问。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileApi {

    /**
     * 文件服务实例，提供文件上传、下载、元数据管理等核心功能
     */
    private final FileService fileService;

    /**
     * 查询文件列表
     * <p>
     * 支持分页、排序、模糊搜索等条件查询，通过 FileMetadataSpecification 对象传递查询参数。
     * </p>
     *
     * @param sc 查询条件对象，包含分页、排序、过滤条件
     * @return 文件列表，包含所有匹配条件的文件元数据
     */
    @GetMapping
    public Result<Page<FileMetadata>> list(FileMetadataSpecification sc, PageRequest page) {
        Page<FileMetadata> files = fileService.getFileList(sc, page);
        return Result.success(files);
    }

    /**
     * 上传文件（单文件上传）
     * <p>
     * 使用 MultipartFile 接收上传的文件，文件会被存储到配置的存储目录中，
     * 同时将文件元数据（文件名、大小、路径、URL等）保存到数据库。
     * </p>
     *
     * @param file        上传的文件（MultipartFile格式）
     * @param categoryId  分类ID（可选），用于归类文件
     * @param description 文件描述（可选），用于说明文件用途
     * @param isPublic    是否公开（默认true），控制文件是否可公开访问
     * @return 上传结果，包含文件元数据（ID、文件名、URL等）
     */
    @PostMapping("/upload")
    public Result<FileMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic) {

        FileMetadata metadata = fileService.upload(file, categoryId, description, isPublic);
        return Result.success(metadata);
    }

    /**
     * 根据ID获取文件详情
     * <p>
     * 通过文件ID从数据库中查询文件的完整元数据信息。
     * </p>
     *
     * @param id 文件ID（主键）
     * @return 文件元数据，如果文件不存在返回错误信息
     */
    @GetMapping("/{id}")
    public Result<FileMetadata> get(@PathVariable long id) {
        FileMetadata metadata = fileService.getFileById(id);
        if (metadata != null) {
            return Result.success(metadata);
        }
        return Result.fail("文件不存在");
    }

    /**
     * 更新文件信息
     * <p>
     * 更新文件的元数据信息（如分类、描述、是否公开等），不支持修改文件内容本身。
     * </p>
     *
     * @param id   文件ID（主键）
     * @param form 文件更新表单，包含需要修改的字段
     * @return 更新后的文件元数据，如果文件不存在返回错误信息
     */
    @PutMapping("/{id}")
    public Result<FileMetadata> update(
            @PathVariable long id,
            @RequestBody FileMetadataForm form) {

        FileMetadata metadata = fileService.updateFile(id, form);
        if (metadata != null) {
            return Result.success("更新成功", metadata);
        }
        return Result.fail("文件不存在");
    }

    /**
     * 删除文件
     * <p>
     * 根据文件ID删除文件，同时删除数据库中的元数据和存储中的实际文件。
     * </p>
     *
     * @param id 文件ID（主键）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        fileService.deleteFile(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除文件
     * <p>
     * 批量删除多个文件，逐个调用删除方法，任一文件删除失败会抛出异常。
     * </p>
     *
     * @param ids 文件ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            fileService.deleteFile(id);
        }
        return Result.success("批量删除成功");
    }

    /// 空行分隔，保持代码可读性

    /**
     * 获取所有文件分类
     * <p>
     * 查询文件分类列表，支持通过 FileCategorySpecification 进行条件过滤。
     * </p>
     *
     * @param sc 查询条件对象
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<FileCategory>> getCategories(FileCategorySpecification sc) {
        List<FileCategory> categories = fileService.getCategories(sc);
        return Result.success(categories);
    }

    /**
     * 创建文件分类
     * <p>
     * 创建新的文件分类，用于对文件进行归类管理。
     * </p>
     *
     * @param fileCategoryForm 文件分类表单，包含分类名称等信息
     * @return 创建的分类信息
     */
    @PostMapping("/categories")
    public Result<FileCategory> createCategory(@RequestBody FileCategoryForm fileCategoryForm) {
        FileCategory category = fileService.createCategory(fileCategoryForm);
        return Result.success("创建成功", category);
    }

    /**
     * 更新文件分类
     * <p>
     * 更新已有分类的信息（如分类名称）。
     * </p>
     *
     * @param id               分类ID（主键）
     * @param fileCategoryForm 文件分类表单，包含需要修改的字段
     * @return 更新后的分类信息，如果分类不存在返回错误信息
     */
    @PutMapping("/categories/{id}")
    public Result<FileCategory> updateCategory(
            @PathVariable long id,
            @RequestBody FileCategoryForm fileCategoryForm) {

        FileCategory category = fileService.updateCategory(id, fileCategoryForm);
        if (category != null) {
            return Result.success("更新成功", category);
        }
        return Result.fail("分类不存在");
    }

    /**
     * 删除文件分类
     * <p>
     * 删除指定的文件分类。注意：如果该分类下有文件，需要先删除文件或修改文件的分类。
     * </p>
     *
     * @param id 分类ID（主键）
     * @return 删除结果
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable long id) {
        fileService.deleteCategory(id);
        return Result.success("删除成功");
    }
}