package com.cm.dto;

import lombok.Data;

@Data
public class AccessoryUpdateDTO {
    private Long id;
    private String barcode;
    private Long categoryId;
    private String remark;
    private Integer isHighValue;
    private Long shelfId;
}
