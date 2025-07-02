import { get, put, post } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取用户信息
 * @param {number} id - 用户ID
 * @returns {Promise} - 返回用户信息
 */
export function getUserInfo(id) {
  return get(`${API_PREFIX}/user/${id}`);
}

/**
 * 更新用户信息
 * @param {number} id - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回更新结果
 */
export function updateUserInfo(id, data) {
  return put(`${API_PREFIX}/user/${id}`, data);
}

/**
 * 修改密码
 * @param {Object} data - 密码数据 {username, oldPassword, newPassword}
 * @returns {Promise} - 返回修改结果
 */
export function modifyPassword(data) {
  return post(`${API_PREFIX}/user/modifyPassword`, data);
} 