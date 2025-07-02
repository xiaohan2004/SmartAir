<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authApi } from '@/api';

const router = useRouter();
const loading = ref(false);

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  realName: '', // 真实姓名
  idCard: '', // 身份证号
  role: 'user' // 默认角色为用户，不允许更改
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,10}$/, message: '请输入正确的中文姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
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

// 验证码相关
const verificationCode = ref('');
const isCodeSent = ref(false);
const countdown = ref(0);
const timer = ref(null);

// 发送验证码
const sendCode = async () => {
  // 先验证邮箱
  const emailItem = formRef.value.validateField('email');
  
  if (!registerForm.email) {
    ElMessage.warning('请先填写邮箱地址');
    return;
  }
  
  try {
    loading.value = true;
    
    // 调用发送验证码API
    await authApi.sendVerificationCode({
      email: registerForm.email,
      type: 'register'  // 标记是注册场景
    });
    
    ElMessage.success('验证码已发送，请查收邮件');
    isCodeSent.value = true;
    
    // 开始倒计时
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

// 注册方法
const handleRegister = async () => {
  try {
    await formRef.value.validate();
    
    loading.value = true;
    
    // 准备注册数据
    const userData = {
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      phone: registerForm.phone,
      realName: registerForm.realName,
      idCard: registerForm.idCard,
      role: registerForm.role
    };
    
    // 调用注册API
    await authApi.register({
      user: userData,
      code: verificationCode.value
    });
    
    // 注册成功
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (error) {
    console.error('注册失败:', error);
    ElMessage.error(error.message || '注册失败，请检查表单信息');
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
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>注册账号</h2>
      </div>

      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        label-position="top"
        class="register-form"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <!-- 邮箱和手机号并排 -->
        <div class="form-row">
          <el-form-item label="邮箱" prop="email" class="form-col">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone" class="form-col">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
            />
          </el-form-item>
        </div>

        <!-- 真实姓名和身份证号并排 -->
        <div class="form-row">
          <el-form-item label="真实姓名" prop="realName" class="form-col">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              prefix-icon="UserFilled"
            />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard" class="form-col">
            <el-input
              v-model="registerForm.idCard"
              placeholder="请输入身份证号"
              prefix-icon="Document"
            />
          </el-form-item>
        </div>

        <!-- 密码和确认密码并排 -->
        <div class="form-row">
          <el-form-item label="密码" prop="password" class="form-col">
            <el-input
              v-model="registerForm.password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword" class="form-col">
            <el-input
              v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              type="password"
              show-password
            />
          </el-form-item>
        </div>
        
        <el-form-item label="验证码">
          <div class="verification-code">
            <el-input 
              v-model="verificationCode" 
              placeholder="请输入验证码"
              style="width: calc(100% - 120px)"
            />
            <el-button 
              type="primary" 
              @click="sendCode" 
              :disabled="isCodeSent" 
              :loading="loading && !isCodeSent"
              style="width: 110px"
            >
              {{ isCodeSent ? `${countdown}秒后重新获取` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="register-button" :loading="loading">注册</el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？<el-link type="primary" @click="goToLogin">去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
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

.register-box {
  width: 100%;
  max-width: 600px;
  background-color: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin-bottom: 10px;
  color: #303133;
}

.register-header p {
  color: #909399;
}

.register-form {
  width: 100%;
}

.register-button {
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

.verification-code {
  display: flex;
  justify-content: space-between;
}

/* 表单布局相关样式 */
.form-row {
  display: flex;
  gap: 20px;
}

.form-col {
  flex: 1;
  min-width: 0;
}

/* 在小屏幕上改为垂直布局 */
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .register-box {
    max-width: 500px;
    padding: 30px 20px;
  }
}
</style>
