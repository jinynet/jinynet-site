package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 文章分类表
 */
@Entity
@Table(
        name = "article_category"
)
public interface ArticleCategory extends EntityId, BaseEntity {

    /**
     * 分类名称
     */
    @Column(
            name = "name"
    )
    String name();

    /**
     * 分类别名
     */
    @Key(
            group = "uk_article_category_slug"
    )
    @Column(
            name = "slug"
    )
    String slug();

    /**
     * 分类描述
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
