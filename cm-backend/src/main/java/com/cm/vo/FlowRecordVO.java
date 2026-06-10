package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class FlowRecordVO {
    private Long id;
    private Long accessoryId;
    private String itemCode;
    private String barcode;
    private Integer flowType;
    private Long fromWorkerId;
    private String fromWorkerName;
    private Long toWorkerId;
    private String toWorkerName;
    private String customerName;
    private String remark;
    private BigDecimal price;
    private String customerPhone;
    private String operator;
    private LocalDateTime createTime;
}