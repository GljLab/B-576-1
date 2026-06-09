package com.interview.service;

import com.interview.entity.ExaminerScore;
import com.interview.entity.FinalScore;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 评分服务接口
 */
public interface ScoreService {
    
    /**
     * 考官提交评分
     */
    boolean submitScore(ExaminerScore score);
    
    /**
     * 获取考生的所有评分
     */
    List<Map<String, Object>> getCandidateScores(Long candidateId);
    
    /**
     * 计算并保存考生最终成绩
     */
    FinalScore calculateFinalScore(Long projectId, Long candidateId);
    
    /**
     * 批量计算项目所有考生成绩
     */
    int calculateAllScores(Long projectId);
    
    /**
     * 获取考生最终成绩
     */
    FinalScore getFinalScore(Long candidateId);
    
    /**
     * 获取项目成绩排名
     */
    List<Map<String, Object>> getProjectRanking(Long projectId, Long positionId);
    
    /**
     * 发布成绩
     */
    boolean publishScores(Long projectId);
    
    /**
     * 获取评分统计
     */
    Map<String, Object> getScoreStatistics(Long projectId);
    
    /**
     * 实时计算考生面试分数（去掉最高最低后平均）
     */
    BigDecimal calculateInterviewScore(Long candidateId, boolean removeExtreme);
}
