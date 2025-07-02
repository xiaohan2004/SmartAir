import { get, put, post, del } from '@/utils/request';
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
export function updateUser(id, data) {
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

/**
 * 获取所有用户列表（管理员使用）
 * @returns {Promise} - 返回用户列表
 */
export function listAllUsers() {
  return get(`${API_PREFIX}/admin/user/list`);
}

/**
 * 创建新用户（管理员使用）
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回创建结果
 */
export function createUser(data) {
  return post(`${API_PREFIX}/admin/user`, data);
}

/**
 * 删除用户（管理员使用）
 * @param {number} id - 用户ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteUser(id) {
  return del(`${API_PREFIX}/admin/user/${id}`);
}

/**
 * 重置用户密码（管理员使用）
 * @param {number} id - 用户ID
 * @returns {Promise} - 返回重置结果
 */
export function resetPassword(id) {
  return post(`${API_PREFIX}/admin/user/${id}/resetPassword`);
}

/**
 * 修改用户状态（启用/禁用）（管理员使用）
 * @param {number} id - 用户ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise} - 返回修改结果
 */
export function updateUserStatus(id, enabled) {
  return put(`${API_PREFIX}/admin/user/${id}/status`, { enabled });
}

/**
 * 修改用户角色（管理员使用）
 * @param {number} id - 用户ID
 * @param {number} roleId - 角色ID
 * @returns {Promise} - 返回修改结果
 */
export function updateUserRole(id, roleId) {
  return put(`${API_PREFIX}/admin/user/${id}/role`, { roleId });
}

/**
 * 修改用户信息（管理员使用）
 * @param {number} id - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回修改结果
 */
export function updateUserInfo(id, data) {
  return put(`${API_PREFIX}/admin/user/${id}`, data);
} 