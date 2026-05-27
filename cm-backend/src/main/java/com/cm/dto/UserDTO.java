package com.cm.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String realName;
    /** 角色：1=管理员 2=操作员 */
    private Integer role;
}