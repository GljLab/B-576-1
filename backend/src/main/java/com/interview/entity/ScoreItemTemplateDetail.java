package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_item_template_detail")
public class ScoreItemTemplateDetail implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long templateId;
    
    private String itemName;
    
    private String itemCode;
    
    private BigDecimal maxScore;
    
    private BigDecimal weight;
    
    private String description;
    
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
