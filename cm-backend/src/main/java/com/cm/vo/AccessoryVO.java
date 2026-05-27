package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessoryVO {
    private Long id;
    private String barcode;
    private String name;
    private String spec;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private String remark;
    private Integer availableQty;
    private LocalDateTime createTime;
}