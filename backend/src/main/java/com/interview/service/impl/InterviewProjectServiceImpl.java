package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.entity.*;
import com.interview.mapper.*;
import com.interview.service.InterviewProjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 面试项目服务实现
 */
@Service
public class InterviewProjectServiceImpl extends ServiceImpl<InterviewProjectMapper, InterviewProject> 
        implements InterviewProjectService {
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Autowired
    private CandidateMapper candidateMapper;
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Autowired
    private ExamRoomMapper examRoomMapper;
    
    @Override
    public Page<InterviewProject> pageProjects(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<InterviewProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InterviewProject> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(InterviewProject::getProjectName, keyword)
                    .or().like(InterviewProject::getProjectCode, keyword)
                    .or().like(InterviewProject::getOrganizer, keyword));
        }
        
        if (status != null) {
            wrapper.eq(InterviewProject::getStatus, status);
        }
        
        wrapper.orderByDesc(InterviewProject::getCreateTime);
        
        return page(page, wrapper);
    }
    
    @Override
    public Map<String, Object> getProjectDetail(Long projectId) {
        InterviewProject project = getById(projectId);
        if (project == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.putAll(getProjectStatistics(projectId));
        
        return result;
    }
    
    @Override
    public boolean updateStatus(Long projectId, Integer status) {
        InterviewProject project = new InterviewProject();
        project.setId(projectId);
        project.setStatus(status);
        return updateById(project);
    }
    
    @Override
    public Map<String, Object> getProjectStatistics(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 职位数量
        Long positionCount = positionMapper.selectCount(
                new LambdaQueryWrapper<Position>().eq(Position::getProjectId, projectId));
        stats.put("positionCount", positionCount);
        
        // 考生数量
        Long candidateCount = candidateMapper.selectCount(
                new LambdaQueryWrapper<Candidate>().eq(Candidate::getProjectId, projectId));
        stats.put("candidateCount", candidateCount);
        
        // 已签到考生数量
        Long checkedInCount = candidateMapper.selectCount(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getProjectId, projectId)
                        .eq(Candidate::getCheckInStatus, 1));
        stats.put("checkedInCount", checkedInCount);
        
        // 已完成面试考生数量
        Long completedCount = candidateMapper.selectCount(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getProjectId, projectId)
                        .eq(Candidate::getInterviewStatus, 2));
        stats.put("completedCount", completedCount);
        
        // 考官数量
        Long examinerCount = examinerMapper.selectCount(
                new LambdaQueryWrapper<Examiner>().eq(Examiner::getProjectId, projectId));
        stats.put("examinerCount", examinerCount);
        
        // 考场数量
        Long roomCount = examRoomMapper.selectCount(
                new LambdaQueryWrapper<ExamRoom>().eq(ExamRoom::getProjectId, projectId));
        stats.put("roomCount", roomCount);
        
        return stats;
    }
}
