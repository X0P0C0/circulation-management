package com.cm.controller;

import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.entity.OperationLog;
import com.cm.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService logService;

    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sortFields,
            @RequestParam(required = false) String sortOrders,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(logService.listPage(actionType, operator, barcode, startDate, endDate, sortFields, sortOrders, pageNum, pageSize));
    }
}
