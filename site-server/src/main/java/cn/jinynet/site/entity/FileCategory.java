package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * 文件分类实体
 * <p>
 * 用于对文件进行分类管理，支持按分类检索和管理文件。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Entity
@Table(name = "file_category")
public interface FileCategory extends EntityId, BaseEntity {

    /**
     * 获取分类名称
     *
     * @return 分类名称
     */
    @Column(name = "name")
    String name();

    /**
     * 获取分类编码
     *
     * @return 分类编码
     */
    @Key(group = "uk_file_category_code")
    @Column(name = "code")
    String code();

    /**
     * 获取分类描述
     *
     * @return 分类描述
     */
    @Column(name = "description")
    @Nullable
    String description();

    /**
     * 获取分类图标
     *
     * @return 分类图标
     */
    @Column(name = "icon")
    @Nullable
    String icon();

    /**
     * 获取排序顺序
     *
     * @return 排序顺序
     */
    @Column(name = "sort_order")
    Integer sortOrder();
}
