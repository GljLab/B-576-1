package com.interview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.interview.common.Result;
import com.interview.entity.Position;
import com.interview.mapper.PositionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 职位控制器
 */
@Tag(name = "职位管理", description = "职位增删改查接口")
@RestController
@RequestMapping("/positions")
public class PositionController {
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Operation(summary = "分页查询职位")
    @GetMapping
    public Result<Page<Position>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword) {
        
        Page<Position> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(Position::getProjectId, projectId);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Position::getPositionName, keyword)
                    .or().like(Position::getPositionCode, keyword)
                    .or().like(Position::getDepartment, keyword));
        }
        
        wrapper.eq(Position::getStatus, 1);
        wrapper.orderByAsc(Position::getInterviewOrder);
        
        return Result.success(positionMapper.selectPage(page, wrapper));
    }
    
    @Operation(summary = "获取项目所有职位")
    @GetMapping("/project/{projectId}")
    public Result<List<Position>> getByProjectId(@PathVariable Long projectId) {
        List<Position> positions = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getProjectId, projectId)
                        .eq(Position::getStatus, 1)
                        .orderByAsc(Position::getInterviewOrder));
        return Result.success(positions);
    }
    
    @Operation(summary = "获取职位详情")
    @GetMapping("/{id}")
    public Result<Position> getById(@PathVariable Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            return Result.notFound();
        }
        return Result.success(position);
    }
    
    @Operation(summary = "创建职位")
    @PostMapping
    public Result<Position> create(@Valid @RequestBody Position position) {
        position.setStatus(1);
        if (positionMapper.insert(position) > 0) {
            return Result.success("创建成功", position);
        }
        return Result.error("创建失败");
    }
    
    @Operation(summary = "更新职位")
    @PutMapping("/{id}")
    public Result<Position> update(@PathVariable Long id, @Valid @RequestBody Position position) {
        position.setId(id);
        if (positionMapper.updateById(position) > 0) {
            return Result.success("更新成功", position);
        }
        return Result.error("更新失败");
    }
    
    @Operation(summary = "删除职位")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (positionMapper.deleteById(id) > 0) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
}
