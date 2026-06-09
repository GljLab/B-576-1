package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.InterviewProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试项目Mapper
 */
@Mapper
public interface InterviewProjectMapper extends BaseMapper<InterviewProject> {
}
