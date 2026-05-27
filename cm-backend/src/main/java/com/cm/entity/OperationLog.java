package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String actionType;
    private String content;
    private String relatedBarcode;
    private Long relatedWorkerId;
    private String operator;
    private String ip;
    private LocalDateTime createTime;
}