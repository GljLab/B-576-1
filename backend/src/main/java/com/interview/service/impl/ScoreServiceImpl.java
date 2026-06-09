package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.entity.*;
import com.interview.entity.dto.SubmitMultiScoreDTO;
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
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Autowired
    private ScoreItemMapper scoreItemMapper;
    
    @Override
    @Transactional
    public boolean submitScore(ExaminerScore score) {
        score.setSubmitTime(LocalDateTime.now());
        score.setStatus(1);
        score.setIsValid(1);
        
        ExaminerScore existing = examinerScoreMapper.selectOne(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, score.getCandidateId())
                        .eq(ExaminerScore::getExaminerId, score.getExaminerId())
                        .isNull(ExaminerScore::getScoreItemId));
        
        if (existing != null) {
            score.setId(existing.getId());
            return examinerScoreMapper.updateById(score) > 0;
        }
        
        return examinerScoreMapper.insert(score) > 0;
    }
    
    @Override
    @Transactional
    public boolean submitMultiScore(SubmitMultiScoreDTO dto) {
        if (dto.getItemScores() == null || dto.getItemScores().isEmpty()) {
            return false;
        }
        
        List<ScoreItem> projectItems = scoreItemMapper.selectList(
                new LambdaQueryWrapper<ScoreItem>()
                        .eq(ScoreItem::getProjectId, dto.getProjectId())
                        .eq(ScoreItem::getStatus, 1));
        Map<Long, ScoreItem> itemMap = projectItems.stream()
                .collect(Collectors.toMap(ScoreItem::getId, item -> item));
        
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        
        for (SubmitMultiScoreDTO.ScoreItemDetail detail : dto.getItemScores()) {
            ScoreItem item = itemMap.get(detail.getScoreItemId());
            if (item == null || detail.getScore() == null) {
                continue;
            }
            
            if (detail.getScore().compareTo(BigDecimal.ZERO) < 0 
                    || detail.getScore().compareTo(item.getMaxScore()) > 0) {
                throw new IllegalArgumentException("评分项 " + item.getItemName() + " 分数超出范围");
            }
            
            BigDecimal weighted = detail.getScore()
                    .multiply(item.getWeight())
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            totalWeightedScore = totalWeightedScore.add(weighted);
            
            ExaminerScore es = new ExaminerScore();
            es.setProjectId(dto.getProjectId());
            es.setCandidateId(dto.getCandidateId());
            es.setExaminerId(dto.getExaminerId());
            es.setRoomId(dto.getRoomId());
            es.setScoreItemId(detail.getScoreItemId());
            es.setScore(detail.getScore());
            es.setWeightedScore(weighted.setScale(2, RoundingMode.HALF_UP));
            es.setSubmitTime(LocalDateTime.now());
            es.setStatus(1);
            es.setIsValid(1);
            
            ExaminerScore existing = examinerScoreMapper.selectOne(
                    new LambdaQueryWrapper<ExaminerScore>()
                            .eq(ExaminerScore::getCandidateId, dto.getCandidateId())
                            .eq(ExaminerScore::getExaminerId, dto.getExaminerId())
                            .eq(ExaminerScore::getScoreItemId, detail.getScoreItemId()));
            
            if (existing != null) {
                es.setId(existing.getId());
                examinerScoreMapper.updateById(es);
            } else {
                examinerScoreMapper.insert(es);
            }
        }
        
        ExaminerScore summary = new ExaminerScore();
        summary.setProjectId(dto.getProjectId());
        summary.setCandidateId(dto.getCandidateId());
        summary.setExaminerId(dto.getExaminerId());
        summary.setRoomId(dto.getRoomId());
        summary.setScoreItemId(null);
        summary.setTotalScore(totalWeightedScore.setScale(2, RoundingMode.HALF_UP));
        summary.setComment(dto.getComment());
        summary.setSubmitTime(LocalDateTime.now());
        summary.setStatus(1);
        summary.setIsValid(1);
        
        ExaminerScore existingSummary = examinerScoreMapper.selectOne(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, dto.getCandidateId())
                        .eq(ExaminerScore::getExaminerId, dto.getExaminerId())
                        .isNull(ExaminerScore::getScoreItemId));
        
        if (existingSummary != null) {
            summary.setId(existingSummary.getId());
            return examinerScoreMapper.updateById(summary) > 0;
        }
        
        return examinerScoreMapper.insert(summary) > 0;
    }
    
    @Override
    public List<Map<String, Object>> getCandidateScores(Long candidateId) {
        List<ExaminerScore> scores = examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getStatus, 1)
                        .isNull(ExaminerScore::getScoreItemId)
                        .orderByAsc(ExaminerScore::getExaminerId));
        
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
        InterviewProject project = projectMapper.selectById(projectId);
        if (project == null) {
            return null;
        }
        
        Candidate candidate = candidateMapper.selectById(candidateId);
        if (candidate == null) {
            return null;
        }
        
        List<ExaminerScore> scores = getExaminerSummaryScores(candidateId);
        if (scores.isEmpty()) {
            return null;
        }
        
        boolean removeExtreme = project.getRemoveHighest() == 1 && project.getRemoveLowest() == 1;
        BigDecimal interviewRawScore = calculateInterviewScore(candidateId, removeExtreme);
        
        BigDecimal writtenScore = candidate.getWrittenScore() != null ? candidate.getWrittenScore() : BigDecimal.ZERO;
        BigDecimal writtenWeight = project.getWrittenWeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal interviewWeight = project.getInterviewWeight().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        BigDecimal weightedWritten = writtenScore.multiply(writtenWeight);
        BigDecimal weightedInterview = interviewRawScore.multiply(interviewWeight);
        BigDecimal totalScore = weightedWritten.add(weightedInterview).setScale(project.getScorePrecision(), RoundingMode.HALF_UP);
        
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
            
            if (score.getCandidateId() != null) {
                Candidate candidate = candidateMapper.selectById(score.getCandidateId());
                if (candidate != null) {
                    item.put("candidateName", candidate.getCandidateName());
                    item.put("ticketNo", candidate.getTicketNo());
                }
            }
            
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
        List<ExaminerScore> scores = getExaminerSummaryScores(candidateId);
        
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
        
        if (removeExtreme && scoreValues.size() > 2) {
            markExtremeScores(candidateId, scores);
            scoreValues = scoreValues.subList(1, scoreValues.size() - 1);
        }
        
        BigDecimal sum = scoreValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(scoreValues.size()), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public Map<String, Object> getCandidateScoreDetails(Long candidateId, Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ScoreItem> items = scoreItemMapper.selectList(
                new LambdaQueryWrapper<ScoreItem>()
                        .eq(ScoreItem::getProjectId, projectId)
                        .eq(ScoreItem::getStatus, 1)
                        .orderByAsc(ScoreItem::getSortOrder));
        result.put("scoreItems", items);
        
        List<Examiner> examiners = examinerMapper.selectList(
                new LambdaQueryWrapper<Examiner>()
                        .eq(Examiner::getProjectId, projectId)
                        .orderByAsc(Examiner::getSeatNo));
        result.put("examiners", examiners);
        
        List<ExaminerScore> allScores = examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getProjectId, projectId)
                        .eq(ExaminerScore::getStatus, 1));
        
        Map<Long, ExaminerScore> summaryByExaminer = new HashMap<>();
        Map<String, ExaminerScore> detailByExaminerItem = new HashMap<>();
        
        for (ExaminerScore es : allScores) {
            if (es.getScoreItemId() == null) {
                summaryByExaminer.put(es.getExaminerId(), es);
            } else {
                String key = es.getExaminerId() + "_" + es.getScoreItemId();
                detailByExaminerItem.put(key, es);
            }
        }
        
        result.put("summaryByExaminer", summaryByExaminer);
        result.put("detailByExaminerItem", detailByExaminerItem);
        
        Map<Long, BigDecimal> itemAvgScores = new HashMap<>();
        for (ScoreItem item : items) {
            List<BigDecimal> itemScores = new ArrayList<>();
            for (Examiner examiner : examiners) {
                String key = examiner.getId() + "_" + item.getId();
                ExaminerScore es = detailByExaminerItem.get(key);
                if (es != null && es.getScore() != null && es.getIsValid() == 1) {
                    itemScores.add(es.getScore());
                }
            }
            if (!itemScores.isEmpty()) {
                BigDecimal sum = itemScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                itemAvgScores.put(item.getId(), sum.divide(new BigDecimal(itemScores.size()), 2, RoundingMode.HALF_UP));
            } else {
                itemAvgScores.put(item.getId(), BigDecimal.ZERO);
            }
        }
        result.put("itemAvgScores", itemAvgScores);
        
        List<Map<String, Object>> radarData = new ArrayList<>();
        for (ScoreItem item : items) {
            Map<String, Object> rd = new HashMap<>();
            rd.put("name", item.getItemName());
            rd.put("avg", itemAvgScores.get(item.getId()));
            rd.put("max", item.getMaxScore());
            radarData.add(rd);
        }
        result.put("radarData", radarData);
        
        return result;
    }
    
    private List<ExaminerScore> getExaminerSummaryScores(Long candidateId) {
        return examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getStatus, 1)
                        .isNull(ExaminerScore::getScoreItemId)
                        .orderByAsc(ExaminerScore::getExaminerId));
    }
    
    private void markExtremeScores(Long candidateId, List<ExaminerScore> scores) {
        if (scores.size() <= 2) {
            return;
        }
        
        ExaminerScore highest = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .max(Comparator.comparing(ExaminerScore::getTotalScore))
                .orElse(null);
        
        ExaminerScore lowest = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .min(Comparator.comparing(ExaminerScore::getTotalScore))
                .orElse(null);
        
        if (highest != null) {
            highest.setIsValid(0);
            examinerScoreMapper.updateById(highest);
        }
        
        if (lowest != null && !lowest.getId().equals(highest != null ? highest.getId() : null)) {
            lowest.setIsValid(0);
            examinerScoreMapper.updateById(lowest);
        }
    }
    
    private void calculateRanking(Long projectId) {
        LambdaQueryWrapper<FinalScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalScore::getProjectId, projectId);
        wrapper.orderByDesc(FinalScore::getTotalScore);
        List<FinalScore> allScores = finalScoreMapper.selectList(wrapper);
        int overallRank = 1;
        for (FinalScore score : allScores) {
            score.setOverallRank(overallRank++);
            finalScoreMapper.updateById(score);
        }
        
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
