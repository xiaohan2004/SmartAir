<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import CustomerList from '@/components/service/CustomerList.vue';
import ChatBox from '@/components/chat/ChatBox.vue';
import { ElMessageBox, ElMessage } from 'element-plus';

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

// 处理关闭会话
const handleCloseConversation = () => {
  if (!currentChat.value) return;
  
  ElMessageBox.confirm(
    `确定要关闭与 ${currentChat.value.name} 的会话吗？`,
    '关闭会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      // 添加系统消息，提示会话已关闭
      chatMessages.value.push({
        content: '会话已关闭',
        type: 'system',
        time: new Date().toLocaleTimeString()
      });
      
      // 更新客户列表中的状态
      const index = customers.findIndex(c => c.id === currentChat.value.id);
      if (index !== -1) {
        customers[index].status = '已关闭';
      }
      
      ElMessage.success(`已关闭与 ${currentChat.value.name} 的会话`);
    })
    .catch(() => {
      // 用户取消关闭会话
    });
};

// 获取当前客户的订单
const getCustomerOrders = (customerName) => {
  return orders.filter(order => order.customer === customerName);
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
      <!-- 左侧区域：会话列表和聊天区域 -->
      <div class="left-section">
        <!-- 会话列表 -->
        <div class="conversation-list">
          <h3 class="list-title">会话列表</h3>
          <CustomerList 
            :customers="customers" 
            :current-customer-id="currentCustomerId"
            @select="selectCustomer"
          />
        </div>
        
        <!-- 聊天区域 -->
        <div class="chat-area">
          <div v-if="currentChat" class="chat-header">
            <span class="customer-name">{{ currentChat.name }}</span>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleCloseConversation" 
              class="close-chat-btn"
            >
              关闭会话
            </el-button>
          </div>
          
          <!-- 使用聊天组件 -->
          <div class="chat-wrapper" v-if="currentChat">
            <ChatBox 
              :messages="chatMessages" 
              placeholder="请输入回复内容" 
              empty-text="暂无消息" 
              user-type="service"
              @send="handleSendMessage"
              @closeConversation="handleCloseConversation"
            />
          </div>
          
          <div v-if="!currentChat" class="no-chat">
            <el-empty description="请选择一个客户开始聊天" />
          </div>
        </div>
      </div>
      
      <!-- 右侧区域：客户信息和订单管理 -->
      <div class="right-section">
        <!-- 客户信息区域 -->
        <div class="customer-info-area">
          <h2 class="section-title">客户信息</h2>
          <div v-if="currentChat" class="customer-details">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="客户姓名">{{ currentChat.name }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">138****1234</el-descriptions-item>
              <el-descriptions-item label="注册时间">2023-11-01</el-descriptions-item>
              <el-descriptions-item label="会员等级">普通会员</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-info">
            <el-empty description="请选择一个客户查看详情" />
          </div>
        </div>
        
        <!-- 订单管理区域 -->
        <div class="order-management-area">
          <h2 class="section-title">订单管理</h2>
          <div v-if="currentChat" class="customer-orders">
            <el-table :data="getCustomerOrders(currentChat.name)" style="width: 100%">
              <el-table-column prop="id" label="订单号" width="80" />
              <el-table-column prop="flight" label="航班" width="80" />
              <el-table-column prop="date" label="日期" width="100" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag
                    :type="scope.row.status === '已确认' ? 'success' : 
                           scope.row.status === '待确认' ? 'warning' : 'info'"
                  >
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default>
                  <el-button size="small" type="primary">处理</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else class="empty-info">
            <el-empty description="请选择一个客户查看订单" />
          </div>
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

/* 左侧区域样式 */
.left-section {
  width: 60%;
  height: 100%;
  display: flex;
  border-right: 1px solid #dcdfe6;
}

.conversation-list {
  width: 280px;
  height: 100%;
  background-color: white;
  border-right: 1px solid #ebeef5;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-title {
  padding: 15px;
  margin: 0;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: white;
  border-bottom: 1px solid #ebeef5;
}

.customer-name {
  font-weight: bold;
  font-size: 16px;
}

.close-chat-btn {
  opacity: 0.8;
  transition: opacity 0.3s;
}

.close-chat-btn:hover {
  opacity: 1;
}

.chat-wrapper {
  flex: 1;
  overflow: hidden;
  height: calc(100% - 60px);
}

.no-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  height: 100%;
}

/* 右侧区域样式 */
.right-section {
  width: 40%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.customer-info-area {
  flex: 1;
  padding: 20px;
  background-color: white;
  border-bottom: 1px solid #ebeef5;
  overflow: auto;
}

.order-management-area {
  flex: 1;
  padding: 20px;
  background-color: white;
  overflow: auto;
}

.section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
}

.empty-info {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}

.customer-details {
  margin-bottom: 20px;
}
</style> 