/**
 * JWT等工具，用于解析JWT token，获取映射的会员等级名称
 */

/**
 * 解析JWT token
 * @param {string} token - JWT token
 * @returns {Object} 解析后的payload部分
 */
export function parseJwt(token) {
  try {
    // 分割JWT的三个部分：header.payload.signature
    const base64Url = token.split('.')[1];
    // Base64URL解码
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    // 解码并解析为JSON对象
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT解析失败:', error);
    return {};
  }
}

/**
 * 根据userType获取角色名称
 * @param {number} userType - 用户类型: 1-普通用户，2-客服人员，3-系统管理员
 * @returns {string} 角色名称
 */
export function getRoleName(userType) {
  switch (userType) {
    case 1:
      return 'user';
    case 2:
      return 'service';
    case 3:
      return 'admin';
    default:
      return 'user';
  }
}

/**
 * 根据memberLevel获取会员等级名称
 * @param {number} memberLevel - 会员等级: 1-普通会员，2-白银会员，3-黄金会员，4-白金会员，5-钻石会员
 * @returns {string} 会员等级名称
 */
export function getMemberLevelName(memberLevel) {
  switch (memberLevel) {
    case 1:
      return '普通会员';
    case 2:
      return '白银会员';
    case 3:
      return '黄金会员';
    case 4:
      return '白金会员';
    case 5:
      return '钻石会员';
    default:
      return '普通会员';
  }
} 