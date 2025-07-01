<script setup>
import { ref, defineProps, defineEmits } from 'vue';

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请输入消息'
  },
  emptyText: {
    type: String,
    default: '暂无消息'
  },
  userType: {
    type: String,
    default: 'user' // 'user' 或 'service'
  }
});

const emit = defineEmits(['send', 'closeConversation']);

const message = ref('');

// 发送消息
const sendMessage = () => {
  if (message.value.trim() === '') return;
  
  emit('send', message.value);
  message.value = '';
};

// 快速回复按钮点击
const quickReply = (text) => {
  message.value = text;
  sendMessage();
};

// 关闭会话
const closeConversation = () => {
  emit('closeConversation');
};
</script>

<template>
  <div class="chat-box">
    <!-- 操作按钮区域 - 仅在用户界面显示 -->
    <div class="chat-actions" v-if="userType === 'user'">
      <el-button type="danger" size="small" @click="closeConversation" class="close-btn">
        关闭会话
      </el-button>
    </div>
    
    <!-- 聊天消息区域 -->
    <div class="chat-messages" v-if="messages.length > 0">
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="message"
        :class="{ 
          'message-user': (userType === 'user' && msg.type === 'user') || (userType === 'service' && msg.type === 'service'), 
          'message-ai': (userType === 'user' && msg.type === 'ai'),
          'message-customer': (userType === 'service' && msg.type === 'customer'),
          'message-system': msg.type === 'system'
        }"
      >
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ msg.time }}</div>
      </div>
    </div>
    
    <!-- 空聊天提示 -->
    <div class="empty-chat" v-else>
      <div v-if="userType === 'user'" class="welcome-message">
        <h1 class="welcome-title">有什么能帮您？</h1>
      </div>
      <el-empty v-else :description="emptyText">
        <slot name="quick-actions">
          <el-button type="primary" @click="quickReply('您好，有什么可以帮助您的？')">发送问候</el-button>
        </slot>
      </el-empty>
    </div>
    
    <!-- 输入框 -->
    <div class="input-container">
      <el-input
        v-model="message"
        :placeholder="placeholder"
        type="textarea"
        :rows="3"
        @keyup.enter.ctrl="sendMessage"
      />
      <el-button type="primary" @click="sendMessage" :disabled="message.trim() === ''">
        发送
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  max-width: 800px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chat-actions {
  display: flex;
  justify-content: flex-end;
  padding: 10px;
  background-color: transparent;
  border-bottom: 1px solid #ebeef5;
}

.close-btn {
  opacity: 0.7;
  transition: opacity 0.3s;
}

.close-btn:hover {
  opacity: 1;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.message {
  max-width: 80%;
  margin-bottom: 15px;
  padding: 10px 15px;
  border-radius: 8px;
  position: relative;
}

.message-user {
  align-self: flex-end;
  background-color: #ecf5ff;
  color: #303133;
  margin-left: auto;
}

.message-ai, .message-customer {
  align-self: flex-start;
  background-color: #f4f4f5;
  color: #303133;
}

.message-system {
  align-self: center;
  background-color: #f0f9eb;
  color: #67c23a;
  max-width: 90%;
  text-align: center;
  padding: 5px 15px;
  border-radius: 16px;
  font-size: 13px;
}

.message-content {
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.empty-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
}

.welcome-message {
  text-align: center;
}

.welcome-title {
  font-size: 36px;
  color: #303133;
  margin-bottom: 20px;
  font-weight: 500;
}

.input-container {
  display: flex;
  align-items: flex-end;
  padding: 15px;
  background-color: white;
  border-top: 1px solid #ebeef5;
  width: 100%;
  box-sizing: border-box;
}

.input-container .el-input {
  margin-right: 10px;
  flex: 1;
}

.input-container .el-button {
  height: 40px;
}
</style> 