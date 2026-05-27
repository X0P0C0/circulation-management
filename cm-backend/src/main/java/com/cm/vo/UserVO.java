package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private Integer role;
    private String roleName;
    private Integer status;
    private LocalDateTime createTime;
}