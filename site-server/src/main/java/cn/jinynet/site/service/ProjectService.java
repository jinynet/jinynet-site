package cn.jinynet.site.service;

import cn.jinynet.site.entity.*;
import cn.jinynet.site.entity.dto.ProjectDetail;
import cn.jinynet.site.entity.dto.ProjectForm;
import cn.jinynet.site.entity.dto.ProjectStackForm;
import lombok.RequiredArgsConstructor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.DeleteResult;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目服务
 *
 * @author jinty
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final JSqlClient sqlClient;
    private final ProjectSearchService projectSearchService;

    /**
     * 创建项目
     *
     * @param projectForm 项目表单
     * @return 项目详情
     */
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(ProjectForm projectForm) {
        Project newProject = ProjectDraft.$.produce(projectForm.toEntity(), draft -> {

        });
        Project savedProject = sqlClient.saveCommand(newProject)
                .setMode(SaveMode.INSERT_ONLY)
                .execute(ProjectDetail.METADATA.getFetcher())
                .getModifiedEntity();
        
        projectSearchService.indexProject(savedProject);
        return savedProject;
    }

    /**
     * 更新项目
     *
     * @param id          项目ID
     * @param projectForm 项目表单
     * @return 项目详情
     */
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(long id, ProjectForm projectForm) {
        Project project = ProjectDraft.$.produce(projectForm.toEntityById(id), draft -> {
        });
        Project updatedProject = sqlClient.saveCommand(project).setMode(SaveMode.UPDATE_ONLY).execute(ProjectDetail.METADATA.getFetcher()).getModifiedEntity();
        
        projectSearchService.indexProject(updatedProject);
        return updatedProject;
    }

    /**
     * 删除项目
     *
     * @param id 项目ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(Project.class, id);
        boolean deleted = deleteResult.getAffectedRowCount(Project.class) > 0;
        if (deleted) {
            projectSearchService.deleteProjectFromIndex(id);
        }
        return deleted;
    }

    /**
     * 创建技术栈
     *
     * @param projectStackForm 技术栈数据
     * @return 创建的技术栈
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectStack createProjectStack(ProjectStackForm projectStackForm) {
        ProjectStack stack = ProjectStackDraft.$.produce(projectStackForm.toEntity(), draft -> {
        });
        return sqlClient.saveCommand(stack).setMode(SaveMode.INSERT_ONLY).execute().getModifiedEntity();
    }

    /**
     * 更新技术栈
     *
     * @param id               技术栈ID
     * @param projectStackForm 技术栈数据
     * @return 更新后的技术栈
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectStack updateProjectStack(long id, ProjectStackForm projectStackForm) {
        ProjectStack stack = ProjectStackDraft.$.produce(projectStackForm.toEntityById(id), draft -> {

        });
        return sqlClient.saveCommand(stack).setMode(SaveMode.UPDATE_ONLY).execute().getModifiedEntity();
    }

    /**
     * 删除技术栈
     *
     * @param id 技术栈ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProjectStack(long id) {
        DeleteResult deleteResult = sqlClient.deleteById(ProjectStack.class, id);
        return deleteResult.getAffectedRowCount(ProjectStack.class) > 0;
    }

    /**
     * 获取所有技术栈
     *
     * @return 技术栈列表
     */
    public List<ProjectStack> getAllProjectStacks() {
        List<Order> orders = Order.makeOrders(ProjectStackTable.$, "sortOrder asc, createdAt desc");
        return sqlClient.createQuery(ProjectStackTable.$).orderBy(orders).select(ProjectStackTable.$).execute();
    }
}