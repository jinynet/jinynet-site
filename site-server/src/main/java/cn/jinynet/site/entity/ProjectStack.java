package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 项目技术栈表
 */
@Entity
@Table(
        name = "project_stack"
)
public interface ProjectStack extends EntityId, BaseEntity {

    /**
     * 技术栈名称
     */
    @Column(
            name = "name"
    )
    @Key(group = "uk_project_stack_name")
    String name();

    /**
     * 技术分类：language/framework/database/tools
     */
    @Column(
            name = "category"
    )
    String category();

    /**
     * 图标名称
     */
    @Column(
            name = "icon"
    )
    @Nullable
    String icon();

    /**
     * 显示颜色
     */
    @Column(
            name = "color"
    )
    @Nullable
    String color();

    /**
     * 技术描述
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
