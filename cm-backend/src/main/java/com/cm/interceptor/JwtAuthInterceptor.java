package com.cm.interceptor;

import com.cm.common.constant.Constants;
import com.cm.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private static final Set<String> ADMIN_PATHS = Set.of("/api/user/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String authHeader = request.getHeader(Constants.TOKEN_HEADER);
        if (authHeader == null || !authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或令牌已过期\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\",\"data\":null}");
            return false;
        }

        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        // role可能为null（旧token兼容）
        Integer roleObj = claims.get("role", Integer.class);
        int role = roleObj != null ? roleObj : Constants.ROLE_ADMIN;

        request.setAttribute(Constants.USER_ID_ATTR, userId);
        request.setAttribute(Constants.USERNAME_ATTR, username);
        request.setAttribute(Constants.ROLE_ATTR, role);

        String path = request.getRequestURI();
        for (String adminPath : ADMIN_PATHS) {
            if (path.startsWith(adminPath) && role != Constants.ROLE_ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，需要管理员权限\",\"data\":null}");
                return false;
            }
        }

        return true;
    }
}