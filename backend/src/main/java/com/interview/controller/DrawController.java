package com.interview.controller;

import com.interview.common.Result;
import com.interview.entity.DrawRecord;
import com.interview.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 抽签控制器
 */
@Tag(name = "抽签管理", description = "三盲抽签相关接口")
@RestController
@RequestMapping("/draw")
public class DrawController {
    
    @Autowired
    private DrawService drawService;
    
    @Operation(summary = "执行三盲抽签")
    @PostMapping("/triple-blind")
    public Result<Map<String, Object>> executeTripleBlindDraw(
            @RequestParam Long projectId,
            @RequestAttribute(value = "userId", required = false) Long operatorId,
            @RequestAttribute(value = "username", required = false) String operatorName) {
        
        if (operatorId == null) {
            operatorId = 1L;
            operatorName = "系统管理员";
        }
        
        Map<String, Object> result = drawService.executeTripleBlindDraw(projectId, operatorId, operatorName);
        return Result.success("三盲抽签完成", result);
    }
    
    @Operation(summary = "考官抽签")
    @PostMapping("/examiners")
    public Result<List<DrawRecord>> drawExaminers(
            @RequestParam Long projectId,
            @RequestAttribute(value = "userId", required = false) Long operatorId,
            @RequestAttribute(value = "username", required = false) String operatorName) {
        
        if (operatorId == null) {
            operatorId = 1L;
            operatorName = "系统管理员";
        }
        
        List<DrawRecord> records = drawService.drawExaminers(projectId, operatorId, operatorName);
        return Result.success("考官抽签完成", records);
    }
    
    @Operation(summary = "考生抽签")
    @PostMapping("/candidates")
    public Result<List<DrawRecord>> drawCandidates(
            @RequestParam Long projectId,
            @RequestAttribute(value = "userId", required = false) Long operatorId,
            @RequestAttribute(value = "username", required = false) String operatorName) {
        
        if (operatorId == null) {
            operatorId = 1L;
            operatorName = "系统管理员";
        }
        
        List<DrawRecord> records = drawService.drawCandidates(projectId, operatorId, operatorName);
        return Result.success("考生抽签完成", records);
    }
    
    @Operation(summary = "职位顺序抽签")
    @PostMapping("/positions")
    public Result<List<DrawRecord>> drawPositionOrder(
            @RequestParam Long projectId,
            @RequestAttribute(value = "userId", required = false) Long operatorId,
            @RequestAttribute(value = "username", required = false) String operatorName) {
        
        if (operatorId == null) {
            operatorId = 1L;
            operatorName = "系统管理员";
        }
        
        List<DrawRecord> records = drawService.drawPositionOrder(projectId, operatorId, operatorName);
        return Result.success("职位顺序抽签完成", records);
    }
    
    @Operation(summary = "获取抽签结果")
    @GetMapping("/results")
    public Result<List<DrawRecord>> getDrawResults(
            @RequestParam Long projectId,
            @RequestParam(required = false) String drawType) {
        List<DrawRecord> records = drawService.getDrawResults(projectId, drawType);
        return Result.success(records);
    }
    
    @Operation(summary = "重置抽签")
    @DeleteMapping("/reset")
    public Result<Void> resetDraw(
            @RequestParam Long projectId,
            @RequestParam String drawType) {
        if (drawService.resetDraw(projectId, drawType)) {
            return Result.success("重置成功", null);
        }
        return Result.error("重置失败");
    }
}
