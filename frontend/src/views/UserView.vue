<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';
import ChatBox from '@/components/chat/ChatBox.vue';
import {conversationApi} from '@/api';
import {parseJwt} from '@/utils/jwt';

const router = useRouter();
const username = ref('用户名');
const chatMessages = ref([]);
const conversationActive = ref(true);
const loading = ref(false);
const conversationUuid = ref('');
const currentUserId = ref(null);
const isTransferToService = ref(false);

// SSE 相关
let eventSource = null;
const isReceivingStream = ref(false);
const currentStreamMessage = ref('');

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
    currentUserId.value = userData.userId;

    // 检查是否有进行中的会话
    const activeConvResponse = await conversationApi.getUserActiveConversation(currentUserId.value);

    // 如果有进行中的会话，加载会话消息
    if (activeConvResponse.data) {
      const conversation = activeConvResponse.data;
      conversationUuid.value = conversation.conversationUuid;

      // 获取会话消息记录
      try {
        const messagesResponse = await conversationApi.getConversationMessages(conversation.conversationUuid);
        if (messagesResponse.code === 200 && messagesResponse.data && messagesResponse.data.messages) {
          chatMessages.value = messagesResponse.data.messages.map(msg => ({
            content: msg.text,
            type: msg.speaker === 'user' ? 'user' : 'ai',
            time: msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString()
          }));
        } else {
          // 如果没有消息记录，添加欢迎消息
          chatMessages.value = [
            {
              content: '欢迎回来，有什么可以帮助您的？',
              type: 'ai',
              time: new Date().toLocaleTimeString()
            }
          ];
        }
      } catch (error) {
        console.error('获取会话消息失败:', error);
        chatMessages.value = [
          {
            content: '欢迎回来，有什么可以帮助您的？',
            type: 'ai',
            time: new Date().toLocaleTimeString()
          }
        ];
      }
    } else {
      // 如果没有进行中的会话，创建新的会话
      const createResponse = await conversationApi.createConversation({
        userId: currentUserId.value,
        initialMessage: '您好，欢迎使用智能客服系统，有什么可以帮助您的？'
      });

      // 保存会话UUID
      conversationUuid.value = createResponse.data.conversationUuid;

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

// 记录消息到后端
const logMessage = async (messageText, speaker) => {
  try {
    if (!conversationUuid.value) return;

    const messageData = {
      text: messageText,
      speaker: speaker,
      timestamp: new Date().toISOString()
    };

    await conversationApi.sendMessage(conversationUuid.value, messageData);
  } catch (error) {
    console.error('记录消息失败:', error);
  }
};

// 创建SSE连接接收AI流式响应
const createSSEConnection = (message) => {
  return new Promise((resolve, reject) => {
    // 关闭之前的连接
    if (eventSource) {
      eventSource.close();
    }

    const url = `http://localhost:8080/api/ai/generateStreamAsString?message=${encodeURIComponent(message)}`;
    eventSource = new EventSource(url);

    let aiResponse = '';
    let aiMessageIndex = -1;

    // 先添加一个空的AI消息占位
    const aiMsg = {
      content: '',
      type: 'ai',
      time: new Date().toLocaleTimeString()
    };
    chatMessages.value.push(aiMsg);
    aiMessageIndex = chatMessages.value.length - 1;

    eventSource.onmessage = (event) => {
      const data = event.data;

      if (data === '[complete]') {
        // 流式响应完成
        eventSource.close();
        eventSource = null;
        isReceivingStream.value = false;

        // 记录AI回复到后端
        logMessage(aiResponse, 'assistant');

        resolve(aiResponse);
      } else {
        // 累积AI响应内容
        aiResponse += data;

        // 实时更新界面上的AI消息
        if (aiMessageIndex >= 0) {
          chatMessages.value[aiMessageIndex].content = aiResponse;
        }
      }
    };

    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error);
      eventSource.close();
      eventSource = null;
      isReceivingStream.value = false;

      // 如果没有收到任何响应，显示错误消息
      if (!aiResponse) {
        if (aiMessageIndex >= 0) {
          chatMessages.value[aiMessageIndex].content = '抱歉，AI服务暂时不可用，请稍后再试。';
        }
      }

      reject(error);
    };

    eventSource.onopen = () => {
      isReceivingStream.value = true;
    };
  });
};

