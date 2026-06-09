package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考官评分实体
 */
@Data
@TableName("examiner_score")
public class ExaminerScore implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long candidateId;
    
    private Long examinerId;
    
    private Long roomId;
    
    private Long scoreItemId;
    
    private BigDecimal score;
    
    private BigDecimal totalScore;
    
    private String comment;
    
    private Integer isValid;
    
    private LocalDateTime submitTime;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
