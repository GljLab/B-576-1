package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("score_item_template")
public class ScoreItemTemplate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String templateName;
    
    private String templateCode;
    
    private String description;
    
    private Integer itemCount;
    
    private Integer isSystem;
    
    private Integer status;
    
    private Long createUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private List<ScoreItemTemplateDetail> details;
}
