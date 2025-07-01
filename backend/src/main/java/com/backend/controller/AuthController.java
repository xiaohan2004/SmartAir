package com.backend.controller;

import com.backend.common.Result;
import com.backend.common.enumeration.ResultCode;
import com.backend.dao.redis.RedisDao;
import com.backend.entity.User;
import com.backend.service.UserService;
import com.backend.util.EmailUtil;
import com.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注册、重置密码等认证相关功能
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private RedisDao redisDao;

    /**
     * 用户登录
     *
     * @param loginMap 包含用户名和密码的Map
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginMap) {
        String username = loginMap.get("username");
        String password = loginMap.get("password");
        
        // 参数校验
        if (username == null || password == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名或密码不能为空");
        }
        
        // 查询用户
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(user.getPassword(), password)) {
            return Result.error(ResultCode.UNAUTHORIZED, "密码错误");
        }

        // 生成新的JWT令牌
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("userId", user.getId());
        tokenClaims.put("username", user.getUsername());
        tokenClaims.put("userType", user.getUserType());
        tokenClaims.put("memberLevel", user.getMemberLevel());

        String token = JwtUtil.generateJwt(tokenClaims);
        
        return Result.success("登录成功", token);
    }

    /**
     * 发送邮箱验证码
     *
     * @param params 包含邮箱和类型的Map
     * @return 发送结果
     */
    @PostMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String type = params.get("type");

        // 1. 生成并发送验证码
        String code = emailUtil.sendVerificationCode(email, type);

        // 2. 存储验证码到 Redis
        redisDao.saveEmailVerificationCode(email, type, code);

        return Result.success("验证码已发送");
    }

    /**
     * 用户注册
     *
     * @param params 包含用户信息与验证码的Map
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, Object> params) {
        User user = (User) params.get("user");
        String code = (String) params.get("code");
        String email = user.getEmail();

        // 参数校验
        if (user.getUsername() == null || user.getPassword() == null || email == null || code == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名、密码、邮箱或验证码不能为空");
        }

        // 检查用户名是否已存在
        User existUser = userService.getByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名已存在");
        }

        // 验证验证码
        String storedCode = redisDao.getEmailVerificationCode(email, "register");
        if (!code.equals(storedCode)) {
            return Result.error(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 设置用户默认信息
        user.setUserType(1); // 默认为普通用户
        user.setMemberLevel(1); // 默认为普通会员

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        boolean success = userService.save(user);
        if (success) {
            return Result.success("注册成功", null);
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 重置密码
     *
     * @param resetMap 包含用户名、邮箱和新密码的Map
     * @return 重置结果
     */
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> resetMap) {
        String username = resetMap.get("username");
        String email = resetMap.get("email");
        String newPassword = resetMap.get("newPassword");
        String code = resetMap.get("code");
        
        // 参数校验
        if (username == null || email == null || newPassword == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户名、邮箱或新密码不能为空");
        }
        
        // 查询用户
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        // 验证邮箱
        if (!email.equals(user.getEmail())) {
            return Result.error(ResultCode.PARAM_ERROR, "邮箱不匹配");
        }

        // 验证验证码
        String storedCode = redisDao.getEmailVerificationCode(email, "reset");
        if (!storedCode.equals(code)) {
            return Result.error(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean success = userService.updateById(user);
        
        if (success) {
            return Result.success("密码重置成功", null);
        } else {
            return Result.error("密码重置失败，请稍后重试");
        }
    }
}