<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  username: {
    type: String,
    default: '用户'
  }
});

const emit = defineEmits(['logout']);
const router = useRouter();

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 退出登录
const logout = () => {
  emit('logout');
  router.push('/login');
};

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    goToProfile();
  } else if (command === 'logout') {
    logout();
  }
};
</script>

<template>
  <header class="header">
    <div class="logo">SmartAir 智能航空</div>
    <el-dropdown trigger="hover" @command="handleCommand">
      <div class="user-info">
        <el-avatar :size="32" icon="User" />
        <span>{{ username }}</span>
        <el-icon><ArrowDown /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">
            <el-icon><User /></el-icon>个人中心
          </el-dropdown-item>
          <el-dropdown-item command="logout" divided class="logout-item">
            <el-icon style="color: #f56c6c;"><SwitchButton /></el-icon>
            <span style="color: #f56c6c;">退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </header>
</template>

<style scoped>
.header {
  height: 60px;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 10;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info span {
  margin-left: 8px;
  margin-right: 5px;
}

.logout-item :deep(.el-dropdown-menu__item:hover) {
  background-color: #fef0f0 !important;
}
</style>
