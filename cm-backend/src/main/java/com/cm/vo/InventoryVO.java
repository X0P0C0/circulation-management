package com.cm.vo;

import lombok.Data;

@Data
public class InventoryVO {
    private Long accessoryId;
    private String barcode;
    private String accessoryName;
    private String spec;
    private String categoryName;
    private String unit;
    private Integer totalQty;
    private Integer availableQty;
}