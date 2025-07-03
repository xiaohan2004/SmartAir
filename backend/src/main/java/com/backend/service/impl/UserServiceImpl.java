package com.backend.service.impl;

import com.backend.entity.User;
import com.backend.dao.mapper.UserMapper;
import com.backend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }

    @Override
    public List<User> listByUserType(Integer userType) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserType, userType);
        return list(queryWrapper);
    }

    @Override
    public boolean updateMemberLevel(Long userId, Integer memberLevel) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        user.setMemberLevel(memberLevel);
        return updateById(user);
    }

    @Override
    public User getUserByRealName(String realName) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRealName, realName);
        return getOne(queryWrapper);
    }
}