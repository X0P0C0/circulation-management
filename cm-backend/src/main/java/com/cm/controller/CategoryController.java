package com.cm.controller;

import com.cm.common.result.Result;
import com.cm.dto.CategoryDTO;
import com.cm.entity.Category;
import com.cm.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryDTO dto) {
        categoryService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/move")
    public Result<Void> moveSort(@PathVariable Long id, @RequestParam String direction) {
        categoryService.moveSort(id, direction);
        return Result.success();
    }

    @PutMapping("/batch-sort")
    public Result<Void> batchSort(@RequestBody List<Long> ids) {
        categoryService.batchSort(ids);
        return Result.success();
    }

    @GetMapping
("/top")
    public Result<List<Category>> getTopCategories() {
        return Result.success(categoryService.getTopCategories());
    }

    @PutMapping("/resort")
    public Result<Void> resort() {
        categoryService.resort();
        return Result.success();
    }

    @GetMapping
    public Result<List<Category>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortFields,
            @RequestParam(required = false) String sortOrders) {
        return Result.success(categoryService.listAll(keyword, sortFields, sortOrders));
    }
}
