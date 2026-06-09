package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 技能表
 */
@Entity
@Table(
        name = "user_skills"
)
public interface UserSkills extends EntityId, BaseEntity {

    /**
     * 技能名称
     */
    @Column(
            name = "name"
    )
    String name();

    /**
     * 技能分类：frontend/backend/database/tools/other
     */
    @Column(
            name = "category"
    )
    String category();

    /**
     * 技能等级（1-5）
     */
    @Column(
            name = "level"
    )
    int level();

    /**
     * 技能描述
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
