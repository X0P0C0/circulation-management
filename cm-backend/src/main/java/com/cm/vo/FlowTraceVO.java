package com.cm.vo;

import lombok.Data;
import java.util.List;

@Data
public class FlowTraceVO {
    private String barcode;
    private String accessoryName;
    private Integer currentQty;
    private String currentHolder;
    private List<FlowStep> steps;

    @Data
    public static class FlowStep {
        private Long id;
        private Integer flowType;
        private String flowTypeDesc;
        private String workerName;
        private String customerName;
        private String operator;
        private String remark;
        private String createTime;
    }
}