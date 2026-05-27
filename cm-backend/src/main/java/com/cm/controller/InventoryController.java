package com.cm.controller;

import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.service.InventoryService;
import com.cm.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<InventoryVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inventoryService.listPage(categoryId, keyword, pageNum, pageSize));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(inventoryService.getStats());
    }
}