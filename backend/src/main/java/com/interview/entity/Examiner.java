package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考官实体
 */
@Data
@TableName("examiner")
public class Examiner implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long userId;
    
    private String examinerName;
    
    private String examinerCode;
    
    private String phone;
    
    private String organization;
    
    private String title;
    
    private String expertise;
    
    private Long roomId;
    
    private Integer seatNo;
    
    private Integer isChief;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
