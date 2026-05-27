package com.cm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cm.entity.User;
import com.cm.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统初始化：首次启动时自动创建默认管理员账号
 * 账号：admin / admin123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count == null || count == 0) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("========================================");
            log.info("  默认管理员账号已创建：admin / admin123");
            log.info("  请登录后立即修改密码！");
            log.info("========================================");
        }
    }
}