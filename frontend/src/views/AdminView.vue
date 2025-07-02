<script setup>
import { ref, reactive } from 'vue';
import HeaderNav from '@/components/common/HeaderNav.vue';
import SideMenu from '@/components/common/SideMenu.vue';
import StatCard from '@/components/admin/StatCard.vue';
import ConversationManagement from '@/components/admin/ConversationManagement.vue';

const username = ref('管理员');
const activeMenu = ref('dashboard');

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

// 仪表盘数据
const dashboardData = {
  stats: [
    { title: '今日航班', value: 246, icon: 'Location', color: '#409eff' },
    { title: '今日订单', value: 158, icon: 'Ticket', color: '#67c23a' },
    { title: '活跃用户', value: 1243, icon: 'User', color: '#e6a23c' },
    { title: '客服工单', value: 78, icon: 'Service', color: '#f56c6c' }
  ]
};

// 模拟航班数据
const flights = [
  { id: 'CA1234', from: '北京', to: '上海', departure: '08:00', arrival: '10:00', status: '准时' },
  { id: 'MU5678', from: '广州', to: '深圳', departure: '09:30', arrival: '10:30', status: '延误' },
  { id: 'CZ8901', from: '成都', to: '重庆', departure: '12:00', arrival: '13:00', status: '取消' }
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
          <div class="stats-container">
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
          <div class="action-bar">
            <el-button type="primary">添加航班</el-button>
            <el-input
              placeholder="搜索航班"
              prefix-icon="Search"
              style="width: 300px; margin-left: auto;"
            />
          </div>
          <el-table :data="flights" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="航班号" width="100" />
            <el-table-column prop="from" label="出发地" width="120" />
            <el-table-column prop="to" label="目的地" width="120" />
            <el-table-column prop="departure" label="出发时间" width="120" />
            <el-table-column prop="arrival" label="到达时间" width="120" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === '准时' ? 'success' : 
                         scope.row.status === '延误' ? 'warning' : 'danger'"
                >
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default>
                <el-button size="small" type="primary">编辑</el-button>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 订单管理 -->
        <div v-if="activeMenu === 'orders'">
          <h2 class="page-title">订单管理</h2>
          <el-empty description="订单管理模块开发中" />
        </div>
        
        <!-- 会话管理 -->
        <div v-if="activeMenu === 'conversations'">
          <h2 class="page-title">会话管理</h2>
          <ConversationManagement />
        </div>
        
        <!-- 其他模块占位 -->
        <div v-if="['prompts', 'knowledge', 'accounts', 'logs'].includes(activeMenu)">
          <h2 class="page-title">{{ 
            activeMenu === 'prompts' ? '提示词管理' :
            activeMenu === 'knowledge' ? '知识库管理' :
            activeMenu === 'accounts' ? '账号管理' : '日志信息'
          }}</h2>
          <el-empty :description="`${
            activeMenu === 'prompts' ? '提示词管理' :
            activeMenu === 'knowledge' ? '知识库管理' :
            activeMenu === 'accounts' ? '账号管理' : '日志信息'
          }模块开发中`" />
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

.action-bar {
  display: flex;
  margin-bottom: 20px;
}
</style> 