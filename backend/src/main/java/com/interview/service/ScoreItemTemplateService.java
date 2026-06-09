package com.interview.service;

import com.interview.entity.ScoreItemTemplate;

import java.util.List;

public interface ScoreItemTemplateService {
    
    List<ScoreItemTemplate> list(Integer isSystem);
    
    ScoreItemTemplate getDetail(Long id);
    
    boolean save(ScoreItemTemplate template);
    
    boolean update(ScoreItemTemplate template);
    
    boolean delete(Long id);
}
