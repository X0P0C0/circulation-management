package com.cm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccessoryInboundDTO {
    @NotBlank(message = "条码不能为空")
    private String barcode;
    @NotBlank(message = "配件名称不能为空")
    private String name;
    private String spec;
    @NotNull(message = "请选择分类")
    private Long categoryId;
    private String unit;
    private String remark;
}