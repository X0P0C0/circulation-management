package com.cm.controller;

import com.cm.common.constant.Constants;
import com.cm.common.result.Result;
import com.cm.dto.ChangePasswordDTO;
import com.cm.dto.LoginDTO;
import com.cm.service.AuthService;
import com.cm.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<LoginVO> info(HttpServletRequest request) {
        LoginVO vo = new LoginVO();
        vo.setUserId((Long) request.getAttribute(Constants.USER_ID_ATTR));
        vo.setUsername((String) request.getAttribute(Constants.USERNAME_ATTR));
        return Result.success(vo);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.USER_ID_ATTR);
        authService.changePassword(userId, dto);
        return Result.success();
    }
}