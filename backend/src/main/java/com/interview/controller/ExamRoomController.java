package com.interview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.common.Result;
import com.interview.entity.ExamRoom;
import com.interview.mapper.ExamRoomMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 考场控制器
 */
@Tag(name = "考场管理", description = "考场增删改查接口")
@RestController
@RequestMapping("/rooms")
public class ExamRoomController {
    
    @Autowired
    private ExamRoomMapper examRoomMapper;
    
    @Operation(summary = "分页查询考场")
    @GetMapping
    public Result<Page<ExamRoom>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword) {
        
        Page<ExamRoom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamRoom> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(ExamRoom::getProjectId, projectId);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(ExamRoom::getRoomName, keyword)
                    .or().like(ExamRoom::getRoomCode, keyword)
                    .or().like(ExamRoom::getLocation, keyword));
        }
        
        wrapper.eq(ExamRoom::getStatus, 1);
        wrapper.orderByAsc(ExamRoom::getRoomCode);
        
        return Result.success(examRoomMapper.selectPage(page, wrapper));
    }
    
    @Operation(summary = "获取项目所有考场")
    @GetMapping("/project/{projectId}")
    public Result<List<ExamRoom>> getByProjectId(@PathVariable Long projectId) {
        List<ExamRoom> rooms = examRoomMapper.selectList(
                new LambdaQueryWrapper<ExamRoom>()
                        .eq(ExamRoom::getProjectId, projectId)
                        .eq(ExamRoom::getStatus, 1)
                        .orderByAsc(ExamRoom::getRoomCode));
        return Result.success(rooms);
    }
    
    @Operation(summary = "获取考场详情")
    @GetMapping("/{id}")
    public Result<ExamRoom> getById(@PathVariable Long id) {
        ExamRoom room = examRoomMapper.selectById(id);
        if (room == null) {
            return Result.notFound();
        }
        return Result.success(room);
    }
    
    @Operation(summary = "创建考场")
    @PostMapping
    public Result<ExamRoom> create(@Valid @RequestBody ExamRoom room) {
        room.setStatus(1);
        if (examRoomMapper.insert(room) > 0) {
            return Result.success("创建成功", room);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新考场")
    @PutMapping("/{id}")
    public Result<ExamRoom> update(@PathVariable Long id, @Valid @RequestBody ExamRoom room) {
        room.setId(id);
        if (examRoomMapper.updateById(room) > 0) {
            return Result.success("更新成功", room);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除考场")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (examRoomMapper.deleteById(id) > 0) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
