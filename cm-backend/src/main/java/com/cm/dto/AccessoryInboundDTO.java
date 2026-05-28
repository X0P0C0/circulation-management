package com.cm.dto;

import lombok.Data;

@Data
public class AccessoryInboundDTO {
    private String barcode;
    private Long categoryId;
    private String remark;
}