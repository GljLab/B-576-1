package com.interview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.common.Result;
import com.interview.entity.InterviewProject;
import com.interview.service.InterviewProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 面试项目控制器
 */
@Tag(name = "面试项目管理", description = "项目增删改查接口")
@RestController
@RequestMapping("/projects")
public class ProjectController {
    
    @Autowired
    private InterviewProjectService projectService;
    
    @Operation(summary = "分页查询项目")
    @GetMapping
    public Result<Page<InterviewProject>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<InterviewProject> page = projectService.pageProjects(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }
    
    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = projectService.getProjectDetail(id);
        if (detail == null) {
            return Result.notFound();
        }
        return Result.success(detail);
    }
    
    @Operation(summary = "创建项目")
    @PostMapping
    public Result<InterviewProject> create(@Valid @RequestBody InterviewProject project) {
        if (projectService.save(project)) {
            return Result.success("创建成功", project);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<InterviewProject> update(@PathVariable Long id, @Valid @RequestBody InterviewProject project) {
        project.setId(id);
        if (projectService.updateById(project)) {
            return Result.success("更新成功", project);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (projectService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @Operation(summary = "更新项目状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("状态不能为空");
        }
        if (projectService.updateStatus(id, status)) {
            return Result.success("状态更新成功", null);
        }
        return Result.error("状态更新失败");
    }
    
    @Operation(summary = "获取项目统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getStatistics(@PathVariable Long id) {
        Map<String, Object> stats = projectService.getProjectStatistics(id);
        return Result.success(stats);
    }
}