// 发送消息处理
const handleSendMessage = async (messageText) => {
  if (!conversationActive.value) {
    ElMessage.warning('会话已关闭，无法发送消息');
    return;
  }

  try {
    // 添加用户消息到界面
    const userMsg = {
      content: messageText,
      type: 'user',
      time: new Date().toLocaleTimeString()
    };
    chatMessages.value.push(userMsg);

    // 记录用户消息到后端
    await logMessage(messageText, 'user');

    if (!isTransferToService.value) {
      loading.value = true;
      // 使用SSE接收AI流式响应
      await createSSEConnection(messageText);
    }

  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败: ' + (error.message || '未知错误'));
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

        // 关闭SSE连接
        if (eventSource) {
          eventSource.close();
          eventSource = null;
        }

        if (conversationUuid.value) {
          // 调用后端API关闭会话
          await conversationApi.closeConversation(conversationUuid.value);
        }

        conversationActive.value = false;

        // 添加系统消息，提示会话已关闭
        chatMessages.value.push({
          content: '会话已关闭。如需继续咨询，请刷新页面开始新的会话。',
          type: 'system',
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
  ElMessageBox.confirm(
    '确定要转接到人工客服吗？转接后将由人工客服为您服务。',
    '转接人工客服',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
    .then(async () => {
      try {
        loading.value = true;

        if (!conversationUuid.value) {
          ElMessage.warning('无效的会话');
          return;
        }

        // 关闭SSE连接
        if (eventSource) {
          eventSource.close();
          eventSource = null;
        }

        // 调用后端API转接到人工客服
        const response = await conversationApi.transferConversation(conversationUuid.value, {
          serviceUserId: null // 系统自动分配客服
        });

        if (response.code === 200) {
          // 添加系统消息
          chatMessages.value.push({
            content: '正在为您转接人工客服，请稍候...',
            type: 'system',
            time: new Date().toLocaleTimeString()
          });

          // 记录转接消息
          await logMessage('用户请求转接人工客服', 'service');

          isTransferToService.value = true;

          ElMessage.success('已转接到人工客服，请等待客服接入');
        } else {
          throw new Error(response.message || '转接失败');
        }
      } catch (error) {
        console.error('转接客服失败:', error);
        ElMessage.error(error.message || '转接客服失败');
      } finally {
        loading.value = false;
      }
    })
    .catch(() => {
      // 用户取消转接
    });
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 组件卸载时清理SSE连接
onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
});

// 组件挂载时初始化会话
onMounted(() => {
  initConversation();
});
</script>

<template>
  <div class="user-container">
    <!-- 使用通用顶部导航栏组件 -->
    <HeaderNav :username="username"/>

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
          :disabled="!conversationActive || isReceivingStream"
          @send="handleSendMessage"
          @closeConversation="handleCloseConversation"
          @transferToService="handleTransferToService"
        />

        <!-- 流式响应状态提示 -->
        <div v-if="isReceivingStream" class="stream-status">
          <el-icon class="is-loading">
            <Loading/>
          </el-icon>
          <span>AI正在思考中...</span>
        </div>
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
  position: relative;
}

.stream-status {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  background-color: rgba(225, 243, 216, 0.95);
  border-radius: 20px;
  color: #67c23a;
  font-size: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(4px);
  z-index: 10;
}

.stream-status .el-icon {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }

  .chat-container {
    max-width: 100%;
  }

  .stream-status {
    bottom: 10px;
    left: 10px;
    right: 10px;
    transform: none;
    font-size: 13px;
    padding: 10px 16px;
  }
}
</style>
