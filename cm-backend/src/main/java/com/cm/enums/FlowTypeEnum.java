package com.cm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlowTypeEnum {
    INBOUND(1, "入库"),
    OUTBOUND(2, "出库"),
    RETURN_COMPLETE(3, "归还(工单完成)"),
    RETURN_CANCEL_LOW(4, "归还(取消-退回)"),
    RETURN_CANCEL_HIGH(5, "归还(取消-寄回厂家)"),
    SELL(6, "售卖"),
    TRANSFER(7, "转移"),
    DELETE(8, "删除");

    private final int code;
    private final String desc;

    public static FlowTypeEnum of(int code) {
        for (FlowTypeEnum f : values()) {
            if (f.code == code) return f;
        }
        return INBOUND;
    }
}