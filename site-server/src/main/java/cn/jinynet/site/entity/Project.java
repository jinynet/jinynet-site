package cn.jinynet.site.entity;

import cn.jinynet.starter.jimmer.entity.BaseEntity;
import cn.jinynet.starter.jimmer.entity.EntityId;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目表
 */
@Entity
@Table(
        name = "project"
)
public interface Project extends EntityId, BaseEntity {
    /**
     * 项目名称
     */
    @Column(
            name = "name"
    )
    String name();

    /**
     * 项目别名（URL友好）
     */
    @Column(
            name = "slug"
    )
    String slug();

    /**
     * 项目描述
     */
    @Column(
            name = "description"
    )
    @Nullable
    String description();

    /**
     * 项目详细介绍
     */
    @Column(
            name = "content"
    )
    @Nullable
    String content();

    /**
     * 封面图片路径
     */
    @Column(
            name = "cover_image"
    )
    @Nullable
    String coverImage();

    /**
     * 项目链接
     */
    @Column(
            name = "project_url"
    )
    @Nullable
    String projectUrl();

    /**
     * 代码仓库地址（支持 GitHub、GitLab、Gitee 等）
     */
    @Column(
            name = "repo_url"
    )
    @Nullable
    String repoUrl();

    /**
     * 状态：active/completed/paused
     */
    @Column(
            name = "status"
    )
    String status();

    /**
     * 开始日期
     */
    @Column(
            name = "start_date"
    )
    @Nullable
    LocalDate startDate();

    /**
     * 结束日期（NULL表示进行中）
     */
    @Column(
            name = "end_date"
    )
    @Nullable
    LocalDate endDate();

    /**
     * 项目角色
     */
    @Column(
            name = "role"
    )
    @Nullable
    String role();

    /**
     * 项目贡献描述
     */
    @Column(
            name = "contribution"
    )
    @Nullable
    String contribution();

    /**
     * 排序顺序
     */
    @Column(
            name = "sort_order"
    )
    int sortOrder();

    /**
     * 是否公开（true=公开，false=私有）
     */
    @Column(
            name = "published"
    )
    boolean published();

    /**
     * 项目使用的技术栈
     */
    @ManyToMany
    @JoinTable(
            name = "project_stack_mapping",
            joinColumnName = "project_id",
            inverseJoinColumnName = "stack_id"
    )
    List<ProjectStack> stacks();
}
