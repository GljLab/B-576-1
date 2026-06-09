package com.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.entity.ExaminerScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考官评分Mapper
 */
@Mapper
public interface ExaminerScoreMapper extends BaseMapper<ExaminerScore> {
    
    /**
     * 获取考生的所有有效评分
     */
    @Select("SELECT total_score FROM examiner_score WHERE candidate_id = #{candidateId} AND is_valid = 1 AND status = 1")
    List<BigDecimal> getValidScoresByCandidateId(@Param("candidateId") Long candidateId);
}
