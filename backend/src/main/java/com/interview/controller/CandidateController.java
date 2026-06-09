package com.interview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.common.Result;
import com.interview.entity.Candidate;
import com.interview.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 考生控制器
 */
@Tag(name = "考生管理", description = "考生增删改查、签到等接口")
@RestController
@RequestMapping("/candidates")
public class CandidateController {
    
    @Autowired
    private CandidateService candidateService;
    
    @Operation(summary = "分页查询考生")
    @GetMapping
    public Result<Page<Candidate>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String keyword) {
        Page<Candidate> page = candidateService.pageCandidates(pageNum, pageSize, projectId, positionId, keyword);
        return Result.success(page);
    }
    
    @Operation(summary = "获取考生详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = candidateService.getCandidateDetail(id);
        if (detail == null) {
            return Result.notFound();
        }
        return Result.success(detail);
    }
    
    @Operation(summary = "创建考生")
    @PostMapping
    public Result<Candidate> create(@Valid @RequestBody Candidate candidate) {
        candidate.setStatus(1);
        candidate.setCheckInStatus(0);
        candidate.setInterviewStatus(0);
        if (candidateService.save(candidate)) {
            return Result.success("创建成功", candidate);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新考生")
    @PutMapping("/{id}")
    public Result<Candidate> update(@PathVariable Long id, @Valid @RequestBody Candidate candidate) {
        candidate.setId(id);
        if (candidateService.updateById(candidate)) {
            return Result.success("更新成功", candidate);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除考生")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (candidateService.removeById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @Operation(summary = "考生签到")
    @PostMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id) {
        if (candidateService.checkIn(id)) {
            return Result.success("签到成功", null);
        }
        return Result.error("签到失败");
    }
    
    @Operation(summary = "批量导入考生")
    @PostMapping("/batch-import")
    public Result<Integer> batchImport(@RequestParam Long projectId, @RequestBody List<Candidate> candidates) {
        int count = candidateService.batchImport(projectId, candidates);
        return Result.success("成功导入" + count + "条记录", count);
    }
    
    @Operation(summary = "更新面试状态")
    @PutMapping("/{id}/interview-status")
    public Result<Void> updateInterviewStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("状态不能为空");
        }
        if (candidateService.updateInterviewStatus(id, status)) {
            return Result.success("状态更新成功", null);
        }
        return Result.error("状态更新失败");
    }
    
    @Operation(summary = "获取待面试考生列表")
    @GetMapping("/waiting")
    public Result<List<Candidate>> getWaitingCandidates(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long roomId) {
        List<Candidate> candidates = candidateService.getWaitingCandidates(projectId, roomId);
        return Result.success(candidates);
    }
}
