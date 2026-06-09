package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 面试项目实体
 */
@Data
@TableName("interview_project")
public class InterviewProject implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String projectName;
    
    private String projectCode;
    
    private String organizer;
    
    private LocalDate interviewDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private String location;
    
    private String description;
    
    private BigDecimal writtenWeight;
    
    private BigDecimal interviewWeight;
    
    private Integer removeHighest;
    
    private Integer removeLowest;
    
    private Integer scorePrecision;
    
    private Integer status;
    
    private Long createUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
