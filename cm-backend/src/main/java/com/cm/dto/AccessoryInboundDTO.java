package com.cm.dto;

import lombok.Data;

@Data
public class AccessoryInboundDTO {
    private String barcode;
    private Long categoryId;
    private Long shelfId;
    private String remark;
    /** 入库数量，默认1 */
    private Integer quantity = 1;
}