package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.common.result.PageResult;
import com.cm.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void log(String actionType, String content, String barcode, Long workerId, String operator, String ip);
    PageResult<OperationLog> listPage(String actionType, String operator, String barcode,
                                      String startDate, String endDate,
                                      String sortFields, String sortOrders,
                                      Integer pageNum, Integer pageSize);
}
