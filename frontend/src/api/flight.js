import { get, post, put, del } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取航班列表
 * @returns {Promise} - 返回航班列表
 */
export function listFlights() {
  return get(`${API_PREFIX}/flight/list`);
}

/**
 * 获取航班详情
 * @param {number} id - 航班ID
 * @returns {Promise} - 返回航班详情
 */
export function getFlightById(id) {
  return get(`${API_PREFIX}/flight/${id}`);
}

/**
 * 根据航班号查询航班
 * @param {string} flightNo - 航班号
 * @returns {Promise} - 返回航班信息
 */
export function getFlightByNo(flightNo) {
  return get(`${API_PREFIX}/flight/no/${flightNo}`);
}

/**
 * 搜索航班
 * @param {Object} params - 搜索参数 {departureCity, arrivalCity, startTime, endTime}
 * @returns {Promise} - 返回航班列表
 */
export function searchFlights(params) {
  return get(`${API_PREFIX}/flight/search`, params);
}

/**
 * 根据航空公司查询航班
 * @param {string} airline - 航空公司
 * @returns {Promise} - 返回航班列表
 */
export function listByAirline(airline) {
  return get(`${API_PREFIX}/flight/airline/${airline}`);
}

/**
 * 添加航班
 * @param {Object} data - 航班信息
 * @returns {Promise} - 返回添加结果
 */
export function addFlight(data) {
  return post(`${API_PREFIX}/flight`, data);
}

/**
 * 更新航班信息
 * @param {number} id - 航班ID
 * @param {Object} data - 航班信息
 * @returns {Promise} - 返回更新结果
 */
export function updateFlight(id, data) {
  return put(`${API_PREFIX}/flight/${id}`, data);
}

/**
 * 删除航班
 * @param {number} id - 航班ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteFlight(id) {
  return del(`${API_PREFIX}/flight/${id}`);
} 