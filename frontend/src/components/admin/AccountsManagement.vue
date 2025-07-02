<script setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userApi } from '@/api';

const accounts = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredAccounts = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const paginatedAccounts = ref([]);

// 用户类型选项
const userTypeOptions = [
  { value: 'all', label: '全部' },
  { value: 1, label: '普通用户' },
  { value: 2, label: '客服人员' },
  { value: 3, label: '系统管理员' }
];

// 当前选中的用户类型
const currentUserType = ref('all');

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  phone: '',
  realName: '',
  idCard: '',
  userType: 1,
  memberLevel: 0
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加用户');

// 生成随机密码的函数
const generateRandomPassword = (length = 12) => {
  const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+';
  let password = '';
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * charset.length);
    password += charset[randomIndex];
  }
  return password;
};

// 获取用户列表
const fetchAccounts = async () => {
  loading.value = true;
  try {
    // 从API获取用户列表
    const response = await userApi.listAllUsers();
    if (response.code === 200) {
      accounts.value = response.data;
      console.log('获取到的用户数据:', accounts.value);
    } else {
      throw new Error(response.message || '获取用户列表失败');
    }
    filterAccounts();
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 筛选用户
const filterAccounts = () => {
  // 先根据用户类型筛选
  let typeFiltered = currentUserType.value === 'all' 
    ? accounts.value 
    : accounts.value.filter(account => account.userType === currentUserType.value);
  
  // 再根据搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filteredAccounts.value = typeFiltered.filter(account => 
      (account.username && account.username.toLowerCase().includes(searchValue)) ||
      (account.email && account.email.toLowerCase().includes(searchValue)) ||
      (account.phone && account.phone.includes(searchValue)) ||
      (account.realName && account.realName.toLowerCase().includes(searchValue)) ||
      (account.id && String(account.id).includes(searchValue))
    );
  } else {
    filteredAccounts.value = typeFiltered;
  }
  
  // 更新总数
  total.value = filteredAccounts.value.length;
  
  // 更新分页数据
  updatePaginatedData();
};

// 更新分页后的数据
const updatePaginatedData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  paginatedAccounts.value = filteredAccounts.value.slice(startIndex, endIndex);
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
  filterAccounts();
};

// 切换用户类型
const handleUserTypeChange = () => {
  currentPage.value = 1; // 筛选时重置为第一页
  filterAccounts();
};

