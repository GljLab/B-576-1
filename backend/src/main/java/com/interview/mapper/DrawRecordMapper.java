package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.DrawRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽签记录Mapper
 */
@Mapper
public interface DrawRecordMapper extends BaseMapper<DrawRecord> {
}
