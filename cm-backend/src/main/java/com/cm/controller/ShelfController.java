package com.cm.controller;

import com.cm.common.result.Result;
import com.cm.entity.Shelf;
import com.cm.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;

    @GetMapping
    public Result<List<Shelf>> list(@RequestParam(required = false) String keyword) {
        return Result.success(shelfService.listAll(keyword));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Shelf shelf) {
        shelfService.create(shelf);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Shelf shelf) {
        shelfService.update(id, shelf);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shelfService.delete(id);
        return Result.success();
    }
}
