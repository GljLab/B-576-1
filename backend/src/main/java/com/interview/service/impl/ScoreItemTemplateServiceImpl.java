package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.entity.ScoreItemTemplate;
import com.interview.entity.ScoreItemTemplateDetail;
import com.interview.mapper.ScoreItemTemplateDetailMapper;
import com.interview.mapper.ScoreItemTemplateMapper;
import com.interview.service.ScoreItemTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScoreItemTemplateServiceImpl implements ScoreItemTemplateService {
    
    @Autowired
    private ScoreItemTemplateMapper templateMapper;
    
    @Autowired
    private ScoreItemTemplateDetailMapper detailMapper;
    
    @Override
    public List<ScoreItemTemplate> list(Integer isSystem) {
        LambdaQueryWrapper<ScoreItemTemplate> wrapper = new LambdaQueryWrapper<>();
        if (isSystem != null) {
            wrapper.eq(ScoreItemTemplate::getIsSystem, isSystem);
        }
        wrapper.eq(ScoreItemTemplate::getStatus, 1);
        wrapper.orderByDesc(ScoreItemTemplate::getIsSystem);
        wrapper.orderByAsc(ScoreItemTemplate::getCreateTime);
        return templateMapper.selectList(wrapper);
    }
    
    @Override
    public ScoreItemTemplate getDetail(Long id) {
        ScoreItemTemplate template = templateMapper.selectById(id);
        if (template != null) {
            List<ScoreItemTemplateDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<ScoreItemTemplateDetail>()
                            .eq(ScoreItemTemplateDetail::getTemplateId, id)
                            .orderByAsc(ScoreItemTemplateDetail::getSortOrder));
            template.setDetails(details);
        }
        return template;
    }
    
    @Override
    @Transactional
    public boolean save(ScoreItemTemplate template) {
        template.setItemCount(template.getDetails() != null ? template.getDetails().size() : 0);
        int result = templateMapper.insert(template);
        if (result > 0 && template.getDetails() != null) {
            for (ScoreItemTemplateDetail detail : template.getDetails()) {
                detail.setTemplateId(template.getId());
                detailMapper.insert(detail);
            }
        }
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean update(ScoreItemTemplate template) {
        template.setItemCount(template.getDetails() != null ? template.getDetails().size() : 0);
        int result = templateMapper.updateById(template);
        if (result > 0) {
            detailMapper.delete(new LambdaQueryWrapper<ScoreItemTemplateDetail>()
                    .eq(ScoreItemTemplateDetail::getTemplateId, template.getId()));
            if (template.getDetails() != null) {
                for (ScoreItemTemplateDetail detail : template.getDetails()) {
                    detail.setTemplateId(template.getId());
                    detailMapper.insert(detail);
                }
            }
        }
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        detailMapper.delete(new LambdaQueryWrapper<ScoreItemTemplateDetail>()
                .eq(ScoreItemTemplateDetail::getTemplateId, id));
        return templateMapper.deleteById(id) > 0;
    }
}
