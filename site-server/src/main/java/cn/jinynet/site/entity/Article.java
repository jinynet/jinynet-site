package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章表
 */
@Entity
@Table(
        name = "article"
)
public interface Article extends EntityId, BaseEntity {
    /**
     * 文章标题
     */
    @Column(
            name = "title"
    )
    String title();

    /**
     * 文章别名（URL友好）
     */
    @Key(group = "uk_article_slug")
    @Column(
            name = "slug"
    )
    String slug();

    /**
     * 文章内容（Markdown格式）
     */
    @Column(
            name = "content"
    )
    @Nullable
    String content();

    /**
     * 文章摘要
     */
    @Column(
            name = "excerpt"
    )
    @Nullable
    String excerpt();

    /**
     * 封面图片路径
     */
    @Column(
            name = "cover_image"
    )
    @Nullable
    String coverImage();

    /**
     * 状态：draft/published/private
     */
    @Column(
            name = "status"
    )
    String status();

    /**
     * 阅读量
     */
    @Column(
            name = "view_count"
    )
    int viewCount();

    /**
     * 点赞数
     */
    @Column(
            name = "like_count"
    )
    int likeCount();

    /**
     * 发布时间
     */
    @Column(
            name = "published_at"
    )
    @Nullable
    LocalDateTime publishedAt();

    /**
     * 分类
     */
    @Nullable
    @ManyToOne
    ArticleCategory category();

    @IdView
    @Nullable
    Long categoryId();

    /**
     * 标签
     */
    @ManyToMany
    @JoinTable(
            name = "article_tag_mapping",
            joinColumnName = "article_id",
            inverseJoinColumnName = "tag_id"
    )
    List<ArticleTag> tags();
}
