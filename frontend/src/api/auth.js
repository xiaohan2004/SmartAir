import { post } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 用户登录
 * @param {Object} data - 登录数据 {username, password}
 * @returns {Promise} - 返回登录结果
 */
export function login(data) {
  return post(`${API_PREFIX}/auth/login`, data);
}

/**
 * 用户注册
 * @param {Object} data - 注册数据 {user: {username, password, email, ...}, code}
 * @returns {Promise} - 返回注册结果
 */
export function register(data) {
  return post(`${API_PREFIX}/auth/register`, data);
}

/**
 * 发送验证码
 * @param {Object} data - 验证码数据 {email, type}
 * @returns {Promise} - 返回发送结果
 */
export function sendVerificationCode(data) {
  return post(`${API_PREFIX}/auth/sendVerificationCode`, data);
}

/**
 * 重置密码
 * @param {Object} data - 重置密码数据 {username, email, newPassword, code}
 * @returns {Promise} - 返回重置结果
 */
export function resetPassword(data) {
  return post(`${API_PREFIX}/auth/resetPassword`, data);
} 