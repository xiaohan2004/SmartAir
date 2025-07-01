<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import CustomerList from '@/components/service/CustomerList.vue';
import ChatBox from '@/components/chat/ChatBox.vue';

const router = useRouter();
const username = ref('客服名称');
const activeMenu = ref('customers');
const message = ref('');
const currentChat = ref(null);
const chatMessages = ref([]);

// 侧边栏菜单项
const menuItems = [
  { index: 'customers', title: '客户信息', icon: 'User' },
  { index: 'orders', title: '订单管理', icon: 'Ticket' }
];

// 模拟客户列表
const customers = reactive([
  { id: 1, name: '张三', unread: 2, lastMessage: '请问我的机票可以改签吗？', time: '12:30' },
  { id: 2, name: '李四', unread: 0, lastMessage: '谢谢您的帮助', time: '昨天' },
  { id: 3, name: '王五', unread: 1, lastMessage: '什么时候能确认我的退款？', time: '昨天' }
]);

// 模拟订单列表
const orders = reactive([
  { id: 101, customer: '张三', flight: 'CA1234', date: '2023-12-15', status: '已确认' },
  { id: 102, customer: '李四', flight: 'MU5678', date: '2023-12-20', status: '待确认' },
  { id: 103, customer: '王五', flight: 'CZ8901', date: '2023-12-25', status: '已退款' }
]);

// 当前客户ID
const currentCustomerId = computed(() => currentChat.value?.id || null);

// 选择客户聊天
const selectCustomer = (customer) => {
  currentChat.value = customer;
  customer.unread = 0;
  // 模拟加载聊天记录
  chatMessages.value = [
    { content: '您好，我是客服，有什么可以帮助您的？', type: 'service', time: '12:00' },
    { content: customer.lastMessage, type: 'customer', time: customer.time }
  ];
};

// 发送消息处理
const handleSendMessage = (messageText) => {
  if (!currentChat.value) return;
  
  // 添加客服消息
  chatMessages.value.push({
    content: messageText,
    type: 'service',
    time: new Date().toLocaleTimeString()
  });
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};
</script>

<template>
  <div class="service-container">
    <!-- 使用通用顶部导航栏组件 -->
    <HeaderNav :username="username" />
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 左侧导航和列表 -->
      <div class="sidebar">
        <!-- 使用通用侧边栏组件 -->
        <SideMenu
          :menu-items="menuItems"
          :active-menu="activeMenu"
          @select="activeMenu = $event"
        />
        
        <!-- 客户列表 -->
        <div v-if="activeMenu === 'customers'" class="customer-list-container">
          <!-- 使用客户列表组件 -->
          <CustomerList 
            :customers="customers" 
            :current-customer-id="currentCustomerId"
            @select="selectCustomer"
          />
        </div>
        
        <!-- 订单列表 -->
        <div v-if="activeMenu === 'orders'" class="order-list">
          <el-table :data="orders" style="width: 100%">
            <el-table-column prop="id" label="订单号" width="80" />
            <el-table-column prop="customer" label="客户" width="70" />
            <el-table-column prop="flight" label="航班" width="80" />
            <el-table-column prop="date" label="日期" width="100" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === '已确认' ? 'success' : 
                         scope.row.status === '待确认' ? 'warning' : 'info'"
                >
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- 聊天区域 -->
      <div class="chat-area">
        <div v-if="currentChat" class="chat-header">
          <span>{{ currentChat.name }}</span>
        </div>
        
        <!-- 使用聊天组件 -->
        <div class="chat-wrapper" v-if="currentChat">
          <ChatBox 
            :messages="chatMessages" 
            placeholder="请输入回复内容" 
            empty-text="暂无消息" 
            user-type="service"
            @send="handleSendMessage"
          />
        </div>
        
        <div v-if="!currentChat" class="no-chat">
          <el-empty description="请选择一个客户开始聊天" />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.service-container {
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
  background-color: #f5f7fa;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  height: calc(100% - 60px);
}

.sidebar {
  width: 300px;
  height: 100%;
  background-color: white;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ebeef5;
  overflow: hidden;
}

.customer-list-container, .order-list {
  flex: 1;
  overflow: auto;
  height: 100%;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
  width: 100%;
}

.chat-wrapper {
  flex: 1;
  overflow: hidden;
  width: 100%;
  height: calc(100% - 60px);
}

.no-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  height: 100%;
  width: 100%;
}
</style> 