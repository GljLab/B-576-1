package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Examiner;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考官Mapper
 */
@Mapper
public interface ExaminerMapper extends BaseMapper<Examiner> {
}
