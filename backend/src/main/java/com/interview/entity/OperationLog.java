package com.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long userId;
    
    private String username;
    
    private String operationType;
    
    private String operationDesc;
    
    private String module;
    
    private String method;
    
    private String requestUrl;
    
    private String requestParams;
    
    private String responseData;
    
    private String ipAddress;
    
    private String userAgent;
    
    private Long executionTime;
    
    private Integer status;
    
    private String errorMsg;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
