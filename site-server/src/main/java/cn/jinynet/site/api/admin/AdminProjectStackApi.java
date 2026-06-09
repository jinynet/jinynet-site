package cn.jinynet.site.api.admin;

import cn.jinynet.starter.common.types.result.Result;
import cn.jinynet.site.service.ProjectService;
import cn.jinynet.site.entity.ProjectStack;
import cn.jinynet.site.entity.dto.ProjectStackForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目技术栈管理接口
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/project-stacks")
@RequiredArgsConstructor
public class AdminProjectStackApi {

    private final ProjectService projectService;

    /**
     * 获取所有技术栈列表
     *
     * @return 技术栈列表
     */
    @GetMapping
    public Result<List<ProjectStack>> getProjectStacks() {
        return Result.success(projectService.getAllProjectStacks());
    }

    /**
     * 创建技术栈
     *
     * @param projectStackForm 技术栈数据
     * @return 创建成功的技术栈
     */
    @PostMapping
    public Result<ProjectStack> createProjectStack(@RequestBody ProjectStackForm projectStackForm) {
        return Result.success(projectService.createProjectStack(projectStackForm));
    }

    /**
     * 更新技术栈
     *
     * @param id               技术栈ID
     * @param projectStackForm 技术栈数据
     * @return 更新成功的技术栈
     */
    @PutMapping("/{id}")
    public Result<ProjectStack> updateProjectStack(@PathVariable long id, @RequestBody ProjectStackForm projectStackForm) {
        return Result.success(projectService.updateProjectStack(id, projectStackForm));
    }

    /**
     * 删除技术栈
     *
     * @param id 技术栈ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProjectStack(@PathVariable long id) {
        return Result.success(projectService.deleteProjectStack(id));
    }
}
