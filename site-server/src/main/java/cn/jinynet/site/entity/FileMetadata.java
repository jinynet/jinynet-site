package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;


import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 文件元数据实体
 * <p>
 * 遵循 x-file-storage 推荐的数据库表结构，用于存储文件上传后的元数据信息。
 * 与 FileInfo 接口字段一一对应，方便 x-file-storage 进行文件管理。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Entity
@Table(name = "file_metadata")
public interface FileMetadata extends EntityId, BaseEntity {

    /**
     * 获取文件访问URL
     * <p>
     * 完整的可访问URL，用于快速定位文件。
     * </p>
     *
     * @return 文件访问URL
     */
    @Key(group = "uk_file_url")
    @Column(name = "url")
    @Nullable
    String url();

    /**
     * 获取文件在存储平台上的相对路径
     *
     * @return 文件路径
     */
    @Column(name = "path")
    @Nullable
    String path();

    /**
     * 获取保存的文件名
     * <p>
     * 存储在存储平台上的实际文件名（不含路径）。
     * </p>
     *
     * @return 保存的文件名
     */
    @Column(name = "filename")
    @Nullable
    String filename();

    /**
     * 获取原始文件名
     * <p>
     * 用户上传时的原始文件名。
     * </p>
     *
     * @return 原始文件名
     */
    @Column(name = "original_filename")
    @Nullable
    String originalFilename();

    /**
     * 获取文件扩展名
     * <p>
     * 不包含点号，如 "jpg"、"png"、"pdf" 等。
     * </p>
     *
     * @return 文件扩展名
     */
    @Column(name = "file_ext")
    @Nullable
    String fileExt();

    /**
     * 获取文件大小（字节）
     *
     * @return 文件大小
     */
    @Column(name = "file_size")
    @Nullable
    Long fileSize();

    /**
     * 获取文件的MIME类型
     * <p>
     * 如 "image/jpeg"、"application/pdf" 等。
     * </p>
     *
     * @return MIME类型
     */
    @Column(name = "content_type")
    @Nullable
    String contentType();

    /**
     * 获取存储平台标识
     * <p>
     * 对应 x-file-storage 中配置的 platform，如 "local-1"、"aliyun-oss-1" 等。
     * </p>
     *
     * @return 存储平台标识
     */
    @Column(name = "platform")
    @Nullable
    String platform();

    /**
     * 获取基础路径
     * <p>
     * 文件存储的基础路径前缀。
     * </p>
     *
     * @return 基础路径
     */
    @Column(name = "base_path")
    @Nullable
    String basePath();

    /**
     * 获取文件元数据（JSON）
     * <p>
     * 存储 x-file-storage 的 metadata 信息。
     * </p>
     *
     * @return 元数据JSON
     */
    @Column(name = "metadata")
    @Nullable
    String metadata();

    /**
     * 获取用户元数据（JSON）
     * <p>
     * 存储 x-file-storage 的 userMetadata 信息。
     * </p>
     *
     * @return 用户元数据JSON
     */
    @Column(name = "user_metadata")
    @Nullable
    String userMetadata();

    /**
     * 获取附加属性（JSON）
     * <p>
     * 存储上传时设置的额外属性信息。
     * </p>
     *
     * @return 附加属性JSON
     */
    @Column(name = "attr")
    @Nullable
    String attr();

    /**
     * 获取哈希信息（JSON）
     * <p>
     * 存储文件的 MD5/SHA 等哈希值，用于完整性校验。
     * </p>
     *
     * @return 哈希信息JSON
     */
    @Column(name = "hash_info")
    @Nullable
    String hashInfo();

    /**
     * 获取关联对象ID
     * <p>
     * 用于将文件与业务对象关联，如用户ID、文章ID等。
     * </p>
     *
     * @return 关联对象ID
     */
    @Column(name = "object_id")
    @Nullable
    String objectId();

    /**
     * 获取关联对象类型
     * <p>
     * 用于标识关联对象的类型，如 "user"、"article" 等。
     * </p>
     *
     * @return 关联对象类型
     */
    @Column(name = "object_type")
    @Nullable
    String objectType();

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    @Column(name = "category_id")
    @Nullable
    Long categoryId();

    /**
     * 获取分类名称
     *
     * @return 分类名称
     */
    @Column(name = "category_name")
    @Nullable
    String categoryName();

    /**
     * 获取文件描述
     *
     * @return 文件描述
     */
    @Column(name = "description")
    @Nullable
    String description();

    /**
     * 判断文件是否公开
     *
     * @return 是否公开
     */
    @Column(name = "is_public")
    Boolean isPublic();

    /**
     * 获取文件类型
     * <p>
     * 可选值：image/video/audio/document/archive/other
     * </p>
     *
     * @return 文件类型
     */
    @Column(name = "file_type")
    String fileType();

    /**
     * 视频标题（视频文件专用）
     */
    @Column(name = "title")
    @Nullable
    String title();

    /**
     * 视频别名（URL友好，视频文件专用）
     */
    @Key(group = "uk_file_slug")
    @Column(name = "slug")
    @Nullable
    String slug();

    /**
     * 视频封面URL（视频文件专用）
     */
    @Column(name = "cover_url")
    @Nullable
    String coverUrl();

    /**
     * HLS播放地址（视频文件专用）
     */
    @Column(name = "hls_url")
    @Nullable
    String hlsUrl();

    /**
     * 源文件播放地址（视频文件专用，非HLS格式）
     */
    @Column(name = "source_url")
    @Nullable
    String sourceUrl();

    /**
     * 视频时长（秒，视频文件专用）
     */
    @Column(name = "duration")
    @Nullable
    Integer duration();

    /**
     * 视频宽度（视频文件专用）
     */
    @Column(name = "width")
    @Nullable
    Integer width();

    /**
     * 视频高度（视频文件专用）
     */
    @Column(name = "height")
    @Nullable
    Integer height();

    /**
     * 原始文件格式：mp4/webm/av1（视频文件专用）
     */
    @Column(name = "file_format")
    @Nullable
    String fileFormat();

    /**
     * 视频类型（视频文件专用）
     */
    @Column(name = "video_type")
    @Nullable
    String videoType();

    /**
     * 标签（逗号分隔，视频文件专用）
     */
    @Column(name = "tags")
    @Nullable
    String tags();

    /**
     * 播放量（视频文件专用）
     */
    @Column(name = "view_count")
    int viewCount();

    /**
     * 点赞数（视频文件专用）
     */
    @Column(name = "like_count")
    int likeCount();

    /**
     * 投币数（视频文件专用）
     */
    @Column(name = "coin_count")
    int coinCount();

    /**
     * 收藏数（视频文件专用）
     */
    @Column(name = "favorite_count")
    int favoriteCount();

    /**
     * 视频状态（视频文件专用）
     */
    @Column(name = "video_status")
    @Nullable
    String videoStatus();

    /**
     * 发布时间（视频文件专用）
     */
    @Column(name = "published_at")
    @Nullable
    LocalDateTime publishedAt();

    /**
     * 排序权重（视频文件专用）
     */
    @Column(name = "sort_weight")
    int sortWeight();

    /**
     * 原始文件ID（转码后关联原始文件，视频文件专用）
     */
    @Column(name = "origin_file_id")
    @Nullable
    Long originFileId();

    /**
     * 转码进度（0-100，视频文件专用）
     */
    @Column(name = "transcoding_progress")
    @Nullable
    Integer transcodingProgress();

    /**
     * 错误信息（视频文件专用）
     */
    @Column(name = "error_message")
    @Nullable
    String errorMessage();
}