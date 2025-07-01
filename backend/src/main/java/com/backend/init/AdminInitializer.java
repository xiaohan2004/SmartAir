package com.backend.init;

import com.backend.config.AdminProperties;
import com.backend.entity.User;
import com.backend.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminProperties adminProperties;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdmin() {
        User existing = userService.getByUsername(adminProperties.getUsername());
        if (existing != null) {
            System.out.println("✅ 管理员已存在：" + existing.getUsername());
            return;
        }

        User admin = new User();
        admin.setUsername(adminProperties.getUsername());
        admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
        admin.setUserType(3); // 3 = 系统管理员

        userService.save(admin);
        System.out.println("✅ 管理员账户已初始化: " + admin.getUsername());
    }
}
