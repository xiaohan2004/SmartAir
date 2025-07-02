import axios from 'axios';

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || 'http://localhost:8080/api',
  timeout: parseInt(import.meta.env.VITE_APP_API_TIMEOUT || '10000')
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    // 如果有token，则所有请求都带上token
    if (token) {
      config.headers['token'] = token;
    }
    
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 请求成功但业务失败
    if (res.code !== 200) {
      // 401: 未授权，token过期或无效
      if (res.code === 401 && res.msg !== '旧密码错误') {
        // 清除token
        localStorage.removeItem('token');
      }
      
      return Promise.reject(new Error(res.msg || '请求失败'));
    } else {
      return res;
    }
  },
  error => {
    console.error('响应错误:', error);
    
    // 处理HTTP错误
    let message = '请求失败';
    if (error.response && error.response.data && error.response.data.msg) {
      // 使用后端返回的错误消息
      message = error.response.data.msg;
    } else if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录';
          // 清除token
          localStorage.removeItem('token');
          break;
        case 403:
          message = '拒绝访问';
          break;
        case 404:
          message = '请求的资源不存在';
          break;
        case 500:
          message = '服务器内部错误';
          break;
        default:
          message = `请求失败: ${error.response.status}`;
      }
    }
    
    // 返回包含自定义错误消息的错误对象
    return Promise.reject(new Error(message));
  }
);

// 封装GET请求
export function get(url, params) {
  return service({
    url,
    method: 'get',
    params
  });
}

// 封装POST请求
export function post(url, data) {
  return service({
    url,
    method: 'post',
    data
  });
}

// 封装PUT请求
export function put(url, data) {
  return service({
    url,
    method: 'put',
    data
  });
}

// 封装DELETE请求
export function del(url, data) {
  return service({
    url,
    method: 'delete',
    data
  });
}

export default service; 