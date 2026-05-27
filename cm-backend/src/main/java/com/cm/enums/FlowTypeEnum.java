package com.cm.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum FlowTypeEnum {
    INBOUND(1, "入库"),
    TRANSFER_OUT(2, "领用"),
    TRANSFER_IN(3, "归还"),
    SELL(4, "售卖");

    private final int code;
    private final String desc;

    public static FlowTypeEnum of(int code) {
        for (FlowTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("无效的流转类型: " + code);
    }
}