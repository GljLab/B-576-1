package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.ExaminerScore;
import com.interview.entity.FinalScore;
import com.interview.entity.dto.SubmitMultiScoreDTO;
import com.interview.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "评分管理", description = "考官评分、成绩计算等接口")
@RestController
@RequestMapping("/scores")
public class ScoreController {
    
    @Autowired
    private ScoreService scoreService;
    
    @Operation(summary = "提交单维度评分（兼容旧版）")
    @PostMapping
    public Result<Void> submitScore(@Valid @RequestBody ExaminerScore score) {
        if (scoreService.submitScore(score)) {
            return Result.success("评分提交成功", null);
        }
        return Result.error("评分提交失败");
    }
    
    @Operation(summary = "提交多维度评分")
    @PostMapping("/multi")
    public Result<Void> submitMultiScore(@Valid @RequestBody SubmitMultiScoreDTO dto) {
        try {
            if (scoreService.submitMultiScore(dto)) {
                return Result.success("评分提交成功", null);
            }
            return Result.error("评分提交失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取考生的所有评分")
    @GetMapping("/candidate/{candidateId}")
    public Result<List<Map<String, Object>>> getCandidateScores(@PathVariable Long candidateId) {
        List<Map<String, Object>> scores = scoreService.getCandidateScores(candidateId);
        return Result.success(scores);
    }
    
    @Operation(summary = "获取考生评分明细（含各评分项详情")
    @GetMapping("/candidate/{candidateId}/details")
    public Result<Map<String, Object>> getCandidateScoreDetails(
            @PathVariable Long candidateId,
            @RequestParam Long projectId) {
        return Result.success(scoreService.getCandidateScoreDetails(candidateId, projectId));
    }
    
    @Operation(summary = "计算考生最终成绩")
    @PostMapping("/calculate/{candidateId}")
    public Result<FinalScore> calculateFinalScore(
            @PathVariable Long candidateId,
            @RequestParam Long projectId) {
        FinalScore score = scoreService.calculateFinalScore(projectId, candidateId);
        if (score != null) {
            return Result.success("成绩计算完成", score);
        }
        return Result.error("成绩计算失败");
    }
    
    @Operation(summary = "批量计算项目所有成绩")
    @PostMapping("/calculate-all")
    public Result<Integer> calculateAllScores(@RequestParam Long projectId) {
        int count = scoreService.calculateAllScores(projectId);
        return Result.success("成功计算" + count + "人的成绩", count);
    }
    
    @Operation(summary = "获取考生最终成绩")
    @GetMapping("/final/{candidateId}")
    public Result<FinalScore> getFinalScore(@PathVariable Long candidateId) {
        FinalScore score = scoreService.getFinalScore(candidateId);
        if (score == null) {
            return Result.notFound();
        }
        return Result.success(score);
    }
    
    @Operation(summary = "获取项目成绩排名")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getProjectRanking(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long positionId) {
        List<Map<String, Object>> ranking = scoreService.getProjectRanking(projectId, positionId);
        return Result.success(ranking);
    }
    
    @Operation(summary = "发布成绩")
    @PostMapping("/publish")
    public Result<Void> publishScores(@RequestParam Long projectId) {
        if (scoreService.publishScores(projectId)) {
            return Result.success("成绩发布成功", null);
        }
        return Result.error("成绩发布失败");
    }
    
    @Operation(summary = "获取评分统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getScoreStatistics(@RequestParam Long projectId) {
        Map<String, Object> stats = scoreService.getScoreStatistics(projectId);
        return Result.success(stats);
    }
    
    @Operation(summary = "实时计算面试分数")
    @GetMapping("/realtime/{candidateId}")
    public Result<BigDecimal> calculateRealtimeScore(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "true") Boolean removeExtreme) {
        BigDecimal score = scoreService.calculateInterviewScore(candidateId, removeExtreme);
        return Result.success(score);
    }
}
