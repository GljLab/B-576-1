package com.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interview.entity.*;
import com.interview.mapper.*;
import com.interview.service.DrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽签服务实现
 */
@Service
public class DrawServiceImpl implements DrawService {
    
    @Autowired
    private DrawRecordMapper drawRecordMapper;
    
    @Autowired
    private ExaminerMapper examinerMapper;
    
    @Autowired
    private CandidateMapper candidateMapper;
    
    @Autowired
    private PositionMapper positionMapper;
    
    @Autowired
    private ExamRoomMapper examRoomMapper;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Override
    @Transactional
    public List<DrawRecord> drawExaminers(Long projectId, Long operatorId, String operatorName) {
        String batch = generateBatch("EXAMINER");
        
        // 获取所有考官
        List<Examiner> examiners = examinerMapper.selectList(
                new LambdaQueryWrapper<Examiner>()
                        .eq(Examiner::getProjectId, projectId)
                        .eq(Examiner::getStatus, 1));
        
        // 获取所有考场
        List<ExamRoom> rooms = examRoomMapper.selectList(
                new LambdaQueryWrapper<ExamRoom>()
                        .eq(ExamRoom::getProjectId, projectId)
                        .eq(ExamRoom::getStatus, 1));
        
        if (examiners.isEmpty() || rooms.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 打乱考官顺序
        Collections.shuffle(examiners, secureRandom);
        
        List<DrawRecord> records = new ArrayList<>();
        int examinerIndex = 0;
        
        // 为每个考场分配考官
        for (ExamRoom room : rooms) {
            int count = room.getExaminerCount();
            for (int seatNo = 1; seatNo <= count && examinerIndex < examiners.size(); seatNo++) {
                Examiner examiner = examiners.get(examinerIndex++);
                
                // 更新考官的考场和座位号
                examiner.setRoomId(room.getId());
                examiner.setSeatNo(seatNo);
                examiner.setIsChief(seatNo == 1 ? 1 : 0); // 第一个座位为主考官
                examinerMapper.updateById(examiner);
                
                // 创建抽签记录
                DrawRecord record = new DrawRecord();
                record.setProjectId(projectId);
                record.setDrawType("EXAMINER");
                record.setDrawBatch(batch);
                record.setTargetId(examiner.getId());
                record.setTargetName(examiner.getExaminerName());
                record.setOriginalInfo("考官编号: " + examiner.getExaminerCode());
                record.setResultInfo("分配至: " + room.getRoomName() + ", 座位号: " + seatNo);
                record.setResultOrder(seatNo);
                record.setRoomId(room.getId());
                record.setDrawTime(LocalDateTime.now());
                record.setOperatorId(operatorId);
                record.setOperatorName(operatorName);
                record.setIsLocked(1);
                
                drawRecordMapper.insert(record);
                records.add(record);
            }
        }
        
        return records;
    }
    
    @Override
    @Transactional
    public List<DrawRecord> drawCandidates(Long projectId, Long operatorId, String operatorName) {
        String batch = generateBatch("CANDIDATE");
        
        // 获取所有有效考生（不限制签到状态，支持预先抽签）
        List<Candidate> candidates = candidateMapper.selectList(
                new LambdaQueryWrapper<Candidate>()
                        .eq(Candidate::getProjectId, projectId)
                        .eq(Candidate::getStatus, 1));
        
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 按职位分组
        Map<Long, List<Candidate>> candidatesByPosition = candidates.stream()
                .collect(Collectors.groupingBy(Candidate::getPositionId));
        
        List<DrawRecord> records = new ArrayList<>();
        
        for (Map.Entry<Long, List<Candidate>> entry : candidatesByPosition.entrySet()) {
            List<Candidate> positionCandidates = entry.getValue();
            
            // 打乱顺序
            Collections.shuffle(positionCandidates, secureRandom);
            
            // 分配面试顺序
            for (int i = 0; i < positionCandidates.size(); i++) {
                Candidate candidate = positionCandidates.get(i);
                int order = i + 1;
                
                // 更新考生面试顺序
                candidate.setInterviewOrder(order);
                candidateMapper.updateById(candidate);
                
                // 创建抽签记录
                DrawRecord record = new DrawRecord();
                record.setProjectId(projectId);
                record.setDrawType("CANDIDATE");
                record.setDrawBatch(batch);
                record.setTargetId(candidate.getId());
                record.setTargetName(candidate.getCandidateName());
                record.setOriginalInfo("准考证号: " + candidate.getTicketNo());
                record.setResultInfo("面试顺序: 第" + order + "号");
                record.setResultOrder(order);
                record.setDrawTime(LocalDateTime.now());
                record.setOperatorId(operatorId);
                record.setOperatorName(operatorName);
                record.setIsLocked(1);
                
                drawRecordMapper.insert(record);
                records.add(record);
            }
        }
        
        return records;
    }
    
    @Override
    @Transactional
    public List<DrawRecord> drawPositionOrder(Long projectId, Long operatorId, String operatorName) {
        String batch = generateBatch("POSITION");
        
        // 获取所有职位
        List<Position> positions = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getProjectId, projectId)
                        .eq(Position::getStatus, 1));
        
