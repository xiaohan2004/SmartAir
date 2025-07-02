import { get, post, del } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取日志列表
 * @returns {Promise} - 返回日志列表
 */
export function listLogs() {
  return get(`${API_PREFIX}/admin/log/list`);
}

/**
 * 根据时间范围查询日志
 * @param {Object} params - 查询参数 {startTime, endTime, keyword, level}
 * @returns {Promise} - 返回日志列表
 */
export function searchLogs(params) {
  return get(`${API_PREFIX}/admin/log/search`, { params });
}

/**
 * 获取日志详情
 * @param {number} id - 日志ID
 * @returns {Promise} - 返回日志详情
 */
export function getLogDetail(id) {
  return get(`${API_PREFIX}/admin/log/${id}`);
}

/**
 * 添加日志
 * @param {Object} data - 日志数据 {message}
 * @returns {Promise} - 返回添加结果
 */
export function addLog(data) {
  return post(`${API_PREFIX}/admin/log`, data);
}

/**
 * 清理指定时间之前的日志
 * @param {Object} data - 清理参数 {beforeTime}
 * @returns {Promise} - 返回清理结果
 */
export function cleanLogs(data) {
  return del(`${API_PREFIX}/admin/log/clean`, data);
}

/**
 * 导出日志
 * @param {Object} params - 导出参数 {startTime, endTime, level, keyword}
 * @returns {Promise} - 返回导出结果
 */
export function exportLogs(params) {
  return get(`${API_PREFIX}/admin/log/export`, { 
    params,
    responseType: 'blob' 
  });
} 