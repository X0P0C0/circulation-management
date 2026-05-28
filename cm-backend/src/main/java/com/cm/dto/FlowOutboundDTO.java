package com.cm.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlowOutboundDTO {
    private Long workerId;
    private List<Long> accessoryIds;
    private String remark;
}