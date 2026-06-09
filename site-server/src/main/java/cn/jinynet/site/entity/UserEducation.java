package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

/**
 * 教育经历表
 */
@Entity
@Table(
        name = "user_education"
)
public interface UserEducation extends EntityId, BaseEntity {

    /**
     * 学校名称
     */
    @Column(
            name = "school_name"
    )
    String schoolName();

    /**
     * 专业
     */
    @Column(
            name = "major"
    )
    @Nullable
    String major();

    /**
     * 学历：bachelor/master/doctor/other
     */
    @Column(
            name = "degree"
    )
    @Nullable
    String degree();

    /**
     * 开始日期
     */
    @Column(
            name = "start_date"
    )
    LocalDate startDate();

    /**
     * 结束日期（NULL表示在读）
     */
    @Column(
            name = "end_date"
    )
    @Nullable
    LocalDate endDate();

    /**
     * 描述/备注
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
