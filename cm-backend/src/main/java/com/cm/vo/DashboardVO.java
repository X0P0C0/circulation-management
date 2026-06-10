package com.cm.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private Long totalAccessories;
    private Long inStockCount;
    private Long outStockCount;
    private Long soldCount;
    private Long workerCount;
    private Long categoryCount;
    private List<FlowRecordVO> recentFlows;
    private List<Map<String, Object>> categoryDistribution;
}