<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { setToken } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  role: 'user', // 默认角色为用户
})

// 角色选项
const roleOptions = [
  { label: '用户', value: 'user' },
  { label: '客服', value: 'service' },
  { label: '管理员', value: 'admin' },
]

// 登录方法
const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  try {
    // 设置加载状态
    loading.value = true

    // 调用登录API
    const response = await authApi.login({
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role,
    })

    // 登录成功，保存token
    const token = response.data
    if (!token) {
      throw new Error('登录失败，未获取到有效的token')
    }

    // 只保存token，不保存用户信息
    setToken(token)

    // 提示成功
    ElMessage.success('登录成功')

    // 根据选择的角色跳转到不同页面
    if (loginForm.role === 'user') {
      router.push('/user')
    } else if (loginForm.role === 'service') {
      router.push('/service')
    } else if (loginForm.role === 'admin') {
      router.push('/admin')
    }
  } catch (error) {
    // 显示错误消息
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    // 无论成功失败，都取消加载状态
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="role-selector">
        <h2>角色选择</h2>
        <div class="role-options">
          <div
            v-for="role in roleOptions"
            :key="role.value"
            class="role-option"
            :class="{ active: loginForm.role === role.value }"
            @click="loginForm.role = role.value"
          >
            <el-icon v-if="role.value === 'user'" class="role-icon"><User /></el-icon>
            <el-icon v-if="role.value === 'service'" class="role-icon"><Service /></el-icon>
            <el-icon v-if="role.value === 'admin'" class="role-icon"><Setting /></el-icon>
            {{ role.label }}
          </div>
        </div>
      </div>

      <div class="login-form">
        <div class="login-content">
          <h2 style="display: flex; align-items: center; gap: 10px">
            <img src="@/assets/logo.png" alt="Logo" style="height: 32px" />
            SmartAir 智能航空
          </h2>

          <el-form>
            <el-form-item class="form-item">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
            </el-form-item>

            <el-form-item class="form-item">
              <el-input
                v-model="loginForm.password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                type="password"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item class="form-item">
              <el-button type="primary" @click="handleLogin" class="login-button" :loading="loading"
                >登录</el-button
              >
            </el-form-item>

            <div class="login-options">
              <el-link type="primary" @click="goToRegister">注册账号</el-link>
              <el-link type="primary" @click="goToForgotPassword">忘记密码</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  box-sizing: border-box;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20px;
}

.login-box {
  width: 80%;
  max-width: 900px;
  height: auto;
  min-height: 500px;
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
}

.role-selector {
  width: 300px;
  background: linear-gradient(135deg, #409eff 0%, #2e88e5 100%);
  color: white;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.role-selector h2 {
  margin-bottom: 40px;
  font-weight: 500;
}

.role-options {
  display: flex;
  flex-direction: column;
  width: 100%;
  flex: 1;
  justify-content: flex-start;
  align-items: center;
}

.role-option {
  padding: 15px 20px;
  margin-bottom: 15px;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.role-icon {
  font-size: 18px;
}

.role-option:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.role-option.active {
  background-color: white;
  color: #409eff;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-form {
  flex: 1;
  background-color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.login-content {
  width: 100%;
  max-width: 350px;
  display: flex;
  flex-direction: column;
}

.login-form h2 {
  margin-bottom: 50px;
  color: #303133;
  text-align: center;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-form :deep(.el-form) {
  width: 100%;
}

.form-item {
  margin-bottom: 25px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  height: 46px;
}

.login-button {
  width: 100%;
  height: 46px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 10px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    flex-direction: column;
    height: auto;
    width: 95%;
  }
  
  .role-selector {
    width: 100%;
    padding: 30px 20px;
  }
  
  .role-options {
    flex-direction: row;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .role-option {
    width: 100px;
    margin-bottom: 0;
  }
  
  .login-form {
    padding: 40px 20px;
  }
  
  .login-form h2 {
    margin-bottom: 40px;
  }
  
  .form-item {
    margin-bottom: 20px;
  }
}
</style>
