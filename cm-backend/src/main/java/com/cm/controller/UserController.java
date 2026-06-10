package com.cm.controller;

import com.cm.common.result.PageResult;
import com.cm.common.result.Result;
import com.cm.dto.UserDTO;
import com.cm.service.UserService;
import com.cm.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        return Result.ok(userService.listAll());
    }

    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortFields,
            @RequestParam(required = false) String sortOrders,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.ok(userService.listPage(keyword, sortFields, sortOrders, pageNum, pageSize));
    }

    @PostMapping
    public Result<UserVO> create(@RequestBody UserDTO dto) {
        return Result.ok(userService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        return Result.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/toggle")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.ok();
    }
}
