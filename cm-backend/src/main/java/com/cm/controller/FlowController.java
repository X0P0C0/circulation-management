package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.FlowSellDTO;
import com.cm.dto.FlowTransferDTO;
import com.cm.service.FlowService;
import com.cm.vo.FlowTraceVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flows")
@RequiredArgsConstructor
public class FlowController {

    private final FlowService flowService;

    @PostMapping("/transfer-out")
    public Result<Void> transferOut(@Valid @RequestBody FlowTransferDTO dto, HttpServletRequest request) {
        flowService.transferOut(dto, (String) request.getAttribute(Constants.USERNAME_ATTR));
        return Result.success();
    }

    @PostMapping("/transfer-in")
    public Result<Void> transferIn(@Valid @RequestBody FlowTransferDTO dto, HttpServletRequest request) {
        flowService.transferIn(dto, (String) request.getAttribute(Constants.USERNAME_ATTR));
        return Result.success();
    }

    @PostMapping("/sell")
    public Result<Void> sell(@Valid @RequestBody FlowSellDTO dto, HttpServletRequest request) {
        flowService.sell(dto, (String) request.getAttribute(Constants.USERNAME_ATTR));
        return Result.success();
    }

    @GetMapping("/trace/{barcode}")
    public Result<FlowTraceVO> trace(@PathVariable String barcode) {
        return Result.success(flowService.trace(barcode));
    }

    @GetMapping("/records")
    public Result<PageResult<?>> records(
            @RequestParam(required = false) Integer flowType,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(flowService.listRecords(flowType, workerId, keyword, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/workers/{workerId}/records")
    public Result<?> workerRecords(@PathVariable Long workerId) {
        return Result.success(flowService.workerRecords(workerId));
    }
}