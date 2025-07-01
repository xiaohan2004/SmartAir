<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import HeaderNav from '@/components/common/HeaderNav.vue';
import ChatBox from '@/components/chat/ChatBox.vue';

const router = useRouter();
const username = ref('用户名');
const chatMessages = ref([]);

// 发送消息处理
const handleSendMessage = (messageText) => {
  // 添加用户消息
  chatMessages.value.push({
    content: messageText,
    type: 'user',
    time: new Date().toLocaleTimeString()
  });
  
  // 模拟AI回复
  setTimeout(() => {
    chatMessages.value.push({
      content: `您好，这是对"${messageText}"的回复`,
      type: 'ai',
      time: new Date().toLocaleTimeString()
    });
  }, 1000);
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};
</script>

<template>
  <div class="user-container">
    <!-- 使用通用顶部导航栏组件 -->
    <HeaderNav :username="username" />
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 使用聊天组件 -->
      <ChatBox 
        :messages="chatMessages" 
        placeholder="请输入您的问题" 
        empty-text="有什么能帮您？"
        user-type="user"
        @send="handleSendMessage"
      />
    </main>
  </div>
</template>

<style scoped>
.user-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: #f5f7fa;
  width: 100%;
  box-sizing: border-box;
  overflow: auto;
}
</style> 