<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { logApi } from '@/api';

// 响应式数据
const logs = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredLogs = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const paginatedLogs = ref([]);

// 日期范围
const dateRange = ref([]);

// 清空日志相关
const clearDialogVisible = ref(false);
const clearBeforeDateTime = ref('');

// 获取日志列表
const fetchLogs = async () => {
  loading.value = true;
  try {
    // 调用API获取日志列表
    const response = await logApi.listLogs();
    
    if (response.code === 200) {
      logs.value = response.data;
      console.log('获取到的日志数据:', logs.value);
    } else {
      throw new Error(response.message || '获取日志列表失败');
    }
    
    filterLogs();
  } catch (error) {
    console.error('获取日志列表失败:', error);
    ElMessage.error('获取日志列表失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 筛选日志
const filterLogs = () => {
  let filtered = logs.value;
  
  // 根据日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    startDate.setHours(0, 0, 0, 0);
    
    const endDate = new Date(dateRange.value[1]);
    endDate.setHours(23, 59, 59, 999);
    
    filtered = filtered.filter(log => {
      if (!log.createdAt) return true;
      const logDate = new Date(log.createdAt);
      return logDate >= startDate && logDate <= endDate;
    });
  }
  
  // 根据搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filtered = filtered.filter(log => 
      (log.className && log.className.toLowerCase().includes(searchValue)) ||
      (log.methodName && log.methodName.toLowerCase().includes(searchValue)) ||
      (log.message && log.message.toLowerCase().includes(searchValue)) ||
      (log.operatorId && String(log.operatorId).includes(searchValue))
    );
  }
  
  filteredLogs.value = filtered;
  
  // 更新总数
  total.value = filteredLogs.value.length;
  
  // 更新分页数据
  updatePaginatedData();
};

// 更新分页后的数据
const updatePaginatedData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  paginatedLogs.value = filteredLogs.value.slice(startIndex, endIndex);
};

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  updatePaginatedData();
};

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  updatePaginatedData();
};

// 监听搜索输入
const handleSearchInput = () => {
  currentPage.value = 1; // 搜索时重置为第一页
  filterLogs();
};

// 日期范围变化
const handleDateRangeChange = () => {
  currentPage.value = 1; // 筛选时重置为第一页
  filterLogs();
};

// 打开清空日志对话框
const openClearLogsDialog = () => {
  // 设置默认值为当前时间
  clearBeforeDateTime.value = format(new Date(), 'yyyy-MM-dd HH:mm:ss');
  clearDialogVisible.value = true;
};

// 清空日志
const clearLogs = async () => {
  try {
    loading.value = true;
    
    // 格式化日期时间为ISO格式
    const beforeTime = new Date(clearBeforeDateTime.value);
    const formattedDateTime = format(beforeTime, "yyyy-MM-dd'T'HH:mm:ss");
    
    // 调用清空API，参数放在请求体中
    const response = await logApi.cleanLogs({
      beforeTime: formattedDateTime
    });
    
    if (response.code === 200) {
      ElMessage.success(`成功清理 ${response.data || 0} 条日志`);
      clearDialogVisible.value = false;
      
      // 重新获取日志列表
      await fetchLogs();
    } else {
      throw new Error(response.message || '清空日志失败');
    }
  } catch (error) {
    console.error('清空日志失败:', error);
    ElMessage.error('清空日志失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 格式化日期时间
const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '-';
  try {
    const date = new Date(dateTimeString);
    return format(date, 'yyyy-MM-dd HH:mm:ss', { locale: zhCN });
  } catch (error) {
    console.error('日期格式化错误:', error);
    return dateTimeString;
  }
};

// 格式化执行时间（毫秒）并获取对应的样式
const formatExecutionTime = (time) => {
  if (time === undefined || time === null) return { text: '-', type: '' };
  
  if (time < 20) {
    // 小于20ms，显示绿色
    return { text: `${time}ms`, type: 'success' };
  } else if (time < 50) {
    // 20-50ms，显示蓝色
    return { text: `${time}ms`, type: 'primary' };
  } else if (time < 200) {
    // 50-200ms，显示黄色
    return { text: `${time}ms`, type: 'warning' };
  } else {
    // 大于200ms，显示红色
    return { text: `${time}ms`, type: 'danger' };
  }
};

// 获取日志级别对应的标签类型
const getLogLevelTagType = (message) => {
  if (!message) return '';
  
  if (message.includes('ERROR') || message.includes('错误')) {
    return 'danger';
  } else if (message.includes('WARN') || message.includes('警告')) {
    return 'warning';
  } else {
    return 'info';
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchLogs();
});
</script>

<template>
  <div class="logs-management">
    <!-- 搜索和筛选 -->
    <div class="action-bar">
      <div class="left-actions">
        <el-button type="danger" @click="openClearLogsDialog">
          <el-icon><Delete /></el-icon> 清空日志
        </el-button>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
          style="margin-left: 20px; width: 260px;"
        />
      </div>
      
      <el-input
        v-model="searchText"
        placeholder="搜索日志内容"
        prefix-icon="Search"
        @input="handleSearchInput"
        style="width: 300px; margin-left: auto;"
      />
    </div>
    
    <!-- 日志列表 -->
    <el-table
      :data="paginatedLogs"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" min-width="80" />
      <el-table-column prop="operatorId" label="操作人ID" min-width="100" />
      <el-table-column prop="className" label="类名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="methodName" label="方法名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="executionTime" label="执行时间" min-width="100">
        <template #default="scope">
          <el-tag :type="formatExecutionTime(scope.row.executionTime).type" size="small">
            {{ formatExecutionTime(scope.row.executionTime).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="日志内容" min-width="300" show-overflow-tooltip>
        <template #default="scope">
          <el-tag :type="getLogLevelTagType(scope.row.message)" size="small">
            {{ scope.row.message }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </div>
    
    <!-- 清空日志对话框 -->
    <el-dialog
      v-model="clearDialogVisible"
      title="清空日志"
      width="400px"
    >
      <div>
        <p>请选择要清空哪个日期时间之前的日志：</p>
        <el-date-picker
          v-model="clearBeforeDateTime"
          type="datetime"
          placeholder="选择日期时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%; margin-top: 15px;"
        />
        <p class="warning-text">注意：此操作将清空所选日期时间（含）之前的所有日志，且不可恢复！</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="clearDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="clearLogs">确认清空</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.logs-management {
  width: 100%;
}

.action-bar {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.left-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.warning-text {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 15px;
}

/* 响应式处理 */
@media (max-width: 992px) {
  .action-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .left-actions {
    margin-bottom: 10px;
  }
}
</style> 