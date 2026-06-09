package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.entity.Candidate;
import com.interview.entity.ExaminerScore;
import com.interview.entity.FinalScore;
import com.interview.entity.Position;
import com.interview.mapper.CandidateMapper;
import com.interview.mapper.ExaminerScoreMapper;
import com.interview.mapper.FinalScoreMapper;
import com.interview.mapper.PositionMapper;
import com.interview.service.CandidateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考生服务实现
 */
@Service
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, Candidate> implements CandidateService {
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Autowired
    private ExaminerScoreMapper examinerScoreMapper;
    
    @Autowired
    private FinalScoreMapper finalScoreMapper;
    
    @Override
    public Page<Candidate> pageCandidates(Integer pageNum, Integer pageSize, Long projectId, Long positionId, String keyword) {
        Page<Candidate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(Candidate::getProjectId, projectId);
        }
        
        if (positionId != null) {
            wrapper.eq(Candidate::getPositionId, positionId);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Candidate::getCandidateName, keyword)
                    .or().like(Candidate::getTicketNo, keyword)
                    .or().like(Candidate::getIdCard, keyword));
        }
        
        wrapper.eq(Candidate::getStatus, 1);
        wrapper.orderByAsc(Candidate::getInterviewOrder);
        
        return page(page, wrapper);
    }
    
    @Override
    @Transactional
    public boolean checkIn(Long candidateId) {
        Candidate candidate = getById(candidateId);
        if (candidate == null) {
            return false;
        }
        
        candidate.setCheckInStatus(1);
        candidate.setCheckInTime(LocalDateTime.now());
        
        return updateById(candidate);
    }
    
    @Override
    @Transactional
    public int batchImport(Long projectId, List<Candidate> candidates) {
        int count = 0;
        for (Candidate candidate : candidates) {
            candidate.setProjectId(projectId);
            candidate.setStatus(1);
            candidate.setCheckInStatus(0);
            candidate.setInterviewStatus(0);
            if (save(candidate)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public Map<String, Object> getCandidateDetail(Long candidateId) {
        Candidate candidate = getById(candidateId);
        if (candidate == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("candidate", candidate);
        
        // 获取职位信息
        if (candidate.getPositionId() != null) {
            Position position = positionMapper.selectById(candidate.getPositionId());
            result.put("position", position);
        }
        
        // 获取评分列表
        List<ExaminerScore> scores = examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .orderByAsc(ExaminerScore::getExaminerId));
        result.put("scores", scores);
        
        // 获取最终成绩
        FinalScore finalScore = finalScoreMapper.selectOne(
                new LambdaQueryWrapper<FinalScore>()
                        .eq(FinalScore::getCandidateId, candidateId));
        result.put("finalScore", finalScore);
        
        return result;
    }
    
    @Override
    public boolean updateInterviewStatus(Long candidateId, Integer status) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        candidate.setInterviewStatus(status);
        return updateById(candidate);
    }
    
    @Override
    public List<Candidate> getWaitingCandidates(Long projectId, Long roomId) {
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Candidate::getProjectId, projectId);
        wrapper.eq(Candidate::getCheckInStatus, 1);
        // 包含待面试和面试中的考生
        wrapper.in(Candidate::getInterviewStatus, 0, 1);
        wrapper.eq(Candidate::getStatus, 1);
        
        if (roomId != null) {
            wrapper.eq(Candidate::getRoomId, roomId);
        }
        
        // 先按状态排序（面试中优先），再按面试顺序排序
        wrapper.orderByDesc(Candidate::getInterviewStatus);
        wrapper.orderByAsc(Candidate::getInterviewOrder);
        
        return list(wrapper);
    }
}
