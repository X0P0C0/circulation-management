package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.service.AccessoryService;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accessories")
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @PostMapping("/inbound")
    public Result<AccessoryVO> inbound(@Valid @RequestBody AccessoryInboundDTO dto,
                                        HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        return Result.ok(accessoryService.inbound(dto, operator));
    }

    @GetMapping("/{id}")
    public Result<AccessoryVO> getById(@PathVariable Long id) {
        return Result.ok(accessoryService.findById(id));
    }

    @GetMapping("/barcode/{barcode}")
    public Result<AccessoryVO> getByBarcode(@PathVariable String barcode) {
        return Result.ok(accessoryService.findByBarcode(barcode));
    }

    /** 统一搜索接口 */
    @GetMapping("/search")
    public Result<PageResult<AccessoryVO>> search(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(accessoryService.search(barcode, categoryId, status, workerId,
                keyword, sortField, sortOrder, pageNum, pageSize));
    }

    /** 按条码分组的库存视图 */
    @GetMapping("/inventory-group")
    public Result<PageResult<InventoryGroupVO>> inventoryGroup(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer statusFilter,
            @RequestParam(required = false) Long workerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(accessoryService.inventoryGroup(barcode, categoryId, statusFilter,
                workerId, pageNum, pageSize));
    }
}