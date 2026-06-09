package cn.jinynet.site.api.home;

import cn.jinynet.site.service.FullTextSearchService;
import cn.jinynet.site.entity.Project;
import cn.jinynet.site.entity.ProjectStack;
import cn.jinynet.site.entity.ProjectStackTable;
import cn.jinynet.site.entity.ProjectTable;
import cn.jinynet.site.entity.dto.ProjectDetail;
import cn.jinynet.site.entity.dto.ProjectList;
import cn.jinynet.starter.common.types.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 已发布项目接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class PostedProjectApi {

    private final JSqlClient sqlClient;
    private final FullTextSearchService fullTextSearchService;

    /**
     * 获取已发布项目列表（只返回公开的项目）
     *
     * @return 项目列表
     */
    @GetMapping
    public Result<List<Project>> getPostedProjects() {
        List<Project> projects = sqlClient.createQuery(ProjectTable.$)
                .where(ProjectTable.$.published().eq(true))
                .orderBy(ProjectTable.$.sortOrder().asc(), ProjectTable.$.createdAt().desc())
                .select(ProjectTable.$.fetch(ProjectList.METADATA.getFetcher()))
                .execute();
        return Result.success(projects);
    }

    /**
     * 通过 slug 获取项目详情（只返回公开的项目）
     *
     * @param slug 项目别名
     * @return 项目详情
     */
    @GetMapping("/{slug}")
    public Result<Project> getProjectBySlug(@PathVariable String slug) {
        Project project = sqlClient.createQuery(ProjectTable.$)
                .where(ProjectTable.$.slug().eq(slug))
                .where(ProjectTable.$.published().eq(true))
                .select(ProjectTable.$.fetch(ProjectDetail.METADATA.getFetcher()))
                .fetchOptional()
                .orElse(null);
        return Result.success(project);
    }

    /**
     * 获取项目技术栈列表
     *
     * @return 技术栈列表
     */
    @GetMapping("/stacks")
    public Result<List<ProjectStack>> getProjectStacks() {
        List<ProjectStack> stacks = sqlClient.createQuery(ProjectStackTable.$)
                .orderBy(ProjectStackTable.$.sortOrder().asc())
                .select(ProjectStackTable.$)
                .execute();
        return Result.success(stacks);
    }

    /**
     * 全文搜索项目
     *
     * @param keyword 搜索关键词
     * @param limit   返回数量，默认 10
     * @return 搜索结果列表
     */
    @GetMapping("/search")
    public Result<List<FullTextSearchService.UnifiedSearchResult>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<FullTextSearchService.UnifiedSearchResult> results = fullTextSearchService.searchProjects(keyword, limit);
        return Result.success(results);
    }
}
