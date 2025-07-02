<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { format } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { conversationApi } from '@/api';

// 响应式数据
const conversations = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredConversations = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const paginatedConversations = ref([]);

// 会话状态选项
const statusOptions = [
  { value: 'all', label: '全部' },
  { value: 'active', label: '进行中' },
  { value: 'transferred', label: '已转接' },
  { value: 'closed', label: '已关闭' }
];

// 当前选中的会话状态
const currentStatus = ref('all');

// 会话详情对话框
const dialogVisible = ref(false);
const currentConversation = ref(null);
const conversationMessages = ref([]);
const loadingMessages = ref(false);

// 获取会话列表
const fetchConversations = async () => {
  loading.value = true;
  try {
    // 调用API获取会话列表
    const response = await conversationApi.listAllConversations();
    if (response.code === 200) {
      conversations.value = response.data;
      console.log('获取到的会话数据:', conversations.value);
    } else {
      throw new Error(response.message || '获取会话列表失败');
    }
    filterConversations();
  } catch (error) {
    console.error('获取会话列表失败:', error);
    ElMessage.error('获取会话列表失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 筛选会话
const filterConversations = () => {
  // 先根据状态筛选
  let statusFiltered = currentStatus.value === 'all' 
    ? conversations.value 
    : conversations.value.filter(conversation => conversation.status === currentStatus.value);
  
  // 再根据搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filteredConversations.value = statusFiltered.filter(conversation => 
      (conversation.conversationUuid && conversation.conversationUuid.toLowerCase().includes(searchValue)) ||
      (conversation.lastMessage && conversation.lastMessage.toLowerCase().includes(searchValue)) ||
      (conversation.userId && String(conversation.userId).includes(searchValue)) ||
      (conversation.serviceUserId && String(conversation.serviceUserId).includes(searchValue)) ||
      (conversation.userName && conversation.userName.toLowerCase().includes(searchValue)) ||
      (conversation.serviceUserName && conversation.serviceUserName.toLowerCase().includes(searchValue))
    );
  } else {
    filteredConversations.value = statusFiltered;
  }
  
  // 更新总数
  total.value = filteredConversations.value.length;
  
  // 更新分页数据
  updatePaginatedData();
};

// 更新分页后的数据
const updatePaginatedData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  paginatedConversations.value = filteredConversations.value.slice(startIndex, endIndex);
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
  filterConversations();
};

// 切换会话状态
const handleStatusChange = () => {
  currentPage.value = 1; // 筛选时重置为第一页
  filterConversations();
};

// 查看会话详情
const viewConversationDetail = async (conversation) => {
  currentConversation.value = conversation;
  dialogVisible.value = true;
  loadingMessages.value = true;
  
  try {
    // 获取会话消息记录
    const response = await conversationApi.getConversationMessages(conversation.conversationUuid);
    if (response.code === 200) {
      conversationMessages.value = response.data;
    } else {
      throw new Error(response.message || '获取会话详情失败');
    }
  } catch (error) {
    console.error('获取会话详情失败:', error);
    ElMessage.error('获取会话详情失败: ' + (error.message || '未知错误'));
    conversationMessages.value = [];
  } finally {
    loadingMessages.value = false;
  }
};

