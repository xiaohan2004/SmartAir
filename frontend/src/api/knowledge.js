import { get, post, put, del } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 添加知识文档
 * @param {Object} data - 文档数据
 * @returns {Promise} - 返回创建结果
 */
export function createKnowledge(data) {
  return post(`${API_PREFIX}/knowledge`, data);
}

/**
 * 搜索知识文档
 * @param {string} keyword - 搜索关键词
 * @returns {Promise} - 返回搜索结果
 */
export function searchKnowledge(keyword) {
  return get(`${API_PREFIX}/knowledge/search?keyword=${encodeURIComponent(keyword)}`);
}

/**
 * 更新知识文档
 * @param {string} id - 文档ID
 * @param {Object} data - 更新数据，包含 title, content, tags 等字段
 * @returns {Promise} - 返回更新结果
 */
export function updateKnowledge(id, data) {
  return put(`${API_PREFIX}/knowledge/${id}`, data);
}

/**
 * 删除知识文档
 * @param {string} id - 文档ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteKnowledge(id) {
  return del(`${API_PREFIX}/knowledge/${id}`);
}

/**
 * 获取所有知识文档
 * @returns {Promise} - 返回所有文档列表
 */
export function getAllKnowledge() {
  return get(`${API_PREFIX}/knowledge`);
}

export default {
  createKnowledge,
  searchKnowledge,
  updateKnowledge,
  deleteKnowledge,
  getAllKnowledge
}; 