package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.entity.*;
import com.interview.mapper.*;
import com.interview.service.ScoreItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreItemServiceImpl implements ScoreItemService {
    
    @Autowired
    private ScoreItemMapper scoreItemMapper;
    
    @Autowired
    private ScoreItemTemplateMapper templateMapper;
    
    @Autowired
    private ScoreItemTemplateDetailMapper detailMapper;
    
    @Autowired
    private ExaminerScoreMapper examinerScoreMapper;
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Override
    public List<ScoreItem> getByProjectId(Long projectId) {
        return scoreItemMapper.selectList(
                new LambdaQueryWrapper<ScoreItem>()
                        .eq(ScoreItem::getProjectId, projectId)
                        .eq(ScoreItem::getStatus, 1)
                        .orderByAsc(ScoreItem::getSortOrder));
    }
    
    @Override
    public ScoreItem getById(Long id) {
        return scoreItemMapper.selectById(id);
    }
    
    @Override
    public boolean save(ScoreItem scoreItem) {
        return scoreItemMapper.insert(scoreItem) > 0;
    }
    
    @Override
    public boolean update(ScoreItem scoreItem) {
        return scoreItemMapper.updateById(scoreItem) > 0;
    }
    
    @Override
    @Transactional
    public boolean delete(Long id) {
        ScoreItem item = scoreItemMapper.selectById(id);
        if (item != null) {
            item.setStatus(0);
            return scoreItemMapper.updateById(item) > 0;
        }
        return false;
    }
    
    @Override
    @Transactional
    public boolean batchSave(List<ScoreItem> items) {
        if (items == null || items.isEmpty()) return false;
        for (ScoreItem item : items) {
            if (item.getId() != null) {
                scoreItemMapper.updateById(item);
            } else {
                scoreItemMapper.insert(item);
            }
        }
        return true;
    }
    
    @Override
    public Map<String, Object> validateWeights(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<ScoreItem> items = getByProjectId(projectId);
        BigDecimal totalWeight = items.stream()
                .map(ScoreItem::getWeight)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalWeight", totalWeight);
        result.put("itemCount", items.size());
        result.put("isValid", totalWeight.compareTo(new BigDecimal("100")) == 0);
        result.put("diff", totalWeight.subtract(new BigDecimal("100")));
        return result;
    }
    
    @Override
    @Transactional
    public boolean updateSortOrder(Long projectId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return false;
        for (int i = 0; i < itemIds.size(); i++) {
            ScoreItem item = new ScoreItem();
            item.setId(itemIds.get(i));
            item.setSortOrder(i + 1);
            scoreItemMapper.updateById(item);
        }
        return true;
    }
    
    @Override
    @Transactional
    public boolean applyTemplate(Long projectId, Long templateId, boolean overwrite) {
        ScoreItemTemplate template = templateMapper.selectById(templateId);
        if (template == null) return false;
        
        if (overwrite) {
            scoreItemMapper.delete(new LambdaQueryWrapper<ScoreItem>()
                    .eq(ScoreItem::getProjectId, projectId));
        }
        
        List<ScoreItemTemplateDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<ScoreItemTemplateDetail>()
                        .eq(ScoreItemTemplateDetail::getTemplateId, templateId)
                        .orderByAsc(ScoreItemTemplateDetail::getSortOrder));
        
        int currentMax = 0;
        if (!overwrite) {
            List<ScoreItem> existing = getByProjectId(projectId);
            if (!existing.isEmpty()) {
                currentMax = existing.stream()
                        .mapToInt(ScoreItem::getSortOrder)
                        .max()
                        .orElse(0);
            }
        }
        
        for (ScoreItemTemplateDetail detail : details) {
            ScoreItem item = new ScoreItem();
            item.setProjectId(projectId);
            item.setItemName(detail.getItemName());
            item.setItemCode(detail.getItemCode());
            item.setMaxScore(detail.getMaxScore());
            item.setWeight(detail.getWeight());
            item.setDescription(detail.getDescription());
            item.setSortOrder(currentMax + detail.getSortOrder());
            item.setStatus(1);
            scoreItemMapper.insert(item);
        }
        return true;
    }
    
    @Override
    public Map<String, Object> preview(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<ScoreItem> items = getByProjectId(projectId);
        result.put("items", items);
        result.put("weightValidation", validateWeights(projectId));
        
        BigDecimal totalMaxWeighted = items.stream()
                .map(item -> {
                    BigDecimal max = item.getMaxScore() != null ? item.getMaxScore() : BigDecimal.ZERO;
                    BigDecimal weight = item.getWeight() != null ? item.getWeight() : BigDecimal.ZERO;
                    return max.multiply(weight).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalMaxWeighted", totalMaxWeighted);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getItemStatistics(Long projectId) {
        List<ScoreItem> items = getByProjectId(projectId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ScoreItem item : items) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("itemId", item.getId());
            stats.put("itemName", item.getItemName());
            stats.put("itemCode", item.getItemCode());
            stats.put("maxScore", item.getMaxScore());
            stats.put("weight", item.getWeight());
            
            List<ExaminerScore> itemScores = examinerScoreMapper.selectList(
                    new LambdaQueryWrapper<ExaminerScore>()
                            .eq(ExaminerScore::getProjectId, projectId)
                            .eq(ExaminerScore::getScoreItemId, item.getId())
                            .eq(ExaminerScore::getStatus, 1)
                            .eq(ExaminerScore::getIsValid, 1));
            
            List<BigDecimal> scores = itemScores.stream()
                    .map(ExaminerScore::getScore)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            
            if (!scores.isEmpty()) {
                BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = sum.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);
                stats.put("count", scores.size());
                stats.put("avgScore", avg);
                stats.put("maxScoreVal", scores.get(scores.size() - 1));
                stats.put("minScoreVal", scores.get(0));
                
                int[] distribution = new int[10];
                for (BigDecimal s : scores) {
                    int bucket = Math.min(9, s.divide(new BigDecimal("10"), 0, RoundingMode.DOWN).intValue());
                    distribution[bucket]++;
                }
                List<Map<String, Object>> distList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Map<String, Object> d = new HashMap<>();
                    d.put("range", (i * 10) + "-" + ((i + 1) * 10));
                    d.put("count", distribution[i]);
                    distList.add(d);
                }
                stats.put("distribution", distList);
            } else {
                stats.put("count", 0);
                stats.put("avgScore", BigDecimal.ZERO);
                stats.put("maxScoreVal", BigDecimal.ZERO);
                stats.put("minScoreVal", BigDecimal.ZERO);
                stats.put("distribution", new ArrayList<>());
            }
            result.add(stats);
        }
        return result;
    }
    
    @Override
    public Map<String, Object> getCandidateItemScores(Long candidateId, Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<ScoreItem> items = getByProjectId(projectId);
        result.put("items", items);
        
        List<Examiner> examiners = examinerMapper.selectList(
                new LambdaQueryWrapper<Examiner>()
                        .eq(Examiner::getProjectId, projectId));
        result.put("examiners", examiners);
        
        List<ExaminerScore> allScores = examinerScoreMapper.selectList(
                new LambdaQueryWrapper<ExaminerScore>()
                        .eq(ExaminerScore::getCandidateId, candidateId)
                        .eq(ExaminerScore::getProjectId, projectId)
                        .eq(ExaminerScore::getStatus, 1));
        
        Map<Long, Map<Long, ExaminerScore>> examinerItemScores = new HashMap<>();
        ExaminerScore summaryScore = null;
        
        for (ExaminerScore es : allScores) {
            if (es.getScoreItemId() == null) {
                summaryScore = es;
            } else {
                examinerItemScores
                        .computeIfAbsent(es.getExaminerId(), k -> new HashMap<>())
                        .put(es.getScoreItemId(), es);
            }
        }
        
        result.put("summaryScore", summaryScore);
        result.put("examinerItemScores", examinerItemScores);
        
        return result;
    }
}
