package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Position;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位Mapper
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {
}
