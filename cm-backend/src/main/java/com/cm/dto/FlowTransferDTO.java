package com.cm.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlowTransferDTO {
    private Long fromWorkerId;
    private Long toWorkerId;
    private List<Long> accessoryIds;
    private String remark;
}