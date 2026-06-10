package com.cm.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellItemDTO {
    private Long accessoryId;
    private BigDecimal price;
}