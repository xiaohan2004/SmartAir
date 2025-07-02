import { get } from '@/utils/request';
import { API_PREFIX } from '@/utils/api-prefix';

/**
 * 获取仪表盘概览数据
 * @returns {Promise} - 返回仪表盘数据
 */
export function getDashboardOverview() {
  return get(`${API_PREFIX}/dashboard/overview`);
}

export const dashboardApi = {
  getDashboardOverview
}; 