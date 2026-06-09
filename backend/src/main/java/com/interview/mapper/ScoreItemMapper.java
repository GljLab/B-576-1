package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.ScoreItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评分项Mapper
 */
@Mapper
public interface ScoreItemMapper extends BaseMapper<ScoreItem> {
}
