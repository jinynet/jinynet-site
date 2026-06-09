package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 系统设置表
 */
@Entity
@Table(
        name = "system_settings"
)
public interface SystemSettings extends EntityId, BaseEntity {

    /**
     * 设置键名，用于标识配置项
     */
    @Key
    @Column(
            name = "setting_key"
    )
    String settingKey();

    /**
     * 设置值，存储配置内容
     */
    @Column(
            name = "setting_value"
    )
    @Nullable
    String settingValue();

    /**
     * 设置类型：string/text/number/boolean/json
     */
    @Column(
            name = "setting_type"
    )
    String settingType();

    /**
     * 设置项描述说明
     */
    @Column(
            name = "description"
    )
    @Nullable
    String description();

    /**
     * 配置分类：site/seo/security/other
     */
    @Column(
            name = "category"
    )
    String category();
}
