package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLogVO {
    private Long id;
    private String actionType;
    private String content;
    private Long accessoryId;
    private String accessoryIds;
    private Long relatedCategoryId;
    private Long relatedWorkerId;
    private String operator;
    private String ip;
    private LocalDateTime createTime;
    // JOIN fields
    private String barcode;
    private String itemCode;
    private String categoryName;
    private String parentCategoryName;
    private String partNumber;
    private String workerName;
}