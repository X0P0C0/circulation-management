package com.cm.controller;

import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.WorkerDTO;
import com.cm.service.WorkerService;
import com.cm.vo.WorkerVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody WorkerDTO dto) {
        workerService.create(dto);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody WorkerDTO dto) {
        workerService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workerService.delete(id);
        return Result.ok();
    }

    @GetMapping
    public Result<PageResult<WorkerVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(workerService.listPage(keyword, pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<WorkerVO>> listAll() {
        return Result.ok(workerService.listAll());
    }
}