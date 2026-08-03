package com.cm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cm.common.result.PageResult;
import com.cm.entity.OperationLog;
import com.cm.vo.OperationLogVO;

public interface OperationLogService extends IService<OperationLog> {
    void log(String actionType, String content, Long accessoryId, String accessoryIds, Long categoryId, Long workerId, String operator, String ip);
    PageResult<OperationLogVO> listPage(String actionType, String operator, String barcode,
                                      String startDate, String endDate,
                                      String sortFields, String sortOrders,
                                      Integer pageNum, Integer pageSize);
}
