package cn.jinynet.site.api.admin;

import cn.jinynet.starter.common.types.exception.BaseBizException;
import cn.jinynet.starter.common.types.request.PageRequest;
import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.ProjectService;
import cn.jinynet.site.entity.Project;
import cn.jinynet.site.entity.ProjectTable;
import cn.jinynet.site.entity.dto.ProjectDetail;
import cn.jinynet.site.entity.dto.ProjectForm;
import cn.jinynet.site.entity.dto.ProjectList;
import cn.jinynet.site.entity.dto.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectApi {

    private final JSqlClient sqlClient;
    private final ProjectService projectService;

    /**
     * 获取项目列表
     *
     * @param pq 分页参数
     * @return 项目列表
     */
    @GetMapping
    public Result<Page<Project>> getProjects(ProjectSpecification sc, PageRequest pq) {
        List<Order> orders = pq.order()
                ? Order.makeOrders(ProjectTable.$, pq.getOrderBy())
                : Order.makeOrders(ProjectTable.$, "sortOrder asc, createdAt desc");

        Page<Project> projectListPage = sqlClient.createQuery(ProjectTable.$)
                .where(sc)
                .orderBy(orders)
                .select(ProjectTable.$.fetch(ProjectList.METADATA.getFetcher()))
                .fetchPage(pq.getPageIndex(), pq.getPageSize());
        return Result.success(projectListPage);
    }

    /**
     * 获取项目详情
     *
     * @param id 项目ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable long id) {
        Project projectDetail = sqlClient.createQuery(ProjectTable.$)
                .where(ProjectTable.$.id().eq(id))
                .select(ProjectTable.$.fetch(ProjectDetail.METADATA.getFetcher()))
                .fetchOptional()
                .orElseThrow(() -> new BaseBizException("项目不存在"));
        return Result.success(projectDetail);
    }

    /**
     * 创建项目
     *
     * @param projectForm 项目表单
     * @return 创建成功的项目
     */
    @PostMapping
    public Result<Project> createProject(@RequestBody ProjectForm projectForm) {
        return Result.success(projectService.createProject(projectForm));
    }

    /**
     * 更新项目
     *
     * @param id          项目ID
     * @param projectForm 项目表单
     * @return 更新的项目
     */
    @PutMapping("/{id}")
    public Result<Project> updateProject(@PathVariable long id, @RequestBody ProjectForm projectForm) {
        return Result.success(projectService.updateProject(id, projectForm));
    }

    /**
     * 删除项目
     *
     * @param id 项目ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProject(@PathVariable long id) {
        return Result.success(projectService.deleteProject(id));
    }
}