// 关闭会话
const closeConversation = (conversationUuid) => {
  ElMessageBox.confirm(
    '确定要关闭该会话吗？',
    '关闭会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      loading.value = true;
      
      // 调用关闭会话API
      const response = await conversationApi.closeConversation(conversationUuid);
      if (response.code === 200) {
        ElMessage.success('会话已关闭');
        // 刷新会话列表
        fetchConversations();
      } else {
        throw new Error(response.message || '关闭会话失败');
      }
    } catch (error) {
      console.error('关闭会话失败:', error);
      ElMessage.error('关闭会话失败: ' + (error.message || '未知错误'));
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 转接会话
const transferConversation = (conversationUuid) => {
  ElMessageBox.prompt(
    '请输入要转接的客服ID',
    '转接会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+$/,
      inputErrorMessage: '客服ID必须为数字'
    }
  ).then(async ({ value }) => {
    try {
      loading.value = true;
      
      // 调用转接会话API
      const response = await conversationApi.transferConversation(conversationUuid, parseInt(value));
      if (response.code === 200) {
        ElMessage.success('会话已转接');
        // 刷新会话列表
        fetchConversations();
      } else {
        throw new Error(response.message || '转接会话失败');
      }
    } catch (error) {
      console.error('转接会话失败:', error);
      ElMessage.error('转接会话失败: ' + (error.message || '未知错误'));
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 获取状态名称
const getStatusName = (status) => {
  switch (status) {
    case 'active': return '进行中';
    case 'transferred': return '已转接';
    case 'closed': return '已关闭';
    default: return '未知';
  }
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'active': return 'success';
    case 'transferred': return 'warning';
    case 'closed': return 'info';
    default: return '';
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

// 获取用户名称（带默认值）
const getUserName = (conversation) => {
  return conversation.userName || `用户ID: ${conversation.userId}`;
};

// 获取客服名称（带默认值）
const getServiceName = (conversation) => {
  if (!conversation.serviceUserId) return '-';
  return conversation.serviceUserName || `客服ID: ${conversation.serviceUserId}`;
};

// 组件挂载时获取数据
onMounted(() => {
  fetchConversations();
});
</script>

<template>
  <div class="conversations-management">
    <!-- 搜索和筛选 -->
    <div class="action-bar">
      <div class="left-actions">
        <el-radio-group 
          v-model="currentStatus" 
          @change="handleStatusChange"
        >
          <el-radio-button 
            v-for="option in statusOptions" 
            :key="option.value" 
            :label="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      
      <el-input
        v-model="searchText"
        placeholder="搜索会话(ID/用户/客服/内容)"
        prefix-icon="Search"
        @input="handleSearchInput"
        style="width: 300px; margin-left: auto;"
      />
    </div>
    
    <!-- 会话列表 -->
    <el-table
      :data="paginatedConversations"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="conversationUuid" label="会话ID" min-width="220" show-overflow-tooltip />
      <el-table-column label="用户" min-width="120">
        <template #default="scope">
          {{ getUserName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="客服" min-width="120">
        <template #default="scope">
          {{ getServiceName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="lastMessage" label="最后消息" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="240">
        <template #default="scope">
          <div class="action-buttons">
            <el-button 
              size="small" 
              type="primary"
              @click="viewConversationDetail(scope.row)"
            >
              查看详情
            </el-button>
            
            <el-button 
              size="small" 
              type="warning"
              :disabled="scope.row.status !== 'active'"
              @click="transferConversation(scope.row.conversationUuid)"
            >
              转接
            </el-button>
            
            <el-button 
              size="small" 
              type="danger"
              :disabled="scope.row.status === 'closed'"
              @click="closeConversation(scope.row.conversationUuid)"
            >
              关闭
            </el-button>
          </div>
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
    
    <!-- 会话详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="会话详情" 
      width="700px"
      destroy-on-close
    >
      <div v-loading="loadingMessages" class="conversation-detail">
        <div v-if="currentConversation" class="conversation-info">
          <p><strong>会话ID:</strong> {{ currentConversation.conversationUuid }}</p>
          <p><strong>用户:</strong> {{ getUserName(currentConversation) }}</p>
          <p><strong>客服:</strong> {{ getServiceName(currentConversation) }}</p>
          <p><strong>状态:</strong> {{ getStatusName(currentConversation.status) }}</p>
          <p><strong>更新时间:</strong> {{ formatDateTime(currentConversation.updatedAt) }}</p>
        </div>
        
        <el-divider content-position="center">会话记录</el-divider>
        
        <div class="conversation-messages">
          <div 
            v-for="(message, index) in conversationMessages" 
            :key="index"
            class="message-item"
            :class="{ 
              'user-message': message.userId === currentConversation.userId,
              'service-message': message.userId !== currentConversation.userId
            }"
          >
            <div class="message-header">
              <span class="message-sender">
                {{ message.userId === currentConversation.userId ? getUserName(currentConversation) : getServiceName(currentConversation) }}
              </span>
              <span class="message-time">{{ formatDateTime(message.createdAt) }}</span>
            </div>
            <div class="message-content">{{ message.message }}</div>
          </div>
          
          <div v-if="conversationMessages.length === 0" class="no-messages">
            暂无会话记录
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.conversations-management {
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

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-buttons .el-button {
  margin-left: 0;
}

.conversation-detail {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.conversation-info {
  margin-bottom: 20px;
}

.conversation-messages {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  padding: 10px;
  border-radius: 8px;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  background-color: #e1f3ff;
}

.service-message {
  align-self: flex-start;
  background-color: #f0f0f0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.message-content {
  word-break: break-word;
}

.no-messages {
  text-align: center;
  color: #999;
  padding: 20px;
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