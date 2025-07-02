import { get, post, put, del } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 创建新模板
 * @param {Object} data - 模板数据
 * @returns {Promise} - 返回创建结果
 */
export function createPrompt(data) {
  return post(`${API_PREFIX}/prompt`, data);
}

/**
 * 按名称查询模板
 * @param {string} name - 模板名称
 * @returns {Promise} - 返回查询结果
 */
export function getPromptByName(name) {
  return get(`${API_PREFIX}/prompt/name/${name}`);
}

/**
 * 查询所有模板
 * @returns {Promise} - 返回所有模板列表
 */
export function getAllPrompts() {
  return get(`${API_PREFIX}/prompt`);
}

/**
 * 更新模板内容
 * @param {string} id - 模板ID
 * @param {Object} data - 更新数据，包含 name, template, description 等字段
 * @returns {Promise} - 返回更新结果
 */
export function updatePrompt(id, data) {
  return put(`${API_PREFIX}/prompt/${id}`, data);
}

/**
 * 删除模板
 * @param {string} id - 模板ID
 * @returns {Promise} - 返回删除结果
 */
export function deletePrompt(id) {
  return del(`${API_PREFIX}/prompt/${id}`);
}

export default {
  createPrompt,
  getPromptByName,
  getAllPrompts,
  updatePrompt,
  deletePrompt
}; 