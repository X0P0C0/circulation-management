package com.cm.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlowReturnDTO {
    /** 归还类型：1=工单完成(换件) 2=工单取消 */
    private Integer returnType;
    /** 工单取消时：是否高价值 */
    private Boolean highValue;
    /** 要归还的工件ID列表 */
    private List<Long> accessoryIds;
    private String remark;
}