<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';
import ChatBox from '@/components/chat/ChatBox.vue';

const router = useRouter();
const username = ref('用户名');
const chatMessages = ref([]);
const conversationActive = ref(true);

// 发送消息处理
const handleSendMessage = (messageText) => {
  if (!conversationActive.value) {
    ElMessage.warning('会话已关闭，无法发送消息');
    return;
  }
  
  // 添加用户消息
  chatMessages.value.push({
    content: messageText,
    type: 'user',
    time: new Date().toLocaleTimeString()
  });
  
  // 模拟AI回复
  setTimeout(() => {
    if (conversationActive.value) {
      chatMessages.value.push({
        content: `您好，这是对"${messageText}"的回复`,
        type: 'ai',
        time: new Date().toLocaleTimeString()
      });
    }
  }, 1000);
};

// 处理关闭会话
const handleCloseConversation = () => {
  ElMessageBox.confirm(
    '确定要关闭当前会话吗？关闭后将无法继续发送消息。',
    '关闭会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      conversationActive.value = false;
      
      // 添加系统消息，提示会话已关闭
      chatMessages.value.push({
        content: '会话已关闭。如需继续咨询，请刷新页面开始新的会话。',
        type: 'ai',
        time: new Date().toLocaleTimeString()
      });
      
      ElMessage.success('会话已关闭');
    })
    .catch(() => {
      // 用户取消关闭会话
    });
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
      <div class="chat-container">
        <!-- 使用聊天组件 -->
        <ChatBox 
          :messages="chatMessages" 
          placeholder="请输入您的问题" 
          empty-text="有什么能帮您？"
          user-type="user"
          @send="handleSendMessage"
          @closeConversation="handleCloseConversation"
        />
      </div>
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

.chat-container {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style> 