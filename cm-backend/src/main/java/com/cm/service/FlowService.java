package com.cm.service;

import com.cm.common.result.PageResult;
import com.cm.dto.FlowSellDTO;
import com.cm.dto.FlowTransferDTO;
import com.cm.vo.FlowTraceVO;
import java.util.List;

public interface FlowService {
    void transferOut(FlowTransferDTO dto, String operator);
    void transferIn(FlowTransferDTO dto, String operator);
    void sell(FlowSellDTO dto, String operator);
    FlowTraceVO trace(String barcode);
    PageResult<?> listRecords(Integer flowType, Long workerId, String keyword,
                              String startDate, String endDate,
                              Integer pageNum, Integer pageSize);
    List<?> workerRecords(Long workerId);
}