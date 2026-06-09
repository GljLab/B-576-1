package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.ScoreItem;
import com.interview.service.ScoreItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "评分项管理", description = "评分项配置、排序、模板套用等接口")
@RestController
@RequestMapping("/score-items")
public class ScoreItemController {
    
    @Autowired
    private ScoreItemService scoreItemService;
    
    @Operation(summary = "获取项目的评分项列表")
    @GetMapping("/project/{projectId}")
    public Result<List<ScoreItem>> getByProjectId(@PathVariable Long projectId) {
        return Result.success(scoreItemService.getByProjectId(projectId));
    }
    
    @Operation(summary = "获取评分项详情")
    @GetMapping("/{id}")
    public Result<ScoreItem> getById(@PathVariable Long id) {
        ScoreItem item = scoreItemService.getById(id);
        if (item == null) {
            return Result.notFound();
        }
        return Result.success(item);
    }
    
    @Operation(summary = "新增评分项")
    @PostMapping
    public Result<ScoreItem> create(@Valid @RequestBody ScoreItem scoreItem) {
        if (scoreItemService.save(scoreItem)) {
            return Result.success("新增成功", scoreItem);
        }
        return Result.error("新增失败");
    }
    
    @Operation(summary = "更新评分项")
    @PutMapping("/{id}")
    public Result<ScoreItem> update(@PathVariable Long id, @Valid @RequestBody ScoreItem scoreItem) {
        scoreItem.setId(id);
        if (scoreItemService.update(scoreItem)) {
            return Result.success("更新成功", scoreItem);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除评分项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (scoreItemService.delete(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @Operation(summary = "批量保存评分项")
    @PostMapping("/batch")
    public Result<Void> batchSave(@RequestBody List<ScoreItem> items) {
        if (scoreItemService.batchSave(items)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @Operation(summary = "验证权重总和")
    @GetMapping("/validate-weights/{projectId}")
    public Result<Map<String, Object>> validateWeights(@PathVariable Long projectId) {
        return Result.success(scoreItemService.validateWeights(projectId));
    }
    
    @Operation(summary = "更新排序顺序")
    @PostMapping("/sort/{projectId}")
    public Result<Void> updateSortOrder(@PathVariable Long projectId, @RequestBody List<Long> itemIds) {
        if (scoreItemService.updateSortOrder(projectId, itemIds)) {
            return Result.success("排序更新成功", null);
        }
        return Result.error("排序更新失败");
    }
    
    @Operation(summary = "套用评分模板")
    @PostMapping("/apply-template/{projectId}")
    public Result<Void> applyTemplate(
            @PathVariable Long projectId,
            @RequestParam Long templateId,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        if (scoreItemService.applyTemplate(projectId, templateId, overwrite)) {
            return Result.success("模板套用成功", null);
        }
        return Result.error("模板套用失败");
    }
    
    @Operation(summary = "预览评分体系")
    @GetMapping("/preview/{projectId}")
    public Result<Map<String, Object>> preview(@PathVariable Long projectId) {
        return Result.success(scoreItemService.preview(projectId));
    }
    
    @Operation(summary = "获取评分项统计数据")
    @GetMapping("/statistics/{projectId}")
    public Result<List<Map<String, Object>>> getItemStatistics(@PathVariable Long projectId) {
        return Result.success(scoreItemService.getItemStatistics(projectId));
    }
    
    @Operation(summary = "获取考生评分明细")
    @GetMapping("/candidate/{candidateId}")
    public Result<Map<String, Object>> getCandidateItemScores(
            @PathVariable Long candidateId,
            @RequestParam Long projectId) {
        return Result.success(scoreItemService.getCandidateItemScores(candidateId, projectId));
    }
}
