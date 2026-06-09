package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考生实体
 */
@Data
@TableName("candidate")
public class Candidate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long positionId;
    
    private String candidateName;
    
    private String idCard;
    
    private String ticketNo;
    
    private String phone;
    
    private Integer gender;
    
    private String photo;
    
    private BigDecimal writtenScore;
    
    private Integer interviewOrder;
    
    private Long roomId;
    
    private LocalDateTime checkInTime;
    
    private Integer checkInStatus;
    
    private Integer interviewStatus;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
