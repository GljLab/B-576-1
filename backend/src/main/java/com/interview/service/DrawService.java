package com.interview.service;

import com.interview.entity.DrawRecord;

import java.util.List;
import java.util.Map;

/**
 * 抽签服务接口
 */
public interface DrawService {
    
    /**
     * 考官抽签（分配考场）
     */
    List<DrawRecord> drawExaminers(Long projectId, Long operatorId, String operatorName);
    
    /**
     * 考生抽签（分配面试顺序）
     */
    List<DrawRecord> drawCandidates(Long projectId, Long operatorId, String operatorName);
    
    /**
     * 职位顺序抽签
     */
    List<DrawRecord> drawPositionOrder(Long projectId, Long operatorId, String operatorName);
    
    /**
     * 执行三盲抽签
     */
    Map<String, Object> executeTripleBlindDraw(Long projectId, Long operatorId, String operatorName);
    
    /**
     * 获取抽签结果
     */
    List<DrawRecord> getDrawResults(Long projectId, String drawType);
    
    /**
     * 重置抽签
     */
    boolean resetDraw(Long projectId, String drawType);
}
