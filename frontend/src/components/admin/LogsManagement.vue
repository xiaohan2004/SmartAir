<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const logs = ref([]);
const searchText = ref('');
const filteredLogs = ref([]);

// 日志级别选项
const logLevels = [
  { value: 'all', label: '全部' },
  { value: 'info', label: '信息' },
  { value: 'warn', label: '警告' },
  { value: 'error', label: '错误' }
];

// 当前选中的日志级别
const currentLevel = ref('all');

// 日期范围
const dateRange = ref([]);

// 模拟日志数据
const mockLogs = [
  { id: 1, level: 'info', message: '用户登录成功', module: '用户认证', operator: 'user1', operateTime: '2023-11-01 08:30:15', ip: '192.168.1.100' },
  { id: 2, level: 'info', message: '航班信息更新', module: '航班管理', operator: 'admin1', operateTime: '2023-11-01 09:15:20', ip: '192.168.1.101' },
  { id: 3, level: 'warn', message: '用户登录失败，密码错误', module: '用户认证', operator: 'unknown', operateTime: '2023-11-01 10:05:42', ip: '192.168.1.102' },
  { id: 4, level: 'error', message: '数据库连接失败', module: '系统', operator: 'system', operateTime: '2023-11-01 11:20:33', ip: '127.0.0.1' },
  { id: 5, level: 'info', message: '订单创建成功', module: '订单管理', operator: 'user2', operateTime: '2023-11-01 13:45:10', ip: '192.168.1.105' },
  { id: 6, level: 'warn', message: '航班延误', module: '航班管理', operator: 'system', operateTime: '2023-11-01 15:30:55', ip: '127.0.0.1' },
  { id: 7, level: 'error', message: '支付系统异常', module: '支付', operator: 'system', operateTime: '2023-11-01 16:20:08', ip: '127.0.0.1' },
  { id: 8, level: 'info', message: '用户信息更新', module: '用户管理', operator: 'user1', operateTime: '2023-11-01 17:10:25', ip: '192.168.1.100' },
  { id: 9, level: 'info', message: '系统定时任务执行', module: '系统', operator: 'system', operateTime: '2023-11-01 23:00:00', ip: '127.0.0.1' },
  { id: 10, level: 'warn', message: '系统负载过高', module: '系统', operator: 'system', operateTime: '2023-11-01 23:30:45', ip: '127.0.0.1' }
];

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的API获取日志列表
    // 模拟API调用
    setTimeout(() => {
      logs.value = mockLogs;
      filterLogs();
      loading.value = false;
    }, 500);
  } catch (error) {
    ElMessage.error('获取日志列表失败');
    loading.value = false;
  }
};

// 筛选日志
const filterLogs = () => {
  let filtered = logs.value;
  
  // 按日志级别筛选
  if (currentLevel.value !== 'all') {
    filtered = filtered.filter(log => log.level === currentLevel.value);
  }
  
  // 按日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    filtered = filtered.filter(log => {
      const logDate = new Date(log.operateTime);
      return logDate >= startDate && logDate <= endDate;
    });
  }
  
  // 按搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filtered = filtered.filter(log => 
      log.message.toLowerCase().includes(searchValue) ||
      log.module.toLowerCase().includes(searchValue) ||
      log.operator.toLowerCase().includes(searchValue) ||
      log.ip.includes(searchValue)
    );
  }
  
  filteredLogs.value = filtered;
};

// 监听搜索输入
const handleSearchInput = () => {
  filterLogs();
};

// 切换日志级别
const handleLevelChange = () => {
  filterLogs();
};

// 日期范围变化
const handleDateRangeChange = () => {
  filterLogs();
};

// 获取日志级别对应的标签类型
const getLevelType = (level) => {
  switch (level) {
    case 'info': return 'info';
    case 'warn': return 'warning';
    case 'error': return 'danger';
    default: return '';
  }
};

// 导出日志
const exportLogs = () => {
  ElMessage.info('导出日志功能开发中...');
  // 这里应该实现导出日志的功能
};

// 清空日志
const clearLogs = () => {
  ElMessage.info('清空日志功能开发中...');
  // 这里应该实现清空日志的功能
};

// 组件挂载时获取数据
onMounted(() => {
  fetchLogs();
});
</script>

<template>
  <div class="logs-management">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="left-actions">
        <el-button type="primary" plain @click="exportLogs">导出日志</el-button>
        <el-button type="danger" plain @click="clearLogs">清空日志</el-button>
      </div>
      
      <div class="filters">
        <el-radio-group v-model="currentLevel" @change="handleLevelChange">
          <el-radio-button 
            v-for="level in logLevels" 
            :key="level.value" 
            :label="level.value"
          >
            {{ level.label }}
          </el-radio-button>
        </el-radio-group>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateRangeChange"
          style="margin: 0 10px; width: 300px;"
        />
        
        <el-input
          v-model="searchText"
          placeholder="搜索日志内容/模块/操作员/IP"
          prefix-icon="Search"
          @input="handleSearchInput"
          style="width: 250px;"
        />
      </div>
    </div>
    
    <!-- 日志列表 -->
    <el-table
      :data="filteredLogs"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="level" label="级别" width="100">
        <template #default="scope">
          <el-tag :type="getLevelType(scope.row.level)">
            {{ 
              scope.row.level === 'info' ? '信息' : 
              scope.row.level === 'warn' ? '警告' : 
              scope.row.level === 'error' ? '错误' : 
              scope.row.level 
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="日志内容" min-width="300" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="operator" label="操作员" width="120" />
      <el-table-column prop="operateTime" label="操作时间" width="180" />
      <el-table-column prop="ip" label="IP地址" width="140" />
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="100"
        :page-size="10"
        class="pagination"
      />
    </div>
  </div>
</template>

<style scoped>
.logs-management {
  width: 100%;
}

.action-bar {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.left-actions {
  display: flex;
  margin-bottom: 10px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式处理 */
@media (min-width: 992px) {
  .action-bar {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .left-actions {
    margin-bottom: 0;
  }
  
  .filters {
    flex-wrap: nowrap;
  }
}
</style> 