// 打开添加对话框
const openAddDialog = () => {
  // 重置表单
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    email: '',
    phone: '',
    realName: '',
    idCard: '',
    userType: 1,
    memberLevel: 0
  });
  
  dialogTitle.value = '添加用户';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = async (userId) => {
  try {
    loading.value = true;
    
    // 获取用户详情
    const response = await userApi.getUserInfo(userId);
    if (response.code === 200) {
      // 填充表单数据
      Object.assign(userForm, response.data);
      // 密码不回显
      userForm.password = '';
      
      dialogTitle.value = '编辑用户';
      dialogVisible.value = true;
    } else {
      throw new Error(response.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
  } finally {
    loading.value = false;
  }
};

// 保存用户
const saveUser = async () => {
  // 表单验证
  if (!userForm.username.trim()) {
    ElMessage.warning('请输入用户名');
    return;
  }
  
  if (!userForm.id && !userForm.password.trim()) {
    ElMessage.warning('请输入密码');
    return;
  }
  
  try {
    loading.value = true;
    
    if (userForm.id) {
      // 更新用户
      const response = await userApi.updateUserInfo(userForm.id, userForm);
      if (response.code === 200) {
        ElMessage.success('用户信息已更新');
      } else {
        throw new Error(response.message || '更新用户失败');
      }
    } else {
      // 添加用户
      const response = await userApi.createUser(userForm);
      if (response.code === 200) {
        ElMessage.success('用户已添加');
      } else {
        throw new Error(response.message || '添加用户失败');
      }
    }
    
    // 关闭对话框并刷新列表
    dialogVisible.value = false;
    fetchAccounts();
  } catch (error) {
    console.error('保存用户失败:', error);
    ElMessage.error(error.message || '操作失败');
  } finally {
    loading.value = false;
  }
};

// 修改用户角色
const changeUserRole = async (userId, newRole) => {
  ElMessageBox.confirm(
    `确定要将该用户角色修改为${getUserTypeName(newRole)}吗？`,
    '修改用户角色',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      loading.value = true;
      
      // 先获取用户信息
      const response = await userApi.getUserInfo(userId);
      if (response.code === 200) {
        const userData = response.data;
        userData.userType = newRole;
        
        // 更新用户角色
        const updateResponse = await userApi.updateUserInfo(userId, userData);
        if (updateResponse.code === 200) {
          ElMessage.success('用户角色已修改');
          fetchAccounts(); // 重新获取用户列表
        } else {
          throw new Error(updateResponse.message || '修改用户角色失败');
        }
      } else {
        throw new Error(response.message || '获取用户信息失败');
      }
    } catch (error) {
      console.error('修改用户角色失败:', error);
      ElMessage.error('修改用户角色失败');
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 重置密码
const resetUserPassword = (userId) => {
  ElMessageBox.confirm(
    '确定要重置该用户的密码吗？系统将生成一个新的随机密码。',
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      loading.value = true;
      
      // 先获取用户信息
      const response = await userApi.getUserInfo(userId);
      if (response.code === 200) {
        const userData = response.data;
        
        // 生成新的随机密码
        const newPassword = generateRandomPassword();
        userData.password = newPassword;
        
        // 更新用户密码
        const updateResponse = await userApi.updateUserInfo(userId, userData);
        if (updateResponse.code === 200) {
          // 显示新密码
          ElMessageBox.alert(
            `新密码: <strong style="font-size: 16px; color: #409EFF;">${newPassword}</strong><br><br>请记下此密码，关闭后将无法再次查看！`,
            '密码重置成功',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '我已记下密码',
              callback: () => {
                ElMessage.success('密码已重置');
              }
            }
          );
        } else {
          throw new Error(updateResponse.message || '重置密码失败');
        }
      } else {
        throw new Error(response.message || '获取用户信息失败');
      }
    } catch (error) {
      console.error('重置密码失败:', error);
      ElMessage.error('重置密码失败');
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 获取用户类型名称
const getUserTypeName = (userType) => {
  switch (userType) {
    case 1: return '普通用户';
    case 2: return '客服人员';
    case 3: return '系统管理员';
    default: return '未知';
  }
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

// 获取用户类型标签类型
const getUserTypeTagType = (userType) => {
  switch (userType) {
    case 1: return 'success'; // 普通用户
    case 2: return 'warning'; // 客服人员
    case 3: return 'danger';  // 系统管理员
    default: return '';
  }
};

// 组件挂载时获取数据
onMounted(() => {
  fetchAccounts();
});
</script>

<template>
  <div class="accounts-management">
    <!-- 搜索和筛选 -->
    <div class="action-bar">
      <div class="left-actions">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 添加用户
        </el-button>
        
        <el-radio-group v-model="currentUserType" @change="handleUserTypeChange" style="margin-left: 20px;">
          <el-radio-button 
            v-for="option in userTypeOptions" 
            :key="option.value" 
            :label="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      
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
      :data="paginatedAccounts"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" min-width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="userType" label="角色" min-width="100">
        <template #default="scope">
          <el-tag :type="getUserTypeTagType(scope.row.userType)">
            {{ getUserTypeName(scope.row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="memberLevel" label="会员等级" min-width="100">
        <template #default="scope">
          <span v-if="scope.row.userType === 1">{{ getMemberLevelName(scope.row.memberLevel) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="240">
        <template #default="scope">
          <div class="action-buttons">
            <el-button 
              size="small" 
              type="primary"
              @click="openEditDialog(scope.row.id)"
            >
              编辑
            </el-button>
            
            <el-dropdown trigger="click" @command="(command) => changeUserRole(scope.row.id, command)">
              <el-button size="small" type="success">
                角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="option in userTypeOptions.filter(o => o.value !== 'all')"
                    :key="option.value"
                    :command="option.value"
                    :disabled="scope.row.userType === option.value"
                  >
                    {{ option.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-button 
              size="small" 
              type="warning"
              @click="resetUserPassword(scope.row.id)"
            >
              重置密码
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
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
    >
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="密码" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="userForm.idCard" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="userForm.userType" placeholder="请选择用户类型">
            <el-option
              v-for="option in userTypeOptions.filter(o => o.value !== 'all')"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级" v-if="userForm.userType === 1">
          <el-select v-model="userForm.memberLevel" placeholder="请选择会员等级">
            <el-option :label="'无'" :value="0" />
            <el-option :label="'普通会员'" :value="1" />
            <el-option :label="'白银会员'" :value="2" />
            <el-option :label="'黄金会员'" :value="3" />
            <el-option :label="'白金会员'" :value="4" />
            <el-option :label="'钻石会员'" :value="5" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.accounts-management {
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