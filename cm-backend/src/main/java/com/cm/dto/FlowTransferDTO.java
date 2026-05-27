package com.cm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FlowTransferDTO {
    @NotNull(message = "请选择师傅")
    private Long workerId;
    @NotEmpty(message = "条码列表不能为空")
    private List<String> barcodes;
    private String remark;
}