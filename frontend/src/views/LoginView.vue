<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  role: 'user' // 默认角色为用户
});

// 角色选项
const roleOptions = [
  { label: '用户', value: 'user' },
  { label: '客服', value: 'service' },
  { label: '管理员', value: 'admin' }
];

// 登录方法
const handleLogin = () => {
  // 这里预留登录请求逻辑
  console.log('登录信息:', loginForm);

  // 根据选择的角色跳转到不同页面
  if (loginForm.role === 'user') {
    router.push('/user');
  } else if (loginForm.role === 'service') {
    router.push('/service');
  } else if (loginForm.role === 'admin') {
    router.push('/admin');
  }
};

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register');
};

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push('/forgot-password');
};
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
            {{ role.label }}
          </div>
        </div>
      </div>

      <div class="login-form">
        <h2>SmartAir 智能航空</h2>
        <el-form>
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="login-button">登录</el-button>
          </el-form-item>

          <div class="login-options">
            <el-link type="primary" @click="goToRegister">注册账号</el-link>
            <el-link type="primary" @click="goToForgotPassword">忘记密码</el-link>
          </div>
        </el-form>
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
  background-color: #f5f7fa;
  box-sizing: border-box;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.login-box {
  width: 80%;
  max-width: 900px;
  height: 500px;
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.role-selector {
  width: 300px;
  background-color: #409eff;
  color: white;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.role-selector h2 {
  margin-bottom: 40px;
}

.role-options {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.role-option {
  padding: 15px 20px;
  margin-bottom: 15px;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.role-option:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.role-option.active {
  background-color: white;
  color: #409eff;
}

.login-form {
  flex: 1;
  background-color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-form h2 {
  margin-bottom: 40px;
  color: #303133;
}

.login-form el-form {
  width: 100%;
}

.login-button {
  width: 100%;
}

.login-options {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}
</style>
