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
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['send', 'closeConversation', 'transferToService']);

const message = ref('');

// 发送消息
const sendMessage = () => {
  if (message.value.trim() === '' || props.disabled) return;
  
  emit('send', message.value);
  message.value = '';
};

// 快速回复按钮点击
const quickReply = (text) => {
  if (props.disabled) return;
  message.value = text;
  sendMessage();
};

// 关闭会话
const closeConversation = () => {
  emit('closeConversation');
};

// 转接人工客服
const transferToService = () => {
  emit('transferToService');
};
</script>

<template>
  <div class="chat-box">
    <!-- 操作按钮区域 - 仅在用户界面显示 -->
    <div class="chat-actions" v-if="userType === 'user'">
      <el-button 
        type="primary" 
        size="small" 
        @click="transferToService" 
        class="action-btn"
        :loading="loading"
        :disabled="disabled"
      >
        人工客服
      </el-button>
      <el-button 
        type="danger" 
        size="small" 
        @click="closeConversation" 
        class="action-btn"
        :loading="loading"
        :disabled="disabled"
      >
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
        <p class="welcome-subtitle">您可以询问任何问题，我会尽力为您解答</p>
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
        :disabled="disabled"
        @keyup.enter.ctrl="sendMessage"
      />
      <el-button 
        type="primary" 
        @click="sendMessage" 
        :disabled="message.trim() === '' || disabled"
        :loading="loading"
      >
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
  gap: 10px;
  padding: 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
  border-radius: 8px 8px 0 0;
}

.action-btn {
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
  background-color: #f8f9fa;
}

.message {
  max-width: 80%;
  margin-bottom: 15px;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-user {
  align-self: flex-end;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  margin-left: auto;
}

.message-ai, .message-customer {
  align-self: flex-start;
  background-color: white;
  color: #303133;
  border: 1px solid #e4e7ed;
}

.message-system {
  align-self: center;
  background-color: #f0f9eb;
  color: #67c23a;
  max-width: 90%;
  text-align: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  border: 1px solid #c2e7b0;
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6px;
  text-align: right;
}

.message-ai .message-time,
.message-customer .message-time {
  color: #909399;
}

.empty-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  background-color: #f8f9fa;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.welcome-title {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 500;
}

.welcome-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.input-container {
  display: flex;
  align-items: flex-end;
  padding: 15px;
  background-color: white;
  border-top: 1px solid #ebeef5;
  width: 100%;
  box-sizing: border-box;
  border-radius: 0 0 8px 8px;
}

.input-container .el-input {
  margin-right: 12px;
  flex: 1;
}

.input-container .el-button {
  height: 40px;
  min-width: 80px;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>