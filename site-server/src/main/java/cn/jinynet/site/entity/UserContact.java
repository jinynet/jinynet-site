package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 联系方式表
 */
@Entity
@Table(
        name = "user_contact"
)
public interface UserContact extends EntityId, BaseEntity {

    /**
     * 联系方式类型：email/phone/github/linkedin/wechat/website/other
     */
    @Column(
            name = "contact_type"
    )
    String contactType();

    /**
     * 联系方式值
     */
    @Column(
            name = "contact_value"
    )
    String contactValue();

    /**
     * 显示名称
     */
    @Column(
            name = "display_name"
    )
    @Nullable
    String displayName();

    /**
     * 图标名称
     */
    @Column(
            name = "icon"
    )
    @Nullable
    String icon();

    /**
     * 排序顺序
     */
    @Column(
            name = "sort_order"
    )
    int sortOrder();

}
