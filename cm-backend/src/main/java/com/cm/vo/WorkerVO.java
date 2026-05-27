package com.cm.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
 public class WorkerVO {
    private Long id;
    private String name;
    private String jobNo;
    private String phone;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
}