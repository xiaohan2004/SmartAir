<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import CustomerList from '@/components/service/CustomerList.vue';
import ChatBox from '@/components/chat/ChatBox.vue';
import { conversationApi, orderApi, userApi } from '@/api';
import { parseJwt } from '@/utils/jwt';

const router = useRouter();
const username = ref('客服名称');
const activeMenu = ref('customers');
const message = ref('');
const currentChat = ref(null);
const chatMessages = ref([]);
const loading = ref(false);
const serviceUserId = ref(null);

// 侧边栏菜单项
const menuItems = [
  { index: 'customers', title: '客户信息', icon: 'User' },
  { index: 'orders', title: '订单管理', icon: 'Ticket' }
];

// 客户会话列表
const customers = ref([]);

// 订单列表
const orders = ref([]);

// 当前客户ID
const currentCustomerId = computed(() => currentChat.value?.userId || null);

// 初始化数据
const initData = async () => {
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
    serviceUserId.value = userData.userId;
    
    // 加载客服的会话列表
    const conversationsResponse = await conversationApi.listConversationsByServiceUserId(serviceUserId.value);
    
    if (conversationsResponse.code === 200) {
      // 转换会话数据格式为客户列表格式
      customers.value = conversationsResponse.data.map(conv => ({
        id: conv.id,
        userId: conv.userId,
        name: conv.user ? conv.user.username : `用户${conv.userId}`,
        unread: 0, // 未读消息数，需要后端API支持
        lastMessage: conv.lastMessage,
        time: new Date(conv.updatedAt).toLocaleTimeString(),
        conversationUuid: conv.conversationUuid,
        status: conv.status === 1 ? '进行中' : conv.status === 2 ? '已转人工' : '已关闭'
      }));
    }
    
    // 加载客服相关订单
    // 这里我们获取所有订单，实际中可能需要根据客服负责的订单筛选
    const ordersResponse = await orderApi.listAllOrders();
    
    if (ordersResponse.code === 200) {
      orders.value = ordersResponse.data;
    }
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error(error.message || '加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 选择客户聊天
const selectCustomer = async (customer) => {
  try {
    loading.value = true;
    currentChat.value = customer;
    
    // 重置未读消息
    if (customer.unread > 0) {
      customer.unread = 0;
    }
    
    // 获取与该客户的聊天记录
    const conversationResponse = await conversationApi.getConversation(customer.conversationUuid);
    
    if (conversationResponse.code === 200) {
      // 这里需要有获取会话消息的API
      // 暂时只添加最后一条消息
      chatMessages.value = [
        { 
          content: customer.lastMessage || '会话开始', 
          type: customer.lastMessage ? 'customer' : 'system', 
          time: customer.time 
        }
      ];
      
      // 获取客户详细信息
      if (customer.userId) {
        const userInfoResponse = await userApi.getUserInfo(customer.userId);
        if (userInfoResponse.code === 200) {
          // 更新客户信息
          customer.userInfo = userInfoResponse.data;
        }
      }
    }
  } catch (error) {
    console.error('加载会话失败:', error);
    ElMessage.error('加载会话失败');
  } finally {
    loading.value = false;
  }
};

// 发送消息处理
const handleSendMessage = async (messageText) => {
  if (!currentChat.value) return;
  
  try {
    // 添加客服消息到界面
    const serviceMsg = {
      content: messageText,
      type: 'service',
      time: new Date().toLocaleTimeString()
    };
    chatMessages.value.push(serviceMsg);
    
    // 调用API发送消息
    const response = await fetch(`/api/conversation/${currentChat.value.conversationUuid}/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': localStorage.getItem('token')
      },
      body: JSON.stringify({
        message: messageText,
        userId: serviceUserId.value,
        isService: true
      })
    });
    
    const data = await response.json();
    if (data.code !== 200) {
      throw new Error(data.message || '发送消息失败');
    }
    
    // 更新会话最后一条消息
    const index = customers.value.findIndex(c => c.conversationUuid === currentChat.value.conversationUuid);
    if (index !== -1) {
      customers.value[index].lastMessage = messageText;
      customers.value[index].time = new Date().toLocaleTimeString();
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败');
  }
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
    .then(async () => {
      try {
        loading.value = true;
        
        // 调用API关闭会话
        const response = await conversationApi.closeConversation(currentChat.value.conversationUuid);
        
        if (response.code === 200) {
          // 添加系统消息，提示会话已关闭
          chatMessages.value.push({
            content: '会话已关闭',
            type: 'system',
            time: new Date().toLocaleTimeString()
          });
          
          // 更新客户列表中的状态
          const index = customers.value.findIndex(c => c.id === currentChat.value.id);
          if (index !== -1) {
            customers.value[index].status = '已关闭';
          }
          
          ElMessage.success(`已关闭与 ${currentChat.value.name} 的会话`);
        } else {
          throw new Error(response.message || '关闭会话失败');
        }
      } catch (error) {
        console.error('关闭会话失败:', error);
        ElMessage.error('关闭会话失败');
      } finally {
        loading.value = false;
      }
    })
    .catch(() => {
      // 用户取消关闭会话
    });
};

// 获取当前客户的订单
const getCustomerOrders = (customerName) => {
  if (!currentChat.value || !currentChat.value.userId) return [];
  
  // 根据userId过滤订单
  return orders.value.filter(order => order.userId === currentChat.value.userId);
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 组件挂载时初始化数据
onMounted(initData);
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
              :disabled="currentChat.status === '已关闭'"
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
              :disabled="currentChat.status === '已关闭'"
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
          <div v-if="currentChat && currentChat.userInfo" class="customer-details">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="客户姓名">{{ currentChat.userInfo.realName || currentChat.name }}</el-descriptions-item>
              <el-descriptions-item label="联系方式">{{ currentChat.userInfo.phone || '未提供' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱地址">{{ currentChat.userInfo.email || '未提供' }}</el-descriptions-item>
              <el-descriptions-item label="会员等级">
                {{ 
                  currentChat.userInfo.memberLevel === 1 ? '普通会员' : 
                  currentChat.userInfo.memberLevel === 2 ? '白银会员' : 
                  currentChat.userInfo.memberLevel === 3 ? '黄金会员' : 
                  currentChat.userInfo.memberLevel === 4 ? '白金会员' : 
                  currentChat.userInfo.memberLevel === 5 ? '钻石会员' : '未知'
                }}
              </el-descriptions-item>
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
            <el-table :data="getCustomerOrders(currentChat.name)" style="width: 100%" v-loading="loading">
              <el-table-column prop="id" label="订单号" width="80" />
              <el-table-column prop="flightId" label="航班ID" width="80" />
              <el-table-column prop="seatNo" label="座位号" width="80" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag
                    :type="scope.row.status === 1 ? 'success' : 'info'"
                  >
                    {{ scope.row.status === 1 ? '已完成' : '已取消' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="router.push(`/admin/order/${scope.row.id}`)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="getCustomerOrders(currentChat.name).length === 0" class="no-orders">
              <el-empty description="暂无订单数据" />
            </div>
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