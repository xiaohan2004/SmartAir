<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userApi } from '@/api';

const accounts = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredAccounts = ref([]);

// 角色选项
const roleOptions = [
  { value: 'user', label: '用户' },
  { value: 'service', label: '客服' },
  { value: 'admin', label: '管理员' }
];

// 模拟用户数据
const mockUsers = [
  { id: 1, username: 'user1', email: 'user1@example.com', phone: '13800000001', role: 'user', memberLevel: 1, status: 'active', createdAt: '2023-11-01' },
  { id: 2, username: 'user2', email: 'user2@example.com', phone: '13800000002', role: 'user', memberLevel: 2, status: 'active', createdAt: '2023-11-05' },
  { id: 3, username: 'service1', email: 'service1@example.com', phone: '13900000001', role: 'service', memberLevel: 0, status: 'active', createdAt: '2023-10-15' },
  { id: 4, username: 'admin1', email: 'admin1@example.com', phone: '13700000001', role: 'admin', memberLevel: 0, status: 'active', createdAt: '2023-10-01' },
  { id: 5, username: 'user3', email: 'user3@example.com', phone: '13800000003', role: 'user', memberLevel: 3, status: 'inactive', createdAt: '2023-11-10' },
];

// 获取用户列表
const fetchAccounts = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的API获取用户列表
    // 模拟API调用
    setTimeout(() => {
      accounts.value = mockUsers;
      filterAccounts();
      loading.value = false;
    }, 500);
  } catch (error) {
    ElMessage.error('获取用户列表失败');
    loading.value = false;
  }
};

// 筛选用户
const filterAccounts = () => {
  if (!searchText.value) {
    filteredAccounts.value = accounts.value;
    return;
  }
  
  const searchValue = searchText.value.toLowerCase();
  filteredAccounts.value = accounts.value.filter(account => 
    account.username.toLowerCase().includes(searchValue) ||
    account.email.toLowerCase().includes(searchValue) ||
    account.phone.includes(searchValue) ||
    String(account.id).includes(searchValue)
  );
};

// 修改用户角色
const changeUserRole = (userId, newRole) => {
  ElMessageBox.confirm(
    `确定要将该用户角色修改为${getRoleName(newRole)}吗？`,
    '修改用户角色',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API修改用户角色
    // 模拟API调用
    const user = accounts.value.find(u => u.id === userId);
    if (user) {
      user.role = newRole;
      ElMessage.success('用户角色已修改');
      filterAccounts();
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 禁用/启用用户账号
const toggleUserStatus = (userId, currentStatus) => {
  const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
  const action = newStatus === 'active' ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确定要${action}该用户账号吗？`,
    `${action}用户账号`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API修改用户状态
    // 模拟API调用
    const user = accounts.value.find(u => u.id === userId);
    if (user) {
      user.status = newStatus;
      ElMessage.success(`用户账号已${action}`);
      filterAccounts();
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 重置密码
const resetUserPassword = (userId) => {
  ElMessageBox.confirm(
    '确定要重置该用户的密码吗？重置后密码将设为默认密码。',
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API重置用户密码
    ElMessage.success('密码已重置为默认密码');
  }).catch(() => {
    // 用户取消操作
  });
};

// 获取角色名称
const getRoleName = (role) => {
  const option = roleOptions.find(o => o.value === role);
  return option ? option.label : '未知';
};

// 获取会员等级名称
const getMemberLevelName = (level) => {
  switch (level) {
    case 0: return '无';
    case 1: return '普通会员';
    case 2: return '白银会员';
    case 3: return '黄金会员';
    case 4: return '白金会员';
    case 5: return '钻石会员';
    default: return '未知';
  }
};

// 监听搜索输入
const handleSearchInput = () => {
  filterAccounts();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchAccounts();
});
</script>

<template>
  <div class="accounts-management">
    <!-- 搜索栏 -->
    <div class="action-bar">
      <el-button type="primary">添加用户</el-button>
      <el-input
        v-model="searchText"
        placeholder="搜索用户(ID/用户名/邮箱/手机号)"
        prefix-icon="Search"
        @input="handleSearchInput"
        style="width: 300px; margin-left: auto;"
      />
    </div>
    
    <!-- 用户列表 -->
    <el-table
      :data="filteredAccounts"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag
            :type="
              scope.row.role === 'admin' ? 'danger' :
              scope.row.role === 'service' ? 'warning' : 'success'
            "
          >
            {{ getRoleName(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="memberLevel" label="会员等级" width="100">
        <template #default="scope">
          <span v-if="scope.row.role === 'user'">{{ getMemberLevelName(scope.row.memberLevel) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag
            :type="scope.row.status === 'active' ? 'success' : 'info'"
          >
            {{ scope.row.status === 'active' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="120" />
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="scope">
          <el-dropdown trigger="click" @command="(command) => changeUserRole(scope.row.id, command)">
            <el-button size="small" type="primary" plain>
              修改角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in roleOptions"
                  :key="option.value"
                  :command="option.value"
                  :disabled="scope.row.role === option.value"
                >
                  {{ option.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button 
            size="small" 
            :type="scope.row.status === 'active' ? 'danger' : 'success'" 
            plain
            @click="toggleUserStatus(scope.row.id, scope.row.status)"
            style="margin-left: 5px;"
          >
            {{ scope.row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            size="small" 
            type="warning" 
            plain
            @click="resetUserPassword(scope.row.id)"
            style="margin-left: 5px;"
          >
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.accounts-management {
  width: 100%;
}

.action-bar {
  display: flex;
  margin-bottom: 20px;
}
</style> 