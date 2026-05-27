package com.cm.service;

import com.cm.dto.LoginDTO;
import com.cm.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
}