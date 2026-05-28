package com.cm.vo;

import lombok.Data;
import java.util.List;

@Data
public class FlowTraceVO {
    private String itemCode;
    private String barcode;
    private Long categoryId;
    private String categoryName;
    private Integer currentStatus;
    private String currentStatusDesc;
    private String currentHolder;
    private List<FlowStep> steps;

    @Data
    public static class FlowStep {
        private Long id;
        private Integer flowType;
        private String flowTypeDesc;
        private String fromWorkerName;
        private String toWorkerName;
        private String customerName;
        private String operator;
        private String remark;
        private String createTime;
    }
}