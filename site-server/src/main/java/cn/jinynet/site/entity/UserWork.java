package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.Formula;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/**
 * 工作经验表
 */
@Entity
@Table(
        name = "user_work"
)
public interface UserWork extends EntityId, BaseEntity {

    /**
     * 公司名称
     */
    @Column(
            name = "company_name"
    )
    String companyName();

    /**
     * 公司名称脱敏
     */
    @Formula(dependencies = {"companyName"})
    default String companyNameShow() {
        return "******";
    }

    /**
     * 职位
     */
    @Column(
            name = "position"
    )
    String position();

    /**
     * 开始日期
     */
    @Column(
            name = "start_date"
    )
    LocalDate startDate();

    /**
     * 结束日期（NULL表示当前在职）
     */
    @Column(
            name = "end_date"
    )
    @Nullable
    LocalDate endDate();

    /**
     * 工作描述
     */
    @Column(
            name = "description"
    )
    @Nullable
    String description();

    /**
     * 主要成就
     */
    @Column(
            name = "achievements"
    )
    @Nullable
    String achievements();

    /**
     * 排序顺序
     */
    @Column(
            name = "sort_order"
    )
    int sortOrder();
}
