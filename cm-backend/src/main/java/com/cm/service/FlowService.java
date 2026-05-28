package com.cm.service;

import com.cm.common.result.PageResult;
import com.cm.dto.*;
import com.cm.vo.FlowTraceVO;
import java.util.List;

public interface FlowService {
    /** 出库（批量分配给师傅） */
    void outbound(FlowOutboundDTO dto, String operator);

    /** 归还 */
    void returnItem(FlowReturnDTO dto, String operator);

    /** 售卖 */
    void sell(FlowSellDTO dto, String operator);

    /** 师傅间转移 */
    void transfer(FlowTransferDTO dto, String operator);

    /** 工件追溯 */
    FlowTraceVO trace(String itemCode);

    /** 流转记录查询 */
    PageResult<?> listRecords(Integer flowType, Long workerId, String keyword,
                              Integer pageNum, Integer pageSize);
}