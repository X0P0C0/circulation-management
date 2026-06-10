package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("flow_record")
public class FlowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accessoryId;
    private String itemCode;
    private String barcode;
    /** 1=入库 2=出库 3=归还(完成) 4=归还(取消-退回) 5=归还(取消-寄回厂家) 6=售卖 7=转移 */
    private Integer flowType;
    private Long fromWorkerId;
    private String fromWorkerName;
    private Long toWorkerId;
    private String toWorkerName;
    private BigDecimal price;
    private String customerName;
    private String customerPhone;
    private String remark;
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}