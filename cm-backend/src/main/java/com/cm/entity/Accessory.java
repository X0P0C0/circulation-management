package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accessory")
public class Accessory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String barcode;
    private String name;
    private String spec;
    private Long categoryId;
    private String unit;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}