        if (positions.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 打乱顺序
        Collections.shuffle(positions, secureRandom);
        
        List<DrawRecord> records = new ArrayList<>();
        
        for (int i = 0; i < positions.size(); i++) {
            Position position = positions.get(i);
            int order = i + 1;
            
            // 更新职位面试顺序
            position.setInterviewOrder(order);
            positionMapper.updateById(position);
            
            // 创建抽签记录
            DrawRecord record = new DrawRecord();
            record.setProjectId(projectId);
            record.setDrawType("POSITION");
            record.setDrawBatch(batch);
            record.setTargetId(position.getId());
            record.setTargetName(position.getPositionName());
            record.setOriginalInfo("职位编码: " + position.getPositionCode());
            record.setResultInfo("面试顺序: 第" + order + "顺位");
            record.setResultOrder(order);
            record.setDrawTime(LocalDateTime.now());
            record.setOperatorId(operatorId);
            record.setOperatorName(operatorName);
            record.setIsLocked(1);
            
            drawRecordMapper.insert(record);
            records.add(record);
        }
        
        return records;
    }
    
    @Override
    @Transactional
    public Map<String, Object> executeTripleBlindDraw(Long projectId, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 考官抽签
        List<DrawRecord> examinerRecords = drawExaminers(projectId, operatorId, operatorName);
        result.put("examinerRecords", examinerRecords);
        
        // 2. 考生抽签
        List<DrawRecord> candidateRecords = drawCandidates(projectId, operatorId, operatorName);
        result.put("candidateRecords", candidateRecords);
        
        // 3. 职位顺序抽签
        List<DrawRecord> positionRecords = drawPositionOrder(projectId, operatorId, operatorName);
        result.put("positionRecords", positionRecords);
        
        result.put("success", true);
        result.put("message", "三盲抽签完成");
        
        return result;
    }
    
    @Override
    public List<DrawRecord> getDrawResults(Long projectId, String drawType) {
        LambdaQueryWrapper<DrawRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrawRecord::getProjectId, projectId);
        
        if (drawType != null && !drawType.isEmpty()) {
            wrapper.eq(DrawRecord::getDrawType, drawType);
        }
        
        wrapper.orderByAsc(DrawRecord::getResultOrder);
        
        return drawRecordMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public boolean resetDraw(Long projectId, String drawType) {
        // 删除抽签记录
        drawRecordMapper.delete(
                new LambdaQueryWrapper<DrawRecord>()
                        .eq(DrawRecord::getProjectId, projectId)
                        .eq(DrawRecord::getDrawType, drawType));
        
        // 重置相关数据
        switch (drawType) {
            case "EXAMINER":
                List<Examiner> examiners = examinerMapper.selectList(
                        new LambdaQueryWrapper<Examiner>().eq(Examiner::getProjectId, projectId));
                for (Examiner examiner : examiners) {
                    examiner.setRoomId(null);
                    examiner.setSeatNo(null);
                    examiner.setIsChief(0);
                    examinerMapper.updateById(examiner);
                }
                break;
            case "CANDIDATE":
                List<Candidate> candidates = candidateMapper.selectList(
                        new LambdaQueryWrapper<Candidate>().eq(Candidate::getProjectId, projectId));
                for (Candidate candidate : candidates) {
                    candidate.setInterviewOrder(null);
                    candidateMapper.updateById(candidate);
                }
                break;
            case "POSITION":
                List<Position> positions = positionMapper.selectList(
                        new LambdaQueryWrapper<Position>().eq(Position::getProjectId, projectId));
                for (Position position : positions) {
                    position.setInterviewOrder(null);
                    positionMapper.updateById(position);
                }
                break;
        }
        
        return true;
    }
    
    /**
     * 生成抽签批次号
     */
    private String generateBatch(String type) {
        return type + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
