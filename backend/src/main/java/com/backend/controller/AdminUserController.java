package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.entity.User;
import com.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 * 处理管理员对用户账号的管理功能
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取账号列表
     *
     * @return 账号列表
     */
    @GetMapping("/list")
    public Result listUsers() {
        return Result.success(userService.list());
    }

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 新增结果
     */
    @PostMapping
    public Result addUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名和密码不能为空");
        }

        // 检查用户名是否已存在
        User existingUser = userService.getByUsername(user.getUsername());
        if (existingUser != null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        boolean success = userService.save(user);
        if (success) {
            return Result.success("用户添加成功", null);
        } else {
            return Result.error("用户添加失败，请稍后重试");
        }
    }

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 修改结果
     */
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {

        // 检查用户是否存在
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 覆盖不能修改的字段
        user.setId(id);
        user.setUsername(existingUser.getUsername());
        // 如果密码不为空，则加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }


        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("用户信息更新成功", null);
        } else {
            return Result.error("用户信息更新失败，请稍后重试");
        }
    }
}
