/**
 * 认证工具函数
 */
import { parseJwt, getRoleName } from '@/utils/jwt';

const TokenKey = 'token';

/**
 * 获取JWT令牌
 * @returns {string|null} 存储的JWT令牌
 */
export function getToken() {
  return localStorage.getItem(TokenKey);
}

/**
 * 设置JWT令牌
 * @param {string} token JWT令牌
 */
export function setToken(token) {
  localStorage.setItem(TokenKey, token);
}

/**
 * 移除JWT令牌
 */
export function removeToken() {
  localStorage.removeItem(TokenKey);
}

/**
 * 获取当前登录的用户基本信息（从JWT解析）
 * @returns {Object|null} 用户基本信息对象，包含id, username, role, userType
 */
export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;
  
  try {
    const tokenData = parseJwt(token);
    const userId = tokenData.userId;
    const username = tokenData.username;
    const userType = tokenData.userType;
    
    if (!userId || !username || !userType) {
      console.warn('Token中缺少必要的用户信息');
      return null;
    }
    
    return {
      id: userId,
      username,
      role: getRoleName(userType),
      userType
    };
  } catch (error) {
    console.error('解析JWT失败:', error);
    return null;
  }
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = getToken();
  if (!token) return false;
  
  try {
    // 检查token是否有效
    const tokenData = parseJwt(token);
    if (!tokenData.userId) return false;
    
    // 检查token是否过期
    const exp = tokenData.exp;
    if (exp && exp < Date.now() / 1000) {
      // token已过期，清除
      removeToken();
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('验证登录状态失败:', error);
    return false;
  }
}

/**
 * 退出登录
 */
export function logout() {
  removeToken();
} 