package com.cm.service;

import com.cm.dto.ChangePasswordDTO;
import com.cm.dto.LoginDTO;
import com.cm.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    void changePassword(Long userId, ChangePasswordDTO dto);
}