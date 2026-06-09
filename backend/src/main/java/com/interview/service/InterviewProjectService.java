package com.interview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.entity.InterviewProject;

import java.util.Map;

/**
 * 面试项目服务接口
 */
public interface InterviewProjectService extends IService<InterviewProject> {
    
    /**
     * 分页查询项目
     */
    Page<InterviewProject> pageProjects(Integer pageNum, Integer pageSize, String keyword, Integer status);
    
    /**
     * 获取项目详情（包含统计信息）
     */
    Map<String, Object> getProjectDetail(Long projectId);
    
    /**
     * 更新项目状态
     */
    boolean updateStatus(Long projectId, Integer status);
    
    /**
     * 获取项目统计信息
     */
    Map<String, Object> getProjectStatistics(Long projectId);
}
