<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';
import ChatBox from '@/components/chat/ChatBox.vue';
import { conversationApi } from '@/api';
import { parseJwt } from '@/utils/jwt';

const router = useRouter();
const username = ref('用户名');
const chatMessages = ref([]);
const conversationActive = ref(true);
const loading = ref(false);
const conversationUuid = ref('');

// 初始化会话
const initConversation = async () => {
  try {
    loading.value = true;
    
    // 从token中解析用户信息
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.error('未找到用户信息，请重新登录');
      router.push('/login');
      return;
    }
    
    const userData = parseJwt(token);
    username.value = userData.username;
    const userId = userData.userId;
    
    // 检查是否有进行中的会话
    const activeConvResponse = await conversationApi.getUserActiveConversation(userId);
    
    // 如果有进行中的会话，加载会话消息
    if (activeConvResponse.data) {
      const conversation = activeConvResponse.data;
      conversationUuid.value = conversation.uuid;
      
      // TODO: 这里应该调用获取会话消息的API，现在模拟一些消息
      chatMessages.value = [
        {
          content: conversation.lastMessage || '欢迎回来，有什么可以帮助您的？',
          type: 'ai',
          time: new Date(conversation.updatedAt).toLocaleTimeString()
        }
      ];
    } else {
      // 如果没有进行中的会话，创建新的会话
      const createResponse = await conversationApi.createConversation({
        userId: userId,
        initialMessage: '您好，欢迎使用智能客服系统，有什么可以帮助您的？'
      });
      
      // 保存会话UUID
      conversationUuid.value = createResponse.data.uuid;
      
      // 添加初始消息
      chatMessages.value = [
        {
          content: '您好，欢迎使用智能客服系统，有什么可以帮助您的？',
          type: 'ai',
          time: new Date().toLocaleTimeString()
        }
      ];
    }
  } catch (error) {
    console.error('初始化会话失败:', error);
    ElMessage.error(error.message || '初始化会话失败');
    
    // 创建本地会话，防止页面崩溃
    chatMessages.value = [
      {
        content: '欢迎使用智能客服系统，有什么可以帮助您的？(本地模式)',
        type: 'ai',
        time: new Date().toLocaleTimeString()
      }
    ];
  } finally {
    loading.value = false;
  }
};

// 发送消息处理
const handleSendMessage = async (messageText) => {
  if (!conversationActive.value) {
    ElMessage.warning('会话已关闭，无法发送消息');
    return;
  }
  
  try {
    // 添加用户消息
    chatMessages.value.push({
      content: messageText,
      type: 'user',
      time: new Date().toLocaleTimeString()
    });
    
    loading.value = true;
    
    // 调用后端API发送消息
    // TODO: 这里应该实现发送消息的API，现在模拟AI回复
    
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    if (conversationActive.value) {
      // 添加AI回复
      chatMessages.value.push({
        content: `您好，这是对"${messageText}"的回复`,
        type: 'ai',
        time: new Date().toLocaleTimeString()
      });
      
      // 更新会话最后一条消息
      if (conversationUuid.value) {
        // 省略实际API调用，应该有一个updateLastMessage的API
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败');
  } finally {
    loading.value = false;
  }
};

// 处理关闭会话
const handleCloseConversation = async () => {
  ElMessageBox.confirm(
    '确定要关闭当前会话吗？关闭后将无法继续发送消息。',
    '关闭会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        loading.value = true;
        
        if (conversationUuid.value) {
          // 调用后端API关闭会话
          await conversationApi.closeConversation(conversationUuid.value);
        }
        
        conversationActive.value = false;
        
        // 添加系统消息，提示会话已关闭
        chatMessages.value.push({
          content: '会话已关闭。如需继续咨询，请刷新页面开始新的会话。',
          type: 'ai',
          time: new Date().toLocaleTimeString()
        });
        
        ElMessage.success('会话已关闭');
      } catch (error) {
        console.error('关闭会话失败:', error);
        ElMessage.error(error.message || '关闭会话失败');
      } finally {
        loading.value = false;
      }
    })
    .catch(() => {
      // 用户取消关闭会话
    });
};

// 处理转人工客服
const handleTransferToService = async () => {
  try {
    loading.value = true;
    
    if (!conversationUuid.value) {
      ElMessage.warning('无效的会话');
      return;
    }
    
    // 调用后端API转接到人工客服
    await conversationApi.transferToService(conversationUuid.value, {
      serviceUserId: null // 系统自动分配客服
    });
    
    // 添加系统消息
    chatMessages.value.push({
      content: '正在为您转接人工客服，请稍候...',
      type: 'ai',
      time: new Date().toLocaleTimeString()
    });
    
    ElMessage.success('已转接到人工客服');
  } catch (error) {
    console.error('转接客服失败:', error);
    ElMessage.error(error.message || '转接客服失败');
  } finally {
    loading.value = false;
  }
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 组件挂载时初始化会话
onMounted(() => {
  initConversation();
});
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
          :loading="loading"
          @send="handleSendMessage"
          @closeConversation="handleCloseConversation"
          @transferToService="handleTransferToService"
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