import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  // 从环境变量中获取API基础URL
  const baseApi = env.VITE_APP_BASE_API || 'http://localhost:8080/api'
  
  // 提取API路径和目标服务器
  const apiPath = baseApi.substring(baseApi.lastIndexOf('/'))
  const targetServer = baseApi.substring(0, baseApi.lastIndexOf('/'))
  
  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port: parseInt(env.PORT || 80),
      open: true,
      proxy: {
        [apiPath]: {
          target: targetServer,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiPath}`), '')
        }
      }
    }
  }
})
