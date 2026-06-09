package com.interview.service;

import com.interview.entity.ExaminerScore;
import com.interview.entity.FinalScore;
import com.interview.entity.dto.SubmitMultiScoreDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ScoreService {
    
    boolean submitScore(ExaminerScore score);
    
    boolean submitMultiScore(SubmitMultiScoreDTO dto);
    
    List<Map<String, Object>> getCandidateScores(Long candidateId);
    
    FinalScore calculateFinalScore(Long projectId, Long candidateId);
    
    int calculateAllScores(Long projectId);
    
    FinalScore getFinalScore(Long candidateId);
    
    List<Map<String, Object>> getProjectRanking(Long projectId, Long positionId);
    
    boolean publishScores(Long projectId);
    
    Map<String, Object> getScoreStatistics(Long projectId);
    
    BigDecimal calculateInterviewScore(Long candidateId, boolean removeExtreme);
    
    Map<String, Object> getCandidateScoreDetails(Long candidateId, Long projectId);
}
