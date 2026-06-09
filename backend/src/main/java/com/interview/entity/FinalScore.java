package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 最终成绩实体
 */
@Data
@TableName("final_score")
public class FinalScore implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long candidateId;
    
    private Long positionId;
    
    private BigDecimal writtenScore;
    
    private BigDecimal interviewRawScore;
    
    private BigDecimal interviewScore;
    
    private BigDecimal totalScore;
    
    private Integer positionRank;
    
    private Integer overallRank;
    
    private Integer isPass;
    
    private Integer publishStatus;
    
    private LocalDateTime publishTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
