package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.User;
import com.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户信息查询、修改等功能
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result getUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        // 构建返回数据，不包含敏感信息
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("realName", user.getRealName());
        data.put("idCard", user.getIdCard());
        data.put("userType", user.getUserType());
        data.put("memberLevel", user.getMemberLevel());
        
        return Result.success(data);
    }

    /**
    * 更新用户信息
    *
    * @param id 用户ID
    * @param updatedUser 用户
    * @return 更新结果
    */
    @PutMapping("/{id}")
    public Result updateUserInfo(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("用户信息更新成功", null);
        } else {
            return Result.error("用户信息更新失败");
        }
    }
}