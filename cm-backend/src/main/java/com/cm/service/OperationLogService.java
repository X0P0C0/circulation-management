package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.common.result.PageResult;
import com.cm.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void log(String actionType, String content, String barcode, Long workerId, String operator, String ip);
    PageResult<OperationLog> listPage(String actionType, String startDate, String endDate,
                                       Integer pageNum, Integer pageSize);
}