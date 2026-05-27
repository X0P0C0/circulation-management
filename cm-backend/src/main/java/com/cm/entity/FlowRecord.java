package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flow_record")
public class FlowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accessoryId;
    private String barcode;
    private String accessoryName;
    private Integer flowType;
    private Long workerId;
    private String workerName;
    private String customerName;
    private String customerPhone;
    private Integer qty;
    private String remark;
    private String operator;
    private LocalDateTime createTime;
}