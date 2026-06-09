package com.interview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.common.Result;
import com.interview.entity.Examiner;
import com.interview.mapper.ExaminerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 考官控制器
 */
@Tag(name = "考官管理", description = "考官增删改查接口")
@RestController
@RequestMapping("/examiners")
public class ExaminerController {
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Operation(summary = "分页查询考官")
    @GetMapping
    public Result<Page<Examiner>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String keyword) {
        
        Page<Examiner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Examiner> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(Examiner::getProjectId, projectId);
        }
        
        if (roomId != null) {
            wrapper.eq(Examiner::getRoomId, roomId);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Examiner::getExaminerName, keyword)
                    .or().like(Examiner::getExaminerCode, keyword)
                    .or().like(Examiner::getOrganization, keyword));
        }
        
        wrapper.eq(Examiner::getStatus, 1);
        wrapper.orderByAsc(Examiner::getSeatNo);
        
        return Result.success(examinerMapper.selectPage(page, wrapper));
    }
    
    @Operation(summary = "获取考官详情")
    @GetMapping("/{id}")
    public Result<Examiner> getById(@PathVariable Long id) {
        Examiner examiner = examinerMapper.selectById(id);
        if (examiner == null) {
            return Result.notFound();
        }
        return Result.success(examiner);
    }
    
    @Operation(summary = "创建考官")
    @PostMapping
    public Result<Examiner> create(@Valid @RequestBody Examiner examiner) {
        examiner.setStatus(1);
        if (examinerMapper.insert(examiner) > 0) {
            return Result.success("创建成功", examiner);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新考官")
    @PutMapping("/{id}")
    public Result<Examiner> update(@PathVariable Long id, @Valid @RequestBody Examiner examiner) {
        examiner.setId(id);
        if (examinerMapper.updateById(examiner) > 0) {
            return Result.success("更新成功", examiner);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除考官")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (examinerMapper.deleteById(id) > 0) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @Operation(summary = "获取考场考官列表")
    @GetMapping("/room/{roomId}")
    public Result<List<Examiner>> getByRoomId(@PathVariable Long roomId) {
        List<Examiner> examiners = examinerMapper.selectList(
                new LambdaQueryWrapper<Examiner>()
                        .eq(Examiner::getRoomId, roomId)
                        .eq(Examiner::getStatus, 1)
                        .orderByAsc(Examiner::getSeatNo));
        return Result.success(examiners);
    }
}
