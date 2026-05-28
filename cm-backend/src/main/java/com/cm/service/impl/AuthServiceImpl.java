package com.cm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cm.common.exception.BusinessException;
import com.cm.dto.ChangePasswordDTO;
import com.cm.dto.LoginDTO;
import com.cm.entity.User;
import com.cm.enums.RoleEnum;
import com.cm.mapper.UserMapper;
import com.cm.service.AuthService;
import com.cm.util.JwtUtil;
import com.cm.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 15;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查账号锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            long minutes = java.time.Duration.between(LocalDateTime.now(), user.getLockTime()).toMinutes() + 1;
            throw new BusinessException(403, "账号已锁定，请 " + minutes + " 分钟后重试");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 密码校验
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            // 失败次数+1
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= MAX_FAIL_COUNT) {
                user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                user.setLoginFailCount(0);
                userMapper.updateById(user);
                throw new BusinessException(403, "连续错误 " + MAX_FAIL_COUNT + " 次，账号已锁定 " + LOCK_MINUTES + " 分钟");
            }
            userMapper.updateById(user);
            throw new BusinessException(401, "用户名或密码错误（还剩 " + (MAX_FAIL_COUNT - failCount) + " 次机会）");
        }

        // 登录成功，重置失败次数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleName(RoleEnum.of(user.getRole()).getDesc());
        vo.setToken(token);
        return vo;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}