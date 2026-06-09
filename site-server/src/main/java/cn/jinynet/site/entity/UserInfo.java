package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Table;
import org.babyfish.jimmer.sql.Transient;
import org.jetbrains.annotations.Nullable;

/**
 * 个人基本信息表
 */
@Entity
@Table(
        name = "user_info"
)
public interface UserInfo extends EntityId, BaseEntity {
    /**
     * 姓名
     */
    @Column(
            name = "name"
    )
    String name();

    /**
     * 昵称/笔名
     */
    @Column(
            name = "nickname"
    )
    @Nullable
    String nickname();

    /**
     * 头像路径
     */
    @Column(
            name = "avatar"
    )
    @Nullable
    String avatar();

    /**
     * 职位/头衔
     */
    @Column(
            name = "title"
    )
    @Nullable
    String title();

    /**
     * 邮箱地址
     */
    @Column(
            name = "email"
    )
    @Nullable
    String email();

    /**
     * 手机号码
     */
    @Column(
            name = "phone"
    )
    @Nullable
    String phone();

    /**
     * 所在城市
     */
    @Column(
            name = "location"
    )
    @Nullable
    String location();

    /**
     * 个人简介
     */
    @Column(
            name = "summary"
    )
    @Nullable
    String summary();

    /**
     * 详细介绍
     */
    @Column(
            name = "bio"
    )
    @Nullable
    String bio();

    /**
     * 是否在线（瞬态字段，不存储到数据库）
     */
    @Transient
    @Nullable
    Boolean online();
}
