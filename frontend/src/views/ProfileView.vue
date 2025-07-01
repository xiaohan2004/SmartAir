<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import HeaderNav from '@/components/common/HeaderNav.vue';

const router = useRouter();
const activeTab = ref('info'); // 默认显示个人信息标签页

// 用户信息
const userInfo = reactive({
  username: '测试用户',
  role: '用户', // 可能是'用户'、'客服'或'管理员'
  email: 'test@example.com',
  phone: '138****1234',
  joinTime: '2023-12-01'
});

// 是否为普通用户
const isNormalUser = computed(() => userInfo.role === '用户');

// 编辑状态
const isEditing = ref(false);

// 暂存编辑中的用户信息
const editingUserInfo = reactive({
  username: '',
  email: '',
  phone: ''
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

// 模拟订单数据
const orders = reactive([
  { id: 'ORD20231201001', flight: 'CA1234', from: '北京', to: '上海', date: '2023-12-15', status: '已出票', price: '1280.00' },
  { id: 'ORD20231115002', flight: 'MU5678', from: '上海', to: '广州', date: '2023-11-20', status: '已完成', price: '960.00' },
  { id: 'ORD20231028003', flight: 'CZ8901', from: '广州', to: '成都', date: '2023-11-05', status: '已完成', price: '1420.00' }
]);

// 模拟历史会话数据
const conversations = reactive([
  { id: 1, serviceUser: '客服小王', lastMessage: '您的问题已解决，感谢您的咨询！', time: '2023-12-01 14:30', status: '已结束' },
  { id: 2, serviceUser: '客服小李', lastMessage: '您的退款申请已经处理完成，款项将在1-3个工作日内退回原支付账户。', time: '2023-11-15 10:25', status: '已结束' },
  { id: 3, serviceUser: '客服小张', lastMessage: '您的改签申请已提交，请等待审核结果。', time: '2023-10-28 16:45', status: '已结束' }
]);

// 开始编辑
const startEditing = () => {
  editingUserInfo.username = userInfo.username;
  editingUserInfo.email = userInfo.email;
  editingUserInfo.phone = userInfo.phone;
  isEditing.value = true;
};

// 保存编辑
const saveEditing = () => {
  // 这里预留保存到后端的逻辑
  userInfo.username = editingUserInfo.username;
  userInfo.email = editingUserInfo.email;
  userInfo.phone = editingUserInfo.phone;
  
  ElMessage.success('资料更新成功');
  isEditing.value = false;
};

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false;
};

// 打开修改密码对话框
const openPasswordDialog = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordDialogVisible.value = true;
};

// 提交修改密码
const submitPasswordChange = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      // 这里预留修改密码的后端请求逻辑
      ElMessage.success('密码修改成功');
      passwordDialogVisible.value = false;
    } else {
      return false;
    }
  });
};

// 查看订单详情
const viewOrderDetail = (orderId) => {
  ElMessage.info(`查看订单详情：${orderId}`);
  // 这里可以添加跳转到订单详情页或弹出订单详情对话框的逻辑
};

// 查看会话详情
const viewConversationDetail = (conversationId) => {
  ElMessage.info(`查看会话详情：${conversationId}`);
  // 这里可以添加跳转到会话详情页或弹出会话详情对话框的逻辑
};

// 返回上一页
const goBack = () => {
  router.back();
};
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
      <el-card class="profile-card">
        <div class="profile-header">
          <el-avatar :size="80" icon="User" />
          <div class="profile-title">
            <h2>{{ userInfo.username }}</h2>
            <el-tag>{{ userInfo.role }}</el-tag>
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
                  <div class="info-label">注册时间</div>
                  <div class="info-value">{{ userInfo.joinTime }}</div>
                </div>
              </div>
              
              <!-- 编辑模式 -->
              <div v-else class="profile-edit">
                <div class="info-row">
                  <div class="info-label">用户名</div>
                  <div class="info-value">
                    <el-input v-model="editingUserInfo.username" />
                  </div>
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
                  <div class="info-label">注册时间</div>
                  <div class="info-value readonly">{{ userInfo.joinTime }}</div>
                </div>
              </div>
            </div>
            
            <div class="profile-actions">
              <template v-if="!isEditing">
                <el-button type="primary" @click="startEditing">修改资料</el-button>
                <el-button @click="openPasswordDialog">修改密码</el-button>
              </template>
              <template v-else>
                <el-button type="primary" @click="saveEditing">保存</el-button>
                <el-button @click="cancelEditing">取消</el-button>
              </template>
            </div>
          </el-tab-pane>
          
          <!-- 我的订单标签页 - 只有普通用户才有 -->
          <el-tab-pane label="我的订单" name="orders" v-if="isNormalUser">
            <div class="orders-container">
              <el-table :data="orders" style="width: 100%">
                <el-table-column prop="id" label="订单号" width="140" />
                <el-table-column prop="flight" label="航班号" width="100" />
                <el-table-column label="行程" min-width="150">
                  <template #default="scope">
                    {{ scope.row.from }} → {{ scope.row.to }}
                  </template>
                </el-table-column>
                <el-table-column prop="date" label="出发日期" width="120" />
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
                <el-table-column prop="serviceUser" label="客服" width="120" />
                <el-table-column prop="lastMessage" label="最后消息" min-width="200" show-overflow-tooltip />
                <el-table-column prop="time" label="时间" width="160" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag type="info">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button size="small" @click="viewConversationDetail(scope.row.id)">查看详情</el-button>
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
          <el-button type="primary" @click="submitPasswordChange">确认</el-button>
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
  margin-bottom: 20px;
}

.profile-title {
  margin-left: 20px;
}

.profile-title h2 {
  margin-bottom: 10px;
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
  height: 50px;
  margin-bottom: 10px;
  align-items: center;
}

.info-label {
  width: 100px;
  color: #909399;
  flex-shrink: 0;
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