package com.cm.dto;

import lombok.Data;
import java.util.List;

@Data
public class FlowSellDTO {
    private List<SellItemDTO> items;
    private String customerName;
    private String customerPhone;
    private String remark;
}