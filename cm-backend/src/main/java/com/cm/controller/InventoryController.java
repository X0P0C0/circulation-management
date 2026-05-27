package com.cm.controller;

import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.entity.Category;
import com.cm.service.CategoryService;
import com.cm.service.InventoryService;
import com.cm.vo.InventoryVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final CategoryService categoryService;

    @GetMapping
    public Result<PageResult<InventoryVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inventoryService.listPage(categoryId, keyword, pageNum, pageSize));
    }

    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.success(inventoryService.getStats());
    }

    @GetMapping("/export")
    public void exportCsv(@RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String keyword,
                          HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=inventory.csv");
        response.setCharacterEncoding("UTF-8");

        // BOM for Excel
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);

        PageResult<InventoryVO> data = inventoryService.listPage(categoryId, keyword, 1, 10000);

        PrintWriter writer = response.getWriter();
        writer.println("Barcode,Name,Spec,Category,Unit,TotalQty,AvailableQty");
        for (InventoryVO item : data.getRecords()) {
            writer.println(String.join(",",
                    escape(item.getBarcode()),
                    escape(item.getAccessoryName()),
                    escape(item.getSpec()),
                    escape(item.getCategoryName()),
                    escape(item.getUnit()),
                    String.valueOf(item.getTotalQty()),
                    String.valueOf(item.getAvailableQty())));
        }
        writer.flush();
    }

    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}