package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考生Mapper
 */
@Mapper
public interface CandidateMapper extends BaseMapper<Candidate> {
}
