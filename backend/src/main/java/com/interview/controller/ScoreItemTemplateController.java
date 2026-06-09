package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.ScoreItemTemplate;
import com.interview.service.ScoreItemTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "评分模板管理", description = "评分模板增删改查接口")
@RestController
@RequestMapping("/score-templates")
public class ScoreItemTemplateController {
    
    @Autowired
    private ScoreItemTemplateService templateService;
    
    @Operation(summary = "获取模板列表")
    @GetMapping
    public Result<List<ScoreItemTemplate>> list(
            @RequestParam(required = false) Integer isSystem) {
        return Result.success(templateService.list(isSystem));
    }
    
    @Operation(summary = "获取模板详情（含评分项）")
    @GetMapping("/{id}")
    public Result<ScoreItemTemplate> getDetail(@PathVariable Long id) {
        ScoreItemTemplate template = templateService.getDetail(id);
        if (template == null) {
            return Result.notFound();
        }
        return Result.success(template);
    }
    
    @Operation(summary = "创建自定义模板")
    @PostMapping
    public Result<ScoreItemTemplate> create(@Valid @RequestBody ScoreItemTemplate template) {
        if (templateService.save(template)) {
            return Result.success("创建成功", template);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<ScoreItemTemplate> update(
            @PathVariable Long id,
            @Valid @RequestBody ScoreItemTemplate template) {
        template.setId(id);
        if (templateService.update(template)) {
            return Result.success("更新成功", template);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (templateService.delete(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
