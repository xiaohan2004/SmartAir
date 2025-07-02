<script setup>
import { ref, reactive, onMounted, watch, onUnmounted } from 'vue';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import StatCard from '@/components/admin/StatCard.vue';
import ConversationsManagement from '@/components/admin/ConversationsManagement.vue';
import FlightsManagement from '@/components/admin/FlightsManagement.vue';
import OrdersManagement from '@/components/admin/OrdersManagement.vue';
import PromptsManagement from '@/components/admin/PromptsManagement.vue';
import KnowledgeManagement from '@/components/admin/KnowledgeManagement.vue';
import AccountsManagement from '@/components/admin/AccountsManagement.vue';
import LogsManagement from '@/components/admin/LogsManagement.vue';
import { parseJwt } from '@/utils/jwt';
import { dashboardApi } from '@/api/dashboard';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const username = ref('管理员');
const activeMenu = ref('dashboard');
const loading = ref(false);

// 图表实例
const flightChartRef = ref(null);
const orderChartRef = ref(null);
let flightChart = null;
let orderChart = null;

// 仪表盘数据
const dashboardData = reactive({
  stats: [
    { title: '今日航班', value: 0, icon: 'Location', color: '#409eff' },
    { title: '今日订单', value: 0, icon: 'Ticket', color: '#67c23a' },
    { title: '活跃用户', value: 0, icon: 'User', color: '#e6a23c' },
    { title: '客服工单', value: 0, icon: 'Service', color: '#f56c6c' }
  ],
  dates: [],
  dailyFlights: [],
  dailyOrders: []
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
    
    // 调用仪表盘API获取数据
    const response = await dashboardApi.getDashboardOverview();
    
    if (response.code === 200) {
      const data = response.data;
      
      // 更新统计数据
      dashboardData.stats[0].value = data.todayFlights || 0;
      dashboardData.stats[1].value = data.todayOrders || 0;
      dashboardData.stats[2].value = data.activeUsers || 0;
      dashboardData.stats[3].value = data.serviceTickets || 0;
      
      // 更新图表数据
      dashboardData.dates = data.dates || [];
      dashboardData.dailyFlights = data.dailyFlights || [];
      dashboardData.dailyOrders = data.dailyOrders || [];
      
      // 初始化图表
      initCharts();
    } else {
      throw new Error(response.message || '获取仪表盘数据失败');
    }
  } catch (error) {
    console.error('加载仪表盘数据失败', error);
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 初始化图表
const initCharts = () => {
  // 确保DOM已经渲染
  setTimeout(() => {
    // 初始化航班图表
    if (flightChartRef.value) {
      // 如果已经有图表实例，先销毁
      if (flightChart) {
        flightChart.dispose();
      }
      flightChart = echarts.init(flightChartRef.value);
      const flightOption = {
        title: {
          text: '近期航班数量趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: dashboardData.dates,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '航班数量'
        },
        series: [
          {
            name: '航班数量',
            type: 'bar',
            data: dashboardData.dailyFlights,
            itemStyle: {
              color: '#409eff'
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      };
      flightChart.setOption(flightOption);
    }
    
    // 初始化订单图表
    if (orderChartRef.value) {
      // 如果已经有图表实例，先销毁
      if (orderChart) {
        orderChart.dispose();
      }
      orderChart = echarts.init(orderChartRef.value);
      const orderOption = {
        title: {
          text: '近期订单数量趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: dashboardData.dates,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '订单数量'
        },
        series: [
          {
            name: '订单数量',
            type: 'line',
            data: dashboardData.dailyOrders,
            smooth: true,
            itemStyle: {
              color: '#67c23a'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
                { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
              ])
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      };
      orderChart.setOption(orderOption);
    }
  }, 100);
};

// 窗口大小变化时重绘图表
const resizeCharts = () => {
  if (flightChart) {
    flightChart.resize();
  }
  if (orderChart) {
    orderChart.resize();
  }
};

// 组件挂载时初始化
onMounted(() => {
  loadUserInfo();
  loadDashboardData();
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCharts);
});

// 监听菜单切换，当回到仪表盘时重新加载数据
watch(activeMenu, (newValue) => {
  if (newValue === 'dashboard') {
    loadDashboardData();
  }
});

// 组件卸载时清理资源
onUnmounted(() => {
  // 移除窗口大小变化监听
  window.removeEventListener('resize', resizeCharts);
  
  // 销毁图表实例
  if (flightChart) {
    flightChart.dispose();
    flightChart = null;
  }
  if (orderChart) {
    orderChart.dispose();
    orderChart = null;
  }
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
              <div ref="flightChartRef" class="chart-container"></div>
            </el-card>
            
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>订单趋势</span>
                </div>
              </template>
              <div ref="orderChartRef" class="chart-container"></div>
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
          <ConversationsManagement />
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

.chart-container {
  height: 300px;
  width: 100%;
}

/* 响应式处理 */
@media (max-width: 992px) {
  .row {
    flex-direction: column;
  }
  
  .chart-card {
    margin-bottom: 20px;
  }
  
  .stats-container {
    flex-wrap: wrap;
  }
  
  .stats-container > div {
    flex-basis: calc(50% - 10px);
  }
}

@media (max-width: 768px) {
  .stats-container > div {
    flex-basis: 100%;
  }
}
</style> 