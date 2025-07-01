package com.backend.dao.redis;

import com.backend.common.constant.EmailConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 数据访问对象
 * 处理 Redis 相关的数据操作
 */
@Component
public class RedisDao {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 存储字符串到 Redis
     *
     * @param key 键
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 从 Redis 获取字符串
     *
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除键
     *
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 存储邮箱验证码
     *
     * @param email 邮箱
     * @param type 验证码类型
     * @param code 验证码
     */
    public void saveEmailVerificationCode(String email, String type, String code) {
        String redisKey = generateEmailCodeKey(type, email);
        setString(redisKey, code, EmailConstant.EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 获取邮箱验证码
     *
     * @param email 邮箱
     * @param type 验证码类型
     * @return 验证码
     */
    public String getEmailVerificationCode(String email, String type) {
        String redisKey = generateEmailCodeKey(type, email);
        return getString(redisKey);
    }

    /**
     * 删除邮箱验证码
     *
     * @param email 邮箱
     * @param type 验证码类型
     * @return 是否成功
     */
    public boolean deleteEmailVerificationCode(String email, String type) {
        String redisKey = generateEmailCodeKey(type, email);
        return delete(redisKey);
    }

    /**
     * 生成邮箱验证码的 Redis key
     *
     * @param type 验证码类型
     * @param email 邮箱
     * @return Redis key
     */
    private String generateEmailCodeKey(String type, String email) {
        return "email:code:" + type + ":" + email;
    }
} 