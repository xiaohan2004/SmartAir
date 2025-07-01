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

const emit = defineEmits(['send']);

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
</script>

<template>
  <div class="chat-box">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" v-if="messages.length > 0">
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="message"
        :class="{ 
          'message-user': (userType === 'user' && msg.type === 'user') || (userType === 'service' && msg.type === 'service'), 
          'message-ai': (userType === 'user' && msg.type === 'ai'),
          'message-customer': (userType === 'service' && msg.type === 'customer')
        }"
      >
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ msg.time }}</div>
      </div>
    </div>
    
    <!-- 空聊天提示 -->
    <div class="empty-chat" v-else>
      <el-empty :description="emptyText">
        <slot name="quick-actions">
          <el-button type="primary" @click="quickReply('我想订机票')">订机票</el-button>
          <el-button @click="quickReply('如何改签')">改签问题</el-button>
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