package com.interview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.entity.Candidate;

import java.util.List;
import java.util.Map;

/**
 * 考生服务接口
 */
public interface CandidateService extends IService<Candidate> {
    
    /**
     * 分页查询考生
     */
    Page<Candidate> pageCandidates(Integer pageNum, Integer pageSize, Long projectId, Long positionId, String keyword);
    
    /**
     * 考生签到
     */
    boolean checkIn(Long candidateId);
    
    /**
     * 批量导入考生
     */
    int batchImport(Long projectId, List<Candidate> candidates);
    
    /**
     * 获取考生详情（包含评分信息）
     */
    Map<String, Object> getCandidateDetail(Long candidateId);
    
    /**
     * 更新面试状态
     */
    boolean updateInterviewStatus(Long candidateId, Integer status);
    
    /**
     * 获取待面试考生列表
     */
    List<Candidate> getWaitingCandidates(Long projectId, Long roomId);
}
