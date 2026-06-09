package com.interview.service;

import com.interview.entity.ScoreItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ScoreItemService {
    
    List<ScoreItem> getByProjectId(Long projectId);
    
    ScoreItem getById(Long id);
    
    boolean save(ScoreItem scoreItem);
    
    boolean update(ScoreItem scoreItem);
    
    boolean delete(Long id);
    
    boolean batchSave(List<ScoreItem> items);
    
    Map<String, Object> validateWeights(Long projectId);
    
    boolean updateSortOrder(Long projectId, List<Long> itemIds);
    
    boolean applyTemplate(Long projectId, Long templateId, boolean overwrite);
    
    Map<String, Object> preview(Long projectId);
    
    List<Map<String, Object>> getItemStatistics(Long projectId);
    
    Map<String, Object> getCandidateItemScores(Long candidateId, Long projectId);
}
