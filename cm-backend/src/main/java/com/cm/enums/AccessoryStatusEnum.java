package com.cm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccessoryStatusEnum {
    IN_STOCK(1, "在库"),
    OUTBOUND(2, "已出库"),
    COMPLETED(3, "已完成"),
    RETURNED_FACTORY(4, "寄回厂家"),
    OLD_PART(5, "旧件待返厂"),
    SOLD(6, "已售卖");

    private final int code;
    private final String desc;

    public static AccessoryStatusEnum of(int code) {
        for (AccessoryStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        return IN_STOCK;
    }
}