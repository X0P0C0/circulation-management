package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.dto.AccessoryUpdateDTO;
import com.cm.service.AccessoryService;
import com.cm.vo.AccessoryVO;
import com.cm.vo.InventoryGroupVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accessories")
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @PostMapping("/inbound")
    public Result<List<AccessoryVO>> inbound(@Valid @RequestBody AccessoryInboundDTO dto,
                                              HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        return Result.ok(accessoryService.inbound(dto, operator));
    }

    @GetMapping("/{id}")
    public Result<AccessoryVO> getById(@PathVariable Long id) {
        return Result.ok(accessoryService.findById(id));
    }

    @PostMapping("/import")
    public Result<List<AccessoryVO>> importCsv(@RequestParam("file") MultipartFile file,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String remark,
                                                HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        return Result.ok(accessoryService.importFromCsv(file, categoryId, remark, operator));
    }

    @GetMapping("/barcode/{barcode}")
    public Result<AccessoryVO> getByBarcode(@PathVariable String barcode) {
        return Result.ok(accessoryService.findByBarcode(barcode));
    }

    @GetMapping("/search")
    public Result<PageResult<AccessoryVO>> search(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer statusNot,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Integer highValue,
            @RequestParam(required = false) Long shelfId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sortFields,
            @RequestParam(required = false) String sortOrders,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(accessoryService.search(barcode, categoryId, status, statusNot, workerId,
                keyword, operator, remark, highValue, shelfId, startDate, endDate, sortFields, sortOrders, pageNum, pageSize));
    }

    @GetMapping("/inventory-group")
    public Result<PageResult<InventoryGroupVO>> inventoryGroup(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer statusFilter,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) Integer highValue,
            @RequestParam(required = false) Long shelfId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(accessoryService.inventoryGroup(barcode, categoryId, statusFilter,
                workerId, remark, highValue, shelfId, startDate, endDate, pageNum, pageSize));
    }

    @PostMapping("/import-excel")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file,
                                                    HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        return Result.ok(accessoryService.importFromExcel(file, operator));
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accessoryService.deleteById(id);
        return Result.ok();
    }

    @PutMapping
    public Result<AccessoryVO> update(@Valid @RequestBody AccessoryUpdateDTO dto,
                                       HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        return Result.ok(accessoryService.update(dto, operator));
    }
}
