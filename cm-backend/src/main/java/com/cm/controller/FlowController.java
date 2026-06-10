package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.*;
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

    @PostMapping("/outbound")
    public Result<Void> outbound(@Valid @RequestBody FlowOutboundDTO dto,
                                  HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        flowService.outbound(dto, operator);
        return Result.ok();
    }

    @PostMapping("/return")
    public Result<Void> returnItem(@Valid @RequestBody FlowReturnDTO dto,
                                    HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        flowService.returnItem(dto, operator);
        return Result.ok();
    }

    @PostMapping("/sell")
    public Result<Void> sell(@Valid @RequestBody FlowSellDTO dto,
                              HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        flowService.sell(dto, operator);
        return Result.ok();
    }

    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody FlowTransferDTO dto,
                                  HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        flowService.transfer(dto, operator);
        return Result.ok();
    }

    @GetMapping("/trace/{itemCode}")
    public Result<FlowTraceVO> trace(@PathVariable String itemCode) {
        return Result.ok(flowService.trace(itemCode));
    }

    @GetMapping("/trace")
    public Result<FlowTraceVO> traceByBarcode(@RequestParam String barcode) {
        return Result.ok(flowService.traceByBarcode(barcode));
    }

    @GetMapping("/records")
    public Result<PageResult<?>> records(
            @RequestParam(required = false) Integer flowType,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(flowService.listRecords(flowType, workerId, keyword, pageNum, pageSize));
    }
}