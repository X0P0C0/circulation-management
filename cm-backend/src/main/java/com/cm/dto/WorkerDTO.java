package com.cm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkerDTO {
    @NotBlank(message = "师傅姓名不能为空")
    private String name;
    private String jobNo;
    private String phone;
    private String remark;
}