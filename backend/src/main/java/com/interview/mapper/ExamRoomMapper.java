package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.ExamRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考场Mapper
 */
@Mapper
public interface ExamRoomMapper extends BaseMapper<ExamRoom> {
}
