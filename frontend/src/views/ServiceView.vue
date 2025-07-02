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

// 订单详情数据
const orderDetail = reactive({
  orderId: '',
  seatNo: '',
  status: 0,
  createdAt: '',
  userId: '',
  username: '',
  realName: '',
  phone: '',
  flightId: '',
  flightNo: '',
  departureCity: '',
  arrivalCity: '',
  scheduledDepartureTime: '',
  scheduledArrivalTime: '',
  airline: '',
  aircraftType: '',
  price: 0
});

// 订单详情弹窗控制
const detailDialogVisible = ref(false);
const detailLoading = ref(false);

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

    // 加载客服的会话列表 - 使用listAllConversationsByServiceUserId函数
    const conversationsResponse = await conversationApi.listAllConversationsByServiceUserId(serviceUserId.value);

    if (conversationsResponse.code === 200) {
      console.log('获取到的会话数据:', conversationsResponse.data);

      // 转换会话数据格式为客户列表格式
      customers.value = conversationsResponse.data.map(conv => ({
        id: conv.id,
        userId: conv.userId,
        name: conv.user ? conv.user.username : `用户${conv.userId}`,
        unread: 0, // 未读消息数，需要后端API支持
        lastMessage: conv.lastMessage,
        time: new Date(conv.updatedAt).toLocaleTimeString(),
        conversationUuid: conv.conversationUuid,
        status: conv.status === 0 ? '进行中' :
          conv.status === 1 ? '已结束' :
            conv.status === 2 ? '已转人工' : '未知'
      }));
    } else {
      ElMessage.warning(conversationsResponse.message || '没有获取到会话数据');
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
      // 获取会话的详细消息记录
      const messagesResponse = await conversationApi.getConversationMessages(customer.conversationUuid);

      if (messagesResponse.code === 200 && messagesResponse.data) {
        console.log('获取到的消息数据:', messagesResponse.data);

        // 只处理新格式：包含 messages 数组的对象
        if (messagesResponse.data.messages && Array.isArray(messagesResponse.data.messages)) {
          chatMessages.value = messagesResponse.data.messages.map(msg => ({
            content: msg.text,
            type: msg.speaker === 'assistant' || msg.speaker === 'service' ? 'service' : 'customer',
            time: msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : new Date().toLocaleTimeString()
          }));
        } else {
          // 如果数据结构不符合预期，显示一条系统消息
          chatMessages.value = [
            {
              content: '无法解析会话消息数据',
              type: 'system',
              time: new Date().toLocaleTimeString()
            }
          ];
          console.error('消息数据格式不符合预期:', messagesResponse.data);
        }
      } else {
        // 如果没有消息记录，至少显示一条系统消息
        chatMessages.value = [
          {
            content: customer.lastMessage || '会话开始',
            type: customer.lastMessage ? 'customer' : 'system',
            time: customer.time
          }
        ];
        ElMessage.warning(messagesResponse?.message || '没有获取到聊天记录');
      }

      // 获取客户详细信息
      if (customer.userId) {
        const userInfoResponse = await userApi.getUserInfo(customer.userId);
        if (userInfoResponse.code === 200) {
          // 更新客户信息
          customer.userInfo = userInfoResponse.data;
        }

        // 获取该用户的订单数据
        const ordersResponse = await orderApi.listOrdersWithDetail(customer.userId);
        if (ordersResponse.code === 200) {
          orders.value = ordersResponse.data;
          console.log('获取到用户订单数据:', ordersResponse.data);
        } else {
          orders.value = [];
          ElMessage.warning(ordersResponse.message || `没有获取到用户 ${customer.name} 的订单数据`);
        }
      } else {
        // 如果没有用户ID，清空订单列表
        orders.value = [];
      }

    }
  } catch (error) {
    console.error('加载会话失败:', error);
    ElMessage.error('加载会话失败: ' + (error.message || '未知错误'));
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

    // 使用新格式发送消息
    const messageData = {
      text: messageText,
      speaker: 'service'
    };

    // 调用API发送消息
    const response = await conversationApi.sendMessage(
      currentChat.value.conversationUuid,
      messageData
    );

    if (response.code !== 200) {
      throw new Error(response.message || '发送消息失败');
    }

    // 更新会话最后一条消息
    const index = customers.value.findIndex(c => c.conversationUuid === currentChat.value.conversationUuid);
    if (index !== -1) {
      customers.value[index].lastMessage = messageText;
      customers.value[index].time = new Date().toLocaleTimeString();
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送消息失败: ' + (error.message || '未知错误'));
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
const getCustomerOrders = () => {
  if (!currentChat.value) return [];
  return orders.value;
};

// 进入个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未知';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 获取状态类型
const getStatusType = (status) => {
  return status === 1 ? 'success' : 'danger';
};

// 获取状态名称
const getStatusName = (status) => {
  return status === 1 ? '已完成' : '已取消';
};

// 查看订单详情
const viewOrderDetail = async (orderId) => {
  detailLoading.value = true;
  detailDialogVisible.value = true;

  try {
    // 尝试调用获取详细订单信息的API
    let response;
    try {
      response = await orderApi.getOrderDetail(orderId);
    } catch (detailError) {
      console.warn('获取订单详情失败，尝试获取基本订单信息:', detailError);
      // 如果获取详情失败，尝试获取基本订单信息
      response = await orderApi.getOrderById(orderId);
    }

    if (response.code === 200) {
      // 更新订单详情数据
      const data = response.data;

      // 根据返回的数据结构设置订单详情
      Object.assign(orderDetail, {
        orderId: data.id || data.orderId,
        seatNo: data.seatNo || '',
        status: data.status,
        createdAt: data.createdAt,

        // 用户信息 - 可能来自关联数据或详情API
        userId: data.userId,
        username: data.username || (data.user ? data.user.username : `用户${data.userId}`),
        realName: data.realName || (data.user ? data.user.realName : ''),
        phone: data.phone || (data.user ? data.user.phone : ''),

        // 航班信息 - 可能来自关联数据或详情API
        flightId: data.flightId,
        flightNo: data.flightNo || (data.flight ? data.flight.flightNo : `航班${data.flightId}`),
        departureCity: data.departureCity || (data.flight ? data.flight.departureCity : ''),
        arrivalCity: data.arrivalCity || (data.flight ? data.flight.arrivalCity : ''),
        scheduledDepartureTime: data.scheduledDepartureTime || (data.flight ? data.flight.scheduledDepartureTime : ''),
        scheduledArrivalTime: data.scheduledArrivalTime || (data.flight ? data.flight.scheduledArrivalTime : ''),
        airline: data.airline || (data.flight ? data.flight.airline : ''),
        aircraftType: data.aircraftType || (data.flight ? data.flight.aircraftType : ''),
        price: data.price || (data.flight ? data.flight.price : 0)
      });

      console.log('订单详情:', orderDetail);
    } else {
      throw new Error(response.message || '获取订单详情失败');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败');
    detailDialogVisible.value = false;
  } finally {
    detailLoading.value = false;
  }
};

// 取消订单
const cancelOrder = async (order) => {
  if (order.status !== 1) {
    ElMessage.warning('只能取消未完成的订单');
    return;
  }

  try {
    ElMessageBox.confirm(
      `确定要取消订单 ${order.id} 吗？`,
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(async () => {
      try {
        const response = await orderApi.cancelOrder(order.id);

        if (response.code === 200) {
          ElMessage.success('订单已取消');

          // 更新订单状态
          const index = orders.value.findIndex(o => o.id === order.id);
          if (index !== -1) {
            orders.value[index].status = 2; // 设置为已取消
          }

          // 如果是在详情弹窗中取消的，也更新详情数据
          if (detailDialogVisible.value && orderDetail.orderId === order.id) {
            orderDetail.status = 2;
          }
        } else {
          throw new Error(response.message || '取消订单失败');
        }
      } catch (error) {
        console.error('取消订单失败:', error);
        ElMessage.error('取消订单失败: ' + (error.message || '未知错误'));
      }
    }).catch(() => {
      // 用户取消操作
    });
  } catch (error) {
    console.error('取消订单操作失败:', error);
    ElMessage.error('操作失败');
  }
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
            <el-table :data="getCustomerOrders()" style="width: 100%; height: calc(100% - 40px)" v-loading="loading">
              <el-table-column prop="id" label="订单号" width="80" />
              <el-table-column prop="flightId" label="航班ID" width="80" />
              <el-table-column prop="seatNo" label="座位号" width="80" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag
                    :type="getStatusType(scope.row.status)"
                  >
                    {{ getStatusName(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="160">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    @click="viewOrderDetail(scope.row.id)"
                  >
                    详情
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="cancelOrder(scope.row)"
                    :disabled="scope.row.status !== 1"
                  >
                    取消
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="getCustomerOrders().length === 0" class="no-orders">
              <el-empty description="暂无订单数据" />
            </div>
          </div>
          <div v-else class="empty-info">
            <el-empty description="请选择一个客户查看订单" />
          </div>
        </div>
      </div>
    </main>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="650px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID" :span="2">{{ orderDetail.orderId }}</el-descriptions-item>
          <el-descriptions-item label="座位号">{{ orderDetail.seatNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(orderDetail.status)">{{ getStatusName(orderDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatDateTime(orderDetail.createdAt) }}</el-descriptions-item>

          <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ orderDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ orderDetail.realName || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.phone || '未提供' }}</el-descriptions-item>

          <el-descriptions-item label="航班信息" :span="2" class="flight-info-header">
            <div class="flight-header">
              <span class="flight-no">{{ orderDetail.flightNo }}</span>
              <span class="airline">{{ orderDetail.airline }}</span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="出发城市">{{ orderDetail.departureCity }}</el-descriptions-item>
          <el-descriptions-item label="到达城市">{{ orderDetail.arrivalCity }}</el-descriptions-item>
          <el-descriptions-item label="计划出发时间">{{ formatDateTime(orderDetail.scheduledDepartureTime) }}</el-descriptions-item>
          <el-descriptions-item label="计划到达时间">{{ formatDateTime(orderDetail.scheduledArrivalTime) }}</el-descriptions-item>
          <el-descriptions-item label="机型">{{ orderDetail.aircraftType }}</el-descriptions-item>
          <el-descriptions-item label="票价">¥{{ orderDetail.price }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            type="danger"
            @click="cancelOrder({id: orderDetail.orderId, status: orderDetail.status})"
            :disabled="orderDetail.status !== 1"
          >
            取消订单
          </el-button>
        </span>
      </template>
    </el-dialog>
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

.customer-orders {
  height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
}

.flight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flight-no {
  font-weight: bold;
  font-size: 16px;
}

.airline {
  color: #606266;
}

.flight-info-header {
  background-color: #f5f7fa;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
