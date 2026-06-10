package com.cm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sort;
    /** 父分类ID，null=顶级分类 */
    private Long parentId;
    @TableField(exist = false)
    private String parentName;
    /** 专用号 */
    private String partNumber;
    @TableField(exist = false)
    private String fullPath;
    private String remark;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}