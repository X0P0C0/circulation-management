package com.cm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FlowSellDTO {
    @NotBlank(message = "条码不能为空")
    private String barcode;
    private String customerName;
    private String customerPhone;
    private String remark;
}