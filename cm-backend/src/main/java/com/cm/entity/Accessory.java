package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accessory")
public class Accessory {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 配件条码（可重复，同型号共享） */
    private String barcode;
    /** 工件业务编号（唯一，系统生成） */
    private String itemCode;
    private Long categoryId;
    private String remark;
    /** 状态：1=在库 2=已出库 3=已完成 4=寄回厂家 5=旧件待返厂 6=已售卖 */
    private Integer status;
    /** 当前持有师傅ID（已出库时有值） */
    private Long workerId;
    /** 关联新工件编号（旧件专用） */
    private String relatedItemCode;
    /** 1=高价值 0=低价值（退件时） */
    private Integer isHighValue;
    @Version
    private Integer version;
    /** 货架ID */
    private Long shelfId;
    @TableField(exist = false)
    private String shelfName;
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}