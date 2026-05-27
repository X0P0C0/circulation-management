package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.Result;
import com.cm.dto.AccessoryInboundDTO;
import com.cm.service.AccessoryService;
import com.cm.vo.AccessoryVO;
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
    public Result<Void> inbound(@Valid @RequestBody AccessoryInboundDTO dto, HttpServletRequest request) {
        String operator = (String) request.getAttribute(Constants.USERNAME_ATTR);
        accessoryService.inbound(dto, operator);
        return Result.success();
    }

    @GetMapping("/{barcode}")
    public Result<AccessoryVO> findByBarcode(@PathVariable String barcode) {
        return Result.success(accessoryService.findByBarcode(barcode));
    }
}