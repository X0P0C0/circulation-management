package com.cm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {

    ADMIN(1, "管理员"),
    OPERATOR(2, "操作员");

    private final int code;
    private final String desc;

    public static RoleEnum of(int code) {
        for (RoleEnum r : values()) {
            if (r.code == code) return r;
        }
        return OPERATOR;
    }
}