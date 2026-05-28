package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccessoryVO {
    private Long id;
    private String barcode;
    private String itemCode;
    private Long categoryId;
    private String categoryName;
    private String remark;
    private Integer status;
    private String statusDesc;
    private Long workerId;
    private String workerName;
    private String relatedItemCode;
    private Integer isHighValue;
    private LocalDateTime createTime;
}