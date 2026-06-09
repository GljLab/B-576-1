package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽签记录实体
 */
@Data
@TableName("draw_record")
public class DrawRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String drawType;
    
    private String drawBatch;
    
    private Long targetId;
    
    private String targetName;
    
    private String originalInfo;
    
    private String resultInfo;
    
    private Integer resultOrder;
    
    private Long roomId;
    
    private LocalDateTime drawTime;
    
    private Long operatorId;
    
    private String operatorName;
    
    private Integer isLocked;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
