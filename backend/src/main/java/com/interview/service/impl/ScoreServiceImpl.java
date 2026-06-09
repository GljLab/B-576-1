package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.entity.*;
import com.interview.mapper.*;
import com.interview.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评分服务实现
 */
@Service
public class ScoreServiceImpl implements ScoreService {
    
    @Autowired
    private ExaminerScoreMapper examinerScoreMapper;
    
    @Autowired
    private FinalScoreMapper finalScoreMapper;
    
    @Autowired
    private CandidateMapper candidateMapper;
    
    @Autowired
    private InterviewProjectMapper projectMapper;
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Override
    @Transactional
    public boolean submitScore(ExaminerScore score) {
        score.setSubmitTime(LocalDateTime.now());
        score.setStatus(1);
        score.setIsValid(1);
        
        // 检查是否已存在评分记录
        ExaminerScore existing = examinerScoreMapper.selectOne(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, score.getCandidateId())
                        .eq(ExaminerScore::getExaminerId, score.getExaminerId()));
        
        if (existing != null) {
            score.setId(existing.getId());
            return examinerScoreMapper.updateById(score) > 0;
        }
        
        return examinerScoreMapper.insert(score) > 0;
    }
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Override
    public List<Map<String, Object>> getCandidateScores(Long candidateId) {
        List<ExaminerScore> scores = examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getStatus, 1)
                        .orderByAsc(ExaminerScore::getExaminerId));
        
        // 转换为包含考官姓名的Map列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExaminerScore score : scores) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", score.getId());
            item.put("projectId", score.getProjectId());
            item.put("candidateId", score.getCandidateId());
            item.put("examinerId", score.getExaminerId());
            item.put("totalScore", score.getTotalScore());
            item.put("comment", score.getComment());
            item.put("isValid", score.getIsValid());
            item.put("submitTime", score.getSubmitTime());
            item.put("status", score.getStatus());
            
            // 获取考官姓名
            if (score.getExaminerId() != null) {
                Examiner examiner = examinerMapper.selectById(score.getExaminerId());
                if (examiner != null) {
                    item.put("examinerName", examiner.getExaminerName());
                }
            }
            
            result.add(item);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public FinalScore calculateFinalScore(Long projectId, Long candidateId) {
        // 获取项目配置
        InterviewProject project = projectMapper.selectById(projectId);
        if (project == null) {
            return null;
        }
        
        // 获取考生信息
        Candidate candidate = candidateMapper.selectById(candidateId);
        if (candidate == null) {
            return null;
        }
        
        // 获取所有评分
        List<ExaminerScore> scores = getExaminerScoreList(candidateId);
        if (scores.isEmpty()) {
            return null;
        }
        
        // 计算面试分数
        boolean removeExtreme = project.getRemoveHighest() == 1 && project.getRemoveLowest() == 1;
        BigDecimal interviewRawScore = calculateInterviewScore(candidateId, removeExtreme);
        
        // 计算加权成绩
        BigDecimal writtenScore = candidate.getWrittenScore() != null ? candidate.getWrittenScore() : BigDecimal.ZERO;
        BigDecimal writtenWeight = project.getWrittenWeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal interviewWeight = project.getInterviewWeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        BigDecimal weightedWritten = writtenScore.multiply(writtenWeight);
        BigDecimal weightedInterview = interviewRawScore.multiply(interviewWeight);
        BigDecimal totalScore = weightedWritten.add(weightedInterview).setScale(project.getScorePrecision(), RoundingMode.HALF_UP);
        
        // 创建或更新最终成绩
        FinalScore finalScore = finalScoreMapper.selectOne(
                new LambdaQueryWrapper<FinalScore>()
                        .eq(FinalScore::getCandidateId, candidateId));
        
        if (finalScore == null) {
            finalScore = new FinalScore();
            finalScore.setCandidateId(candidateId);
        }
        
        finalScore.setProjectId(projectId);
        finalScore.setPositionId(candidate.getPositionId());
        finalScore.setWrittenScore(writtenScore);
        finalScore.setInterviewRawScore(interviewRawScore);
        finalScore.setInterviewScore(weightedInterview.setScale(project.getScorePrecision(), RoundingMode.HALF_UP));
        finalScore.setTotalScore(totalScore);
        
        if (finalScore.getId() == null) {
            finalScoreMapper.insert(finalScore);
        } else {
            finalScoreMapper.updateById(finalScore);
        }
        
        return finalScore;
    }
    
    @Override
    @Transactional
    public int calculateAllScores(Long projectId) {
        // 获取项目所有考生
        List<Candidate> candidates = candidateMapper.selectList(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getProjectId, projectId)
                        .eq(Candidate::getInterviewStatus, 2)
                        .eq(Candidate::getStatus, 1));
        
        int count = 0;
        for (Candidate candidate : candidates) {
            FinalScore score = calculateFinalScore(projectId, candidate.getId());
            if (score != null) {
                count++;
            }
        }
        
        // 计算排名
        calculateRanking(projectId);
        
        return count;
    }
    
    @Override
    public FinalScore getFinalScore(Long candidateId) {
        return finalScoreMapper.selectOne(
                new LambdaQueryWrapper<FinalScore>()
                        .eq(FinalScore::getCandidateId, candidateId));
    }
    
    @Override
    public List<Map<String, Object>> getProjectRanking(Long projectId, Long positionId) {
        LambdaQueryWrapper<FinalScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalScore::getProjectId, projectId);
        
        if (positionId != null) {
            wrapper.eq(FinalScore::getPositionId, positionId);
        }
        
        wrapper.orderByDesc(FinalScore::getTotalScore);
        
        List<FinalScore> scores = finalScoreMapper.selectList(wrapper);
        
        // 转换为包含考生姓名和职位名称的Map列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (FinalScore score : scores) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", score.getId());
            item.put("projectId", score.getProjectId());
            item.put("candidateId", score.getCandidateId());
            item.put("positionId", score.getPositionId());
            item.put("writtenScore", score.getWrittenScore());
            item.put("interviewRawScore", score.getInterviewRawScore());
            item.put("interviewScore", score.getInterviewScore());
            item.put("totalScore", score.getTotalScore());
            item.put("positionRank", score.getPositionRank());
            item.put("overallRank", score.getOverallRank());
            item.put("isPass", score.getIsPass());
            item.put("publishStatus", score.getPublishStatus());
            item.put("publishTime", score.getPublishTime());
            
            // 获取考生姓名
            if (score.getCandidateId() != null) {
                Candidate candidate = candidateMapper.selectById(score.getCandidateId());
                if (candidate != null) {
                    item.put("candidateName", candidate.getCandidateName());
                    item.put("ticketNo", candidate.getTicketNo());
                }
            }
            
            // 获取职位名称
            if (score.getPositionId() != null) {
                Position position = positionMapper.selectById(score.getPositionId());
                if (position != null) {
                    item.put("positionName", position.getPositionName());
                }
            }
            
            result.add(item);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean publishScores(Long projectId) {
        List<FinalScore> scores = finalScoreMapper.selectList(
                new LambdaQueryWrapper<FinalScore>()
                        .eq(FinalScore::getProjectId, projectId));
        
        LocalDateTime now = LocalDateTime.now();
        for (FinalScore score : scores) {
            score.setPublishStatus(1);
            score.setPublishTime(now);
            finalScoreMapper.updateById(score);
        }
        
        return true;
    }
    
    @Override
    public Map<String, Object> getScoreStatistics(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 直接查询FinalScore列表
        List<FinalScore> scores = finalScoreMapper.selectList(
                new LambdaQueryWrapper<FinalScore>()
                        .eq(FinalScore::getProjectId, projectId)
                        .orderByDesc(FinalScore::getTotalScore));
        
        if (scores.isEmpty()) {
            stats.put("count", 0);
            stats.put("avgScore", BigDecimal.ZERO);
            stats.put("maxScore", BigDecimal.ZERO);
            stats.put("minScore", BigDecimal.ZERO);
            return stats;
        }
        
        stats.put("count", scores.size());
        
        BigDecimal sum = scores.stream()
                .map(FinalScore::getTotalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avg = sum.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);
        stats.put("avgScore", avg);
        
        BigDecimal max = scores.stream()
                .map(FinalScore::getTotalScore)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        stats.put("maxScore", max);
        
        BigDecimal min = scores.stream()
                .map(FinalScore::getTotalScore)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        stats.put("minScore", min);
        
        return stats;
    }
    
    @Override
    public BigDecimal calculateInterviewScore(Long candidateId, boolean removeExtreme) {
        // 直接查询ExaminerScore列表
        List<ExaminerScore> scores = getExaminerScoreList(candidateId);
        
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> scoreValues = scores.stream()
                .map(ExaminerScore::getTotalScore)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
        
        if (scoreValues.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 如果需要去掉最高最低分且评委数量大于2
        if (removeExtreme && scoreValues.size() > 2) {
            // 标记最高最低分为无效
            markExtremeScores(candidateId, scores);
            
            // 去掉最高和最低分
            scoreValues = scoreValues.subList(1, scoreValues.size() - 1);
        }
        
        // 计算平均分
        BigDecimal sum = scoreValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(scoreValues.size()), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 获取原始考官评分列表（内部使用）
     */
    private List<ExaminerScore> getExaminerScoreList(Long candidateId) {
        return examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getStatus, 1)
                        .orderByAsc(ExaminerScore::getExaminerId));
    }
    
    /**
     * 标记最高最低分
     */
    private void markExtremeScores(Long candidateId, List<ExaminerScore> scores) {
        if (scores.size() <= 2) {
            return;
        }
        
        // 找出最高和最低分
        ExaminerScore highest = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .max(Comparator.comparing(ExaminerScore::getTotalScore))
                .orElse(null);
        
        ExaminerScore lowest = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .min(Comparator.comparing(ExaminerScore::getTotalScore))
                .orElse(null);
        
        // 标记为无效
        if (highest != null) {
            highest.setIsValid(0);
            examinerScoreMapper.updateById(highest);
        }
        
        if (lowest != null && !lowest.getId().equals(highest != null ? highest.getId() : null)) {
            lowest.setIsValid(0);
            examinerScoreMapper.updateById(lowest);
        }
    }
    
    /**
     * 计算排名
     */
    private void calculateRanking(Long projectId) {
        // 总排名 - 直接查询FinalScore列表
        LambdaQueryWrapper<FinalScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalScore::getProjectId, projectId);
        wrapper.orderByDesc(FinalScore::getTotalScore);
        List<FinalScore> allScores = finalScoreMapper.selectList(wrapper);
        int overallRank = 1;
        for (FinalScore score : allScores) {
            score.setOverallRank(overallRank++);
            finalScoreMapper.updateById(score);
        }
        
        // 按职位排名
        Map<Long, List<FinalScore>> scoresByPosition = allScores.stream()
                .filter(s -> s.getPositionId() != null)
                .collect(Collectors.groupingBy(FinalScore::getPositionId));
        
        for (List<FinalScore> positionScores : scoresByPosition.values()) {
            positionScores.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
            int positionRank = 1;
            for (FinalScore score : positionScores) {
                score.setPositionRank(positionRank++);
                finalScoreMapper.updateById(score);
            }
        }
    }
}
