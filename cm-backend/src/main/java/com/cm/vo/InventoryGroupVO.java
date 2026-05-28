package com.cm.vo;

import lombok.Data;

@Data
public class InventoryGroupVO {
    private String barcode;
    private Long categoryId;
    private String categoryName;
    private Integer totalCount;
    private Integer availableCount;
}