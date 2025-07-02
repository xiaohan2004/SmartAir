<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';
import { userApi, orderApi, conversationApi } from '@/api';
import { getCurrentUser, isLoggedIn, logout } from '@/utils/auth';
import { getMemberLevelName } from '@/utils/jwt';

const router = useRouter();
const activeTab = ref('info'); // 默认显示个人信息标签页
const loading = ref(false);
const passwordLoading = ref(false);

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  role: '',
  email: '',
  phone: '',
  joinTime: '',
  realName: '',
  idCard: '',
  memberLevel: 1,
  memberLevelName: '普通会员'
});

// 是否为普通用户
const isNormalUser = computed(() => userInfo.role === 'user');

// 会员等级对应的类型
const memberLevelType = computed(() => {
  switch (userInfo.memberLevel) {
    case 1:
      return ''; // 普通会员
    case 2:
      return 'info'; // 白银会员
    case 3:
      return 'warning'; // 黄金会员
    case 4:
      return 'success'; // 白金会员
    case 5:
      return 'danger'; // 钻石会员
    default:
      return '';
  }
});

// 会员等级对应的图标
const memberLevelIcon = computed(() => {
  switch (userInfo.memberLevel) {
    case 1:
      return 'User';
    case 2:
      return 'Medal';
    case 3:
      return 'Trophy';
    case 4:
      return 'First';
    case 5:
      return 'Star';
    default:
      return 'User';
  }
});

// 编辑状态
const isEditing = ref(false);

// 暂存编辑中的用户信息
const editingUserInfo = reactive({
  email: '',
  phone: '',
  realName: '',
  idCard: ''
});

// 修改密码对话框
const passwordDialogVisible = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const passwordFormRef = ref(null);

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 订单数据
const orders = ref([]);

// 会话数据
const conversations = ref([]);

// 获取用户信息
const getUserData = async () => {
  try {
    loading.value = true;
    
    // 检查是否登录
    if (!isLoggedIn()) {
      ElMessage.error('用户未登录，请先登录');
      router.push('/login');
      return;
    }
    
    // 从JWT中获取当前用户基本信息
    const currentUser = getCurrentUser();
    if (!currentUser) {
      ElMessage.error('无法获取用户信息，请重新登录');
      router.push('/login');
      return;
    }
    
    // 更新用户基本信息
    Object.assign(userInfo, {
      id: currentUser.id,
      username: currentUser.username,
      role: currentUser.role
    });
    
    // 调用API获取用户详情
    const response = await userApi.getUserInfo(currentUser.id);
    
    if (response && response.code === 200 && response.data) {
      const userData = response.data;
      
      // 更新用户详细信息
      Object.assign(userInfo, {
        email: userData.email || '',
        phone: userData.phone || '',
        realName: userData.realName || '',
        idCard: userData.idCard || '',
        joinTime: userData.createTime || '',
        memberLevel: userData.memberLevel || 1,
        memberLevelName: getMemberLevelName(userData.memberLevel || 1)
      });
      
      // 初始化编辑表单
      Object.assign(editingUserInfo, {
        email: userInfo.email,
        phone: userInfo.phone,
        realName: userInfo.realName,
        idCard: userInfo.idCard
      });
    } else {
      throw new Error(response?.message || '获取用户信息失败');
    }
    
    // 如果是普通用户，获取订单和会话数据
    if (isNormalUser.value) {
      // 获取订单数据
      const orderResponse = await orderApi.listOrdersWithDetail(currentUser.id);
      if (orderResponse && orderResponse.code === 200) {
        orders.value = orderResponse.data || [];
      } else {
        console.error('获取订单数据失败:', orderResponse);
      }
      
      // 获取会话数据
      const conversationResponse = await conversationApi.listConversationsByUserId(currentUser.id);
      if (conversationResponse && conversationResponse.code === 200) {
        conversations.value = conversationResponse.data || [];
      } else {
        console.error('获取会话数据失败:', conversationResponse);
      }
    }
  } catch (error) {
    console.error('获取用户数据失败:', error);
    ElMessage.error(error.message || '获取用户数据失败');
  } finally {
    loading.value = false;
  }
};

// 开始编辑
const startEditing = () => {
  isEditing.value = true;
};

