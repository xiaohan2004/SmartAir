package com.backend.service;

import com.backend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);
    
    /**
     * 根据用户类型查询用户列表
     * @param userType 用户类型
     * @return 用户列表
     */
    java.util.List<User> listByUserType(Integer userType);
    
    /**
     * 更新用户会员等级
     * @param userId 用户ID
     * @param memberLevel 会员等级
     * @return 是否成功
     */
    boolean updateMemberLevel(Long userId, Integer memberLevel);

    User getUserByRealName(String realName);
}