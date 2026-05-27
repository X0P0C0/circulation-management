package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_owner")
public class InventoryOwner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accessoryId;
    private Long workerId;
    private Integer qty;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}