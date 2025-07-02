import { get, post, put } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取订单详情
 * @param {number} id - 订单ID
 * @returns {Promise} - 返回订单详情
 */
export function getOrderById(id) {
  return get(`${API_PREFIX}/order/${id}`);
}

/**
 * 获取用户订单列表
 * @param {number} userId - 用户ID
 * @returns {Promise} - 返回订单列表
 */
export function listOrdersByUserId(userId) {
  return get(`${API_PREFIX}/order/user/${userId}`);
}

/**
 * 获取用户订单详情列表（包含航班和用户信息）
 * @param {number} userId - 用户ID
 * @returns {Promise} - 返回订单详情列表
 */
export function listOrdersWithDetail(userId) {
  return get(`${API_PREFIX}/order/user/${userId}/detail`);
}

/**
 * 获取航班订单列表
 * @param {number} flightId - 航班ID
 * @returns {Promise} - 返回订单列表
 */
export function listOrdersByFlightId(flightId) {
  return get(`${API_PREFIX}/order/flight/${flightId}`);
}

/**
 * 创建订单
 * @param {Object} data - 订单信息 {userId, flightId, seatNo}
 * @returns {Promise} - 返回创建结果
 */
export function createOrder(data) {
  return post(`${API_PREFIX}/order`, data);
}

/**
 * 取消订单
 * @param {number} id - 订单ID
 * @returns {Promise} - 返回取消结果
 */
export function cancelOrder(id) {
  return put(`${API_PREFIX}/order/${id}/cancel`);
}

/**
 * 获取所有订单列表（管理员使用）
 * @returns {Promise} - 返回订单列表
 */
export function listAllOrders() {
  return get(`${API_PREFIX}/order/admin/list`);
} 