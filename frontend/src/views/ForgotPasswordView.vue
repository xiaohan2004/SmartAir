<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api';

const router = useRouter();
const loading = ref(false);

// 步骤
const currentStep = ref(1);

// 表单数据
const forgotForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const formRef = ref(null);

// 发送验证码
const isCodeSent = ref(false);
const countdown = ref(0);
const timer = ref(null);

const sendCode = async () => {
  try {
    // 先验证邮箱
    await formRef.value.validateField('email');
    
    if (!forgotForm.email) {
      return;
    }
    
    loading.value = true;
    
    // 调用发送验证码API
    await authApi.sendVerificationCode({
      email: forgotForm.email,
      type: 'resetPassword'  // 标记是重置密码场景
    });
    
    ElMessage.success('验证码已发送到您的邮箱');
    isCodeSent.value = true;
    
    // 倒计时
    countdown.value = 60;
    timer.value = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer.value);
        isCodeSent.value = false;
      }
    }, 1000);
  } catch (error) {
    console.error('发送验证码失败:', error);
    ElMessage.error(error.message || '发送验证码失败');
  } finally {
    loading.value = false;
  }
};

// 下一步
const nextStep = async () => {
  try {
    // 验证表单
    if (currentStep.value === 1) {
      // 验证第一步的字段
      await formRef.value.validateField(['username', 'email', 'verificationCode']);
      
      // 这里可以调用API验证验证码是否正确，但大多数情况下会在最终提交时一起验证
      currentStep.value = 2;
    } else if (currentStep.value === 2) {
      // 验证第二步的字段
      await formRef.value.validateField(['newPassword', 'confirmPassword']);
      
      loading.value = true;
      
      // 调用重置密码API
      await authApi.resetPassword({
        username: forgotForm.username,
        email: forgotForm.email,
        newPassword: forgotForm.newPassword,
        code: forgotForm.verificationCode
      });
      
      ElMessage.success('密码重置成功，请使用新密码登录');
      router.push('/login');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error(error.message || '操作失败');
  } finally {
    loading.value = false;
  }
};

// 返回登录页
const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="forgot-header">
        <h2>找回密码</h2>
        <p>重置您的账号密码</p>
      </div>
      
      <el-steps :active="currentStep" finish-status="success" simple class="steps">
        <el-step title="验证身份" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>
      
      <el-form 
        ref="formRef"
        :model="forgotForm"
        :rules="rules"
        label-position="top"
        class="forgot-form"
      >
        <!-- 第一步：验证身份 -->
        <template v-if="currentStep === 1">
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="forgotForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="User"
            />
          </el-form-item>
        
          <el-form-item label="邮箱" prop="email">
            <el-input 
              v-model="forgotForm.email" 
              placeholder="请输入注册时使用的邮箱" 
              prefix-icon="Message"
            />
          </el-form-item>
          
          <el-form-item label="验证码" prop="verificationCode">
            <div class="verification-code">
              <el-input 
                v-model="forgotForm.verificationCode" 
                placeholder="请输入验证码"
                class="code-input"
              />
              <el-button 
                type="primary" 
                :disabled="isCodeSent"
                :loading="loading && !isCodeSent"
                @click="sendCode"
                class="code-button"
              >
                {{ isCodeSent ? `${countdown}秒后重新获取` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </template>
        
        <!-- 第二步：设置新密码 -->
        <template v-if="currentStep === 2">
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="forgotForm.newPassword" 
              placeholder="请输入新密码" 
              prefix-icon="Lock" 
              type="password" 
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input 
              v-model="forgotForm.confirmPassword" 
              placeholder="请再次输入新密码" 
              prefix-icon="Lock" 
              type="password" 
              show-password
            />
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="nextStep" 
            class="submit-button"
            :loading="loading"
          >
            {{ currentStep === 1 ? '下一步' : '提交' }}
          </el-button>
        </el-form-item>
        
        <div class="login-link">
          想起密码了？<el-link type="primary" @click="goToLogin">去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.forgot-container {
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

.forgot-box {
  width: 100%;
  max-width: 520px;
  background-color: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-header h2 {
  margin-bottom: 10px;
  color: #303133;
}

.forgot-header p {
  color: #909399;
}

.steps {
  margin-bottom: 30px;
  white-space: nowrap;
}

.verification-code {
  display: flex;
}

.code-input {
  flex: 1;
  margin-right: 10px;
}

.code-button {
  width: 140px;
}

.forgot-form {
  width: 100%;
}

.submit-button {
  width: 100%;
  height: 40px;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 