// 保存编辑
const saveEditing = async () => {
  try {
    loading.value = true;
    
    const currentUser = getCurrentUser();
    if (!currentUser) {
      ElMessage.error('用户未登录，请重新登录');
      router.push('/login');
      return;
    }
    
    // 调用API更新用户信息
    const response = await userApi.updateUser(currentUser.id, {
      email: editingUserInfo.email,
      phone: editingUserInfo.phone,
      realName: editingUserInfo.realName,
      idCard: editingUserInfo.idCard
    });
    
    if (response && response.code === 200) {
      // 更新本地用户信息
      userInfo.email = editingUserInfo.email;
      userInfo.phone = editingUserInfo.phone;
      userInfo.realName = editingUserInfo.realName;
      userInfo.idCard = editingUserInfo.idCard;
      
      ElMessage.success(response.message || '资料更新成功');
      isEditing.value = false;
    } else {
      throw new Error(response?.message || '更新用户信息失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error(error.message || '更新用户信息失败');
  } finally {
    loading.value = false;
  }
};

// 取消编辑
const cancelEditing = () => {
  // 重置编辑表单
  editingUserInfo.email = userInfo.email;
  editingUserInfo.phone = userInfo.phone;
  editingUserInfo.realName = userInfo.realName;
  editingUserInfo.idCard = userInfo.idCard;
  isEditing.value = false;
};

// 打开修改密码对话框
const openPasswordDialog = () => {
  // 重置密码表单
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordDialogVisible.value = true;
};

// 提交修改密码
const submitPasswordChange = async () => {
  try {
    await passwordFormRef.value.validate();
    
    passwordLoading.value = true;
    
    const currentUser = getCurrentUser();
    if (!currentUser) {
      ElMessage.error('用户未登录，请重新登录');
      return;
    }
    
    // 调用修改密码API
    const response = await userApi.modifyPassword({
      username: currentUser.username,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    // 检查响应状态
    if (response) {
      if (response.code === 200) {
        // 真正成功的情况
        ElMessage.success(response.message || '密码修改成功，请重新登录');
        
        // 关闭对话框
        passwordDialogVisible.value = false;
        
        // 清空token并退出登录
        logout();
        
        // 显示提示信息，延迟跳转到登录页
        setTimeout(() => {
          router.push('/');
        }, 1500);
      } else if (response.code === 401) {
        // 旧密码错误
        ElMessage.error(response.msg || '旧密码错误');
      } else {
        // 其他错误情况
        throw new Error(response.msg || '修改密码失败');
      }
    } else {
      throw new Error('服务器响应异常');
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    ElMessage.error(error.message || '修改密码失败');
  } finally {
    passwordLoading.value = false;
  }
};

// 查看订单详情
const viewOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`);
};

// 查看会话详情
const viewConversationDetail = (conversationId) => {
  router.push(`/conversation/${conversationId}`);
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 组件挂载时获取用户数据
onMounted(() => {
  getUserData();
});
</script>

<template>
  <div class="profile-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <div class="page-title">个人中心</div>
      <div class="placeholder"></div>
    </header>
    
    <!-- 个人资料卡片 -->
    <div class="profile-content">
      <el-card class="profile-card" v-loading="loading">
        <div class="profile-header">
          <el-avatar :size="80" icon="User" />
          <div class="profile-title">
            <h2>{{ userInfo.username }}</h2>
            <div class="user-tags">
              <el-tag>{{ userInfo.role }}</el-tag>
              <el-tag 
                :type="memberLevelType" 
                class="member-level-tag"
                :effect="userInfo.memberLevel > 2 ? 'dark' : 'light'"
              >
                <el-icon class="member-icon"><component :is="memberLevelIcon" /></el-icon>
                {{ userInfo.memberLevelName }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <!-- 标签页切换 -->
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 个人信息标签页 - 所有用户都有 -->
          <el-tab-pane label="个人信息" name="info">
            <div class="profile-data-container">
              <!-- 查看模式 -->
              <div v-if="!isEditing" class="profile-info">
                <div class="info-row">
                  <div class="info-label">用户名</div>
                  <div class="info-value">{{ userInfo.username }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">邮箱</div>
                  <div class="info-value">{{ userInfo.email }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">电话</div>
                  <div class="info-value">{{ userInfo.phone }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">真实姓名</div>
                  <div class="info-value">{{ userInfo.realName }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">身份证号</div>
                  <div class="info-value">{{ userInfo.idCard }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">注册时间</div>
                  <div class="info-value">{{ userInfo.joinTime }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">会员等级</div>
                  <div class="info-value">
                    <el-tag 
                      :type="memberLevelType" 
                      class="member-level-tag"
                      :effect="userInfo.memberLevel > 2 ? 'dark' : 'light'"
                    >
                      <el-icon class="member-icon"><component :is="memberLevelIcon" /></el-icon>
                      {{ userInfo.memberLevelName }}
                    </el-tag>
                    <div class="member-benefits">
                      <span v-if="userInfo.memberLevel >= 2">{{ 10 - userInfo.memberLevel }}折</span>
                      <span v-if="userInfo.memberLevel >= 3">行李+{{ userInfo.memberLevel - 1 }}件</span>
                      <span v-if="userInfo.memberLevel >= 4">优先登机</span>
                      <span v-if="userInfo.memberLevel >= 5">专属客服</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 编辑模式 -->
              <div v-else class="profile-edit">
                <div class="info-row">
                  <div class="info-label">用户名</div>
                  <div class="info-value readonly">{{ userInfo.username }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">邮箱</div>
                  <div class="info-value">
                    <el-input v-model="editingUserInfo.email" />
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">电话</div>
                  <div class="info-value">
                    <el-input v-model="editingUserInfo.phone" />
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">真实姓名</div>
                  <div class="info-value">
                    <el-input v-model="editingUserInfo.realName" />
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">身份证号</div>
                  <div class="info-value">
                    <el-input v-model="editingUserInfo.idCard" />
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-label">注册时间</div>
                  <div class="info-value readonly">{{ userInfo.joinTime }}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">会员等级</div>
                  <div class="info-value readonly">
                    <el-tag 
                      :type="memberLevelType" 
                      class="member-level-tag"
                      :effect="userInfo.memberLevel > 2 ? 'dark' : 'light'"
                    >
                      <el-icon class="member-icon"><component :is="memberLevelIcon" /></el-icon>
                      {{ userInfo.memberLevelName }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="profile-actions">
              <template v-if="!isEditing">
                <el-button type="primary" @click="startEditing">修改资料</el-button>
                <el-button @click="openPasswordDialog">修改密码</el-button>
              </template>
              <template v-else>
                <el-button type="primary" @click="saveEditing" :loading="loading">保存</el-button>
                <el-button @click="cancelEditing">取消</el-button>
              </template>
            </div>
          </el-tab-pane>
          
          <!-- 我的订单标签页 - 只有普通用户才有 -->
          <el-tab-pane label="我的订单" name="orders" v-if="isNormalUser">
            <div class="orders-container">
              <el-table :data="orders" style="width: 100%">
                <el-table-column prop="id" label="订单号" width="140" />
                <el-table-column prop="flightNo" label="航班号" width="100" />
                <el-table-column label="行程" min-width="150">
                  <template #default="scope">
                    {{ scope.row.departureCity }} → {{ scope.row.arrivalCity }}
                  </template>
                </el-table-column>
                <el-table-column prop="departureTime" label="出发日期" width="120" />
                <el-table-column prop="price" label="价格" width="100">
                  <template #default="scope">
                    ¥{{ scope.row.price }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === '已出票' ? 'warning' : 
                             scope.row.status === '已完成' ? 'success' : 'info'"
                    >
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button size="small" @click="viewOrderDetail(scope.row.id)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 历史会话标签页 - 只有普通用户才有 -->
          <el-tab-pane label="历史会话" name="conversations" v-if="isNormalUser">
            <div class="conversations-container">
              <el-table :data="conversations" style="width: 100%">
                <el-table-column prop="serviceUserName" label="客服" width="120" />
                <el-table-column prop="lastMessage" label="最后消息" min-width="200" show-overflow-tooltip />
                <el-table-column prop="updatedAt" label="时间" width="160" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag type="info">{{ scope.row.status === 0 ? '进行中' : 
                                          scope.row.status === 1 ? '已结束' :
                                          scope.row.status === 2 ? '已转接' : '未知' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button size="small" @click="viewConversationDetail(scope.row.uuid)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      v-model="passwordDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #f5f7fa;
  box-sizing: border-box;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.header {
  height: 60px;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.back-button {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.back-button span {
  margin-left: 5px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.placeholder {
  width: 60px;
}

.profile-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 20px;
  width: 100%;
  box-sizing: border-box;
  overflow: auto;
}

.profile-card {
  width: 100%;
  max-width: 900px;
}

.profile-header {
  display: flex;
  align-items: center;
}

.profile-title {
  margin-left: 20px;
}

.profile-title h2 {
  margin: 0 0 5px 0;
}

.user-tags {
  display: flex;
  gap: 10px;
}

.member-level-tag {
  display: flex;
  align-items: center;
  font-weight: bold;
  padding: 0 10px;
}

.member-level-tag.el-tag--info {
  background-color: #e9e9eb;
  border-color: #d3d4d6;
}

.member-level-tag.el-tag--warning {
  background-color: #faecd8;
  border-color: #e6a23c;
  color: #b88230;
}

.member-level-tag.el-tag--success {
  background-color: #f0f9eb;
  border-color: #85ce61;
  color: #529b2e;
}

.member-level-tag.el-tag--danger {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #c45656;
}

.member-level-tag.el-tag--warning.el-tag--dark {
  background-color: #e6a23c;
  border-color: #e6a23c;
  color: #ffffff;
}

.member-level-tag.el-tag--success.el-tag--dark {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #ffffff;
}

.member-level-tag.el-tag--danger.el-tag--dark {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #ffffff;
}

.member-icon {
  margin-right: 5px;
}

.member-benefits {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.member-benefits span {
  font-size: 12px;
  color: #666;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.profile-tabs {
  margin-top: 20px;
}

.profile-data-container {
  min-height: 240px;
  margin: 20px 0;
}

.profile-info,
.profile-edit {
  display: flex;
  flex-direction: column;
}

.info-row {
  display: flex;
  min-height: 50px;
  margin-bottom: 10px;
  align-items: flex-start;
  padding: 8px 0;
}

.info-label {
  width: 100px;
  color: #909399;
  flex-shrink: 0;
  padding-top: 8px;
}

.info-value {
  flex: 1;
  color: #303133;
}

.info-value .el-input {
  width: 100%;
}

.readonly {
  line-height: 40px;
  color: #909399;
}

.profile-actions {
  display: flex;
  justify-content: flex-start;
  gap: 15px;
  margin-top: 20px;
}

.orders-container,
.conversations-container {
  margin: 20px 0;
}

@media (max-width: 768px) {
  .profile-card {
    width: 100%;
  }
}
</style> 