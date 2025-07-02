import { get, post, put } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取会话详情
 * @param {string} uuid - 会话UUID
 * @returns {Promise} - 返回会话详情
 */
export function getConversation(uuid) {
  return get(`${API_PREFIX}/conversation/${uuid}`);
}

/**
 * 获取用户会话列表
 * @param {number} userId - 用户ID
 * @returns {Promise} - 返回会话列表
 */
export function listConversationsByUserId(userId) {
  return get(`${API_PREFIX}/conversation/user/${userId}`);
}

/**
 * 获取客服会话列表（仅已转人工的会话）
 * @param {number} serviceUserId - 客服用户ID
 * @returns {Promise} - 返回会话列表
 */
export function listConversationsByServiceUserId(serviceUserId) {
  return get(`${API_PREFIX}/conversation/service/${serviceUserId}`);
}

/**
 * 获取用户活跃会话
 * @param {number} userId - 用户ID
 * @returns {Promise} - 返回活跃会话
 */
export function getUserActiveConversation(userId) {
  return get(`${API_PREFIX}/conversation/user/${userId}/active`);
}

/**
 * 创建新会话
 * @param {Object} data - 会话数据 {userId, initialMessage}
 * @returns {Promise} - 返回创建结果
 */
export function createConversation(data) {
  return post(`${API_PREFIX}/conversation`, data);
}

/**
 * 转接会话到客服
 * @param {string} uuid - 会话UUID
 * @param {Object} data - 转接数据 {serviceUserId}
 * @returns {Promise} - 返回转接结果
 */
export function transferToService(uuid, data) {
  return put(`${API_PREFIX}/conversation/${uuid}/transfer`, data);
}

/**
 * 关闭会话
 * @param {string} uuid - 会话UUID
 * @returns {Promise} - 返回关闭结果
 */
export function closeConversation(uuid) {
  return put(`${API_PREFIX}/conversation/${uuid}/close`);
}

/**
 * 获取所有会话列表（管理员使用）
 * @returns {Promise} - 返回会话列表
 */
export function listAllConversations() {
  return get(`${API_PREFIX}/conversation/admin/list`);
} 