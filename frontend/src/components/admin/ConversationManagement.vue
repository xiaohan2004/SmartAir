<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({
  // 可以传入其他需要的属性
});

// 会话数据
const conversations = ref([]);
const loading = ref(true);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索条件
const searchForm = ref({
  userId: '',
  status: 2, // 默认显示已转人工的会话
  dateRange: []
});

// 会话状态选项
const statusOptions = [
  { value: 1, label: '活跃' },
  { value: 2, label: '已转人工' },
  { value: 3, label: '已关闭' }
];

// 获取会话列表
const fetchConversations = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的API获取会话数据
    // API示例：/api/conversation/admin/list
    // 根据筛选条件获取不同的会话
    // 如果选择了状态，则按状态筛选
    // 注意：客服会话默认是已转人工的会话
    
    // 模拟API调用延迟
    setTimeout(() => {
      // 根据状态筛选不同的数据
      let filteredData = [];
      
      if (searchForm.value.status === 1) { // 活跃
        filteredData = [
          { 
            id: 1, 
            conversationUuid: 'conv-001', 
            userId: 101, 
            userName: '张三',
            lastMessage: '请问如何改签机票？', 
            status: 1, 
            serviceUserId: null,
            serviceName: null,
            updatedAt: '2023-12-01 10:30:25' 
          }
        ];
      } else if (searchForm.value.status === 2 || !searchForm.value.status) { // 默认显示已转人工
        filteredData = [
          { 
            id: 3, 
            conversationUuid: 'conv-003', 
            userId: 103, 
            userName: '王五',
            lastMessage: '我的订单有问题', 
            status: 2, 
            serviceUserId: 202,
            serviceName: '客服小李',
            updatedAt: '2023-12-01 11:45:30' 
          },
          { 
            id: 4, 
            conversationUuid: 'conv-004', 
            userId: 104, 
            userName: '赵六',
            lastMessage: '请帮我查询航班信息', 
            status: 2, 
            serviceUserId: 201,
            serviceName: '客服小王',
            updatedAt: '2023-12-01 12:15:45' 
          }
        ];
      } else if (searchForm.value.status === 3) { // 已关闭
        filteredData = [
          { 
            id: 2, 
            conversationUuid: 'conv-002', 
            userId: 102, 
            userName: '李四',
            lastMessage: '感谢您的帮助', 
            status: 3, 
            serviceUserId: 201,
            serviceName: '客服小王',
            updatedAt: '2023-12-01 09:15:18' 
          }
        ];
      }
      
      // 如果有用户ID筛选，则进一步筛选
      if (searchForm.value.userId) {
        filteredData = filteredData.filter(item => 
          item.userId.toString().includes(searchForm.value.userId) || 
          item.userName.includes(searchForm.value.userId)
        );
      }
      
      conversations.value = filteredData;
      total.value = filteredData.length;
      loading.value = false;
    }, 500);
  } catch (error) {
    ElMessage.error('获取会话列表失败');
    loading.value = false;
  }
};

// 搜索会话
const handleSearch = () => {
  currentPage.value = 1;
  fetchConversations();
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    userId: '',
    status: 2, // 重置为默认显示已转人工的会话
    dateRange: []
  };
  handleSearch();
};

// 查看会话详情
const viewConversationDetail = (row) => {
  ElMessage.info(`查看会话详情：${row.conversationUuid}`);
  // 实现查看会话详情的逻辑
};

// 关闭会话
const closeConversation = (row) => {
  ElMessageBox.confirm(
    `确定要关闭该会话吗？`,
    '关闭会话',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API关闭会话
    ElMessage.success('会话已关闭');
    fetchConversations();
  }).catch(() => {
    // 用户取消操作
  });
};

// 转接会话
const transferConversation = (row) => {
  ElMessageBox.prompt('请输入要转接的客服ID', '转接会话', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[0-9]+$/,
    inputErrorMessage: '客服ID必须是数字'
  }).then(({ value }) => {
    // 这里应该调用实际的API转接会话
    ElMessage.success(`会话已转接至客服ID: ${value}`);
    fetchConversations();
  }).catch(() => {
    // 用户取消操作
  });
};

// 格式化会话状态
const formatStatus = (status) => {
  switch (status) {
    case 1: return '活跃';
    case 2: return '已转人工';
    case 3: return '已关闭';
    default: return '未知';
  }
};

// 状态对应的类型
const statusType = (status) => {
  switch (status) {
    case 1: return 'success';
    case 2: return 'warning';
    case 3: return 'info';
    default: return '';
  }
};

// 页面加载时获取数据
onMounted(() => {
  fetchConversations();
});
</script>

<template>
  <div class="conversation-management">
    <!-- 说明信息 -->
    <div class="info-tip">
      <el-alert
        title="管理会话"
        type="info"
        description="默认显示已转人工的会话列表，您可以通过状态筛选查看其他状态的会话"
        show-icon
        :closable="false"
      />
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" label-width="80px" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="会话状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option 
              v-for="item in statusOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 会话列表 -->
    <el-table
      :data="conversations"
      style="width: 100%"
      border
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="conversationUuid" label="会话UUID" width="120" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="lastMessage" label="最后消息" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">
            {{ formatStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理客服" width="120">
        <template #default="scope">
          <span v-if="scope.row.serviceUserId">{{ scope.row.serviceName }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="primary" @click="viewConversationDetail(scope.row)">查看</el-button>
          <el-button 
            size="small" 
            type="warning" 
            @click="transferConversation(scope.row)"
            v-if="scope.row.status === 1"
          >转接</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="closeConversation(scope.row)"
            v-if="scope.row.status !== 3"
          >关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="val => { pageSize = val; fetchConversations(); }"
        @current-change="val => { currentPage = val; fetchConversations(); }"
      />
    </div>
  </div>
</template>

<style scoped>
.conversation-management {
  width: 100%;
}

.info-tip {
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 