<script setup>
import { ref, reactive, onMounted } from 'vue';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import StatCard from '@/components/admin/StatCard.vue';
import ConversationManagement from '@/components/admin/ConversationManagement.vue';
import FlightsManagement from '@/components/admin/FlightsManagement.vue';
import OrdersManagement from '@/components/admin/OrdersManagement.vue';
import PromptsManagement from '@/components/admin/PromptsManagement.vue';
import KnowledgeManagement from '@/components/admin/KnowledgeManagement.vue';
import AccountsManagement from '@/components/admin/AccountsManagement.vue';
import LogsManagement from '@/components/admin/LogsManagement.vue';
import { parseJwt } from '@/utils/jwt';
import { flightApi, orderApi, conversationApi } from '@/api';
import { ElMessage } from 'element-plus';

const username = ref('管理员');
const activeMenu = ref('dashboard');
const loading = ref(false);

// 仪表盘数据
const dashboardData = reactive({
  stats: [
    { title: '今日航班', value: 0, icon: 'Location', color: '#409eff' },
    { title: '今日订单', value: 0, icon: 'Ticket', color: '#67c23a' },
    { title: '活跃用户', value: 0, icon: 'User', color: '#e6a23c' },
    { title: '客服工单', value: 0, icon: 'Service', color: '#f56c6c' }
  ]
});

// 从 token 中获取用户信息
const loadUserInfo = () => {
  try {
    const token = localStorage.getItem('token');
    if (token) {
      const userData = parseJwt(token);
      if (userData && userData.username) {
        username.value = userData.username;
      }
    }
  } catch (error) {
    console.error('获取用户信息失败', error);
  }
};

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    loading.value = true;
    
    // 获取航班数据
    const flightsResponse = await flightApi.listFlights();
    if (flightsResponse.code === 200) {
      const flights = flightsResponse.data;
      // 统计今日航班数（假设根据当前日期过滤）
      const today = new Date().toISOString().split('T')[0];
      const todayFlights = flights.filter(flight => 
        flight.scheduledDepartureTime && flight.scheduledDepartureTime.startsWith(today)
      );
      dashboardData.stats[0].value = todayFlights.length;
    }
    
    // 获取订单数据
    const ordersResponse = await orderApi.listAllOrders();
    if (ordersResponse.code === 200) {
      const orders = ordersResponse.data;
      // 统计今日订单数
      const today = new Date().toISOString().split('T')[0];
      const todayOrders = orders.filter(order => 
        order.createdAt && order.createdAt.startsWith(today)
      );
      dashboardData.stats[1].value = todayOrders.length;
    }
    
    // 获取活跃用户数（这里需要根据实际API调整）
    try {
      const response = await fetch('/api/admin/stats/activeUsers');
      const data = await response.json();
      if (data.code === 200) {
        dashboardData.stats[2].value = data.data || 0;
      }
    } catch (error) {
      console.error('获取活跃用户数失败', error);
      // 设置一个默认值
      dashboardData.stats[2].value = 0;
    }
    
    // 获取客服工单数
    const conversationsResponse = await conversationApi.listAllConversations();
    if (conversationsResponse.code === 200) {
      const conversations = conversationsResponse.data;
      // 统计正在进行中的会话数
      const activeConversations = conversations.filter(conv => 
        conv.status === 2  // 2表示已转人工
      );
      dashboardData.stats[3].value = activeConversations.length;
    }
    
  } catch (error) {
    console.error('加载仪表盘数据失败', error);
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时初始化
onMounted(() => {
  loadUserInfo();
  loadDashboardData();
});

// 侧边栏菜单项
const menuItems = [
  { index: 'dashboard', title: '仪表盘', icon: 'DataBoard' },
  { index: 'flights', title: '航班管理', icon: 'Location' },
  { index: 'orders', title: '订单管理', icon: 'Ticket' },
  { index: 'conversations', title: '会话管理', icon: 'ChatDotRound' },
  { index: 'prompts', title: '提示词管理', icon: 'ChatLineRound' },
  { index: 'knowledge', title: '知识库管理', icon: 'Document' },
  { index: 'accounts', title: '账号管理', icon: 'UserFilled' },
  { index: 'logs', title: '日志信息', icon: 'List' }
];
</script>

<template>
  <div class="admin-container">
    <!-- 使用通用顶部导航栏组件 -->
    <HeaderNav :username="username" />
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 左侧导航 -->
      <div class="sidebar">
        <!-- 使用通用侧边栏组件 -->
        <SideMenu
          :menu-items="menuItems"
          :active-menu="activeMenu"
          @select="activeMenu = $event"
        />
      </div>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 仪表盘 -->
        <div v-if="activeMenu === 'dashboard'">
          <h2 class="page-title">仪表盘</h2>
          <div v-loading="loading" class="stats-container">
            <!-- 使用统计卡片组件 -->
            <StatCard 
              v-for="(stat, index) in dashboardData.stats" 
              :key="index"
              :title="stat.title"
              :value="stat.value"
              :icon="stat.icon"
              :color="stat.color"
            />
          </div>
          
          <div class="row">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>航班概览</span>
                </div>
              </template>
              <div class="chart-placeholder">
                图表区域（航班统计）
              </div>
            </el-card>
            
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>订单趋势</span>
                </div>
              </template>
              <div class="chart-placeholder">
                图表区域（订单趋势）
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 航班管理 -->
        <div v-if="activeMenu === 'flights'">
          <h2 class="page-title">航班管理</h2>
          <FlightsManagement />
        </div>
        
        <!-- 订单管理 -->
        <div v-if="activeMenu === 'orders'">
          <h2 class="page-title">订单管理</h2>
          <OrdersManagement />
        </div>
        
        <!-- 会话管理 -->
        <div v-if="activeMenu === 'conversations'">
          <h2 class="page-title">会话管理</h2>
          <ConversationManagement />
        </div>
        
        <!-- 提示词管理 -->
        <div v-if="activeMenu === 'prompts'">
          <h2 class="page-title">提示词管理</h2>
          <PromptsManagement />
        </div>
        
        <!-- 知识库管理 -->
        <div v-if="activeMenu === 'knowledge'">
          <h2 class="page-title">知识库管理</h2>
          <KnowledgeManagement />
        </div>
        
        <!-- 账号管理 -->
        <div v-if="activeMenu === 'accounts'">
          <h2 class="page-title">账号管理</h2>
          <AccountsManagement />
        </div>
        
        <!-- 日志信息 -->
        <div v-if="activeMenu === 'logs'">
          <h2 class="page-title">日志信息</h2>
          <LogsManagement />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-container {
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
  height: calc(100% - 60px);
  overflow: hidden;
}

.sidebar {
  width: 220px;
  height: 100%;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  width: 100%;
  box-sizing: border-box;
  height: 100%;
}

.page-title {
  margin-bottom: 20px;
  font-size: 22px;
  color: #303133;
}

.stats-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 20px;
  width: 100%;
}

.stats-container > div {
  flex: 1;
}

.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 20px;
  width: 100%;
}

.chart-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}
</style> 