package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 文章标签表
 */
@Entity
@Table(
        name = "article_tag"
)
public interface ArticleTag extends EntityId, BaseEntity {

    /**
     * 标签名称
     */
    @Column(
            name = "name"
    )
    String name();

    /**
     * 标签别名
     */
    @Key(group = "uk_article_tag_slug")
    @Column(
            name = "slug"
    )
    String slug();

    /**
     * 标签颜色
     */
    @Column(
            name = "color"
    )
    @Nullable
    String color();

    /**
     * 标签描述
     */
    @Column(
            name = "description"
    )
    @Nullable
    String description();

    /**
     * 排序顺序
     */
    @Column(
            name = "sort_order"
    )
    int sortOrder();
}
