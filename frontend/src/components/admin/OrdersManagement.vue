<script setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { orderApi } from '@/api';

const orders = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredOrders = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const paginatedOrders = ref([]);

// 订单详情相关
const detailDialogVisible = ref(false);
const detailLoading = ref(false);
const orderDetail = reactive({
  // 基础订单信息
  orderId: null,
  seatNo: '',
  status: null,
  createdAt: '',

  // 用户信息
  userId: null,
  username: '',
  realName: '',
  phone: '',

  // 航班信息
  flightId: null,
  flightNo: '',
  departureCity: '',
  arrivalCity: '',
  scheduledDepartureTime: '',
  scheduledArrivalTime: '',
  airline: '',
  aircraftType: '',
  price: 0
});

// 订单状态选项 (根据数据库中的状态定义)
const statusOptions = [
  { value: 'all', label: '全部' },
  { value: 1, label: '购票成功' },
  { value: 2, label: '已取消' }
];

// 当前选中的状态
const currentStatus = ref('all');

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true;
  try {
    // 从API获取订单列表
    const response = await orderApi.listAllOrders();
    if (response.code === 200) {
      // 直接使用API返回的数据
      orders.value = response.data;
      console.log('获取到的订单数据:', orders.value);
    } else {
      throw new Error(response.message || '获取订单列表失败');
    }
    filterOrders();
  } catch (error) {
    console.error('获取订单列表失败:', error);
    ElMessage.error('获取订单列表失败');
  } finally {
    loading.value = false;
  }
};

// 筛选订单
const filterOrders = () => {
  // 先根据状态筛选
  let statusFiltered = currentStatus.value === 'all' 
    ? orders.value 
    : orders.value.filter(order => order.status === currentStatus.value);
  
  // 再根据搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filteredOrders.value = statusFiltered.filter(order => 
      (order.id && String(order.id).includes(searchValue)) ||
      (order.userId && String(order.userId).includes(searchValue)) ||
      (order.flightId && String(order.flightId).includes(searchValue)) ||
      (order.seatNo && order.seatNo.toLowerCase().includes(searchValue)) ||
      (order.user && order.user.username && order.user.username.toLowerCase().includes(searchValue)) ||
      (order.flight && order.flight.flightNo && order.flight.flightNo.toLowerCase().includes(searchValue)) ||
      (order.flight && order.flight.departureCity && order.flight.departureCity.toLowerCase().includes(searchValue)) ||
      (order.flight && order.flight.arrivalCity && order.flight.arrivalCity.toLowerCase().includes(searchValue))
    );
  } else {
    filteredOrders.value = statusFiltered;
  }
  
  // 更新总数
  total.value = filteredOrders.value.length;
  
  // 更新分页数据
  updatePaginatedData();
};

// 更新分页后的数据
const updatePaginatedData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  paginatedOrders.value = filteredOrders.value.slice(startIndex, endIndex);
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
  filterOrders();
};

// 切换订单状态
const handleStatusChange = () => {
  currentPage.value = 1; // 筛选时重置为第一页
  filterOrders();
};

// 查看订单详情
const viewOrderDetail = async (orderId) => {
  detailLoading.value = true;
  detailDialogVisible.value = true;
  
  try {
    // 尝试调用获取详细订单信息的API
    let response;
    try {
      response = await orderApi.getOrderDetail(orderId);
    } catch (detailError) {
      console.warn('获取订单详情失败，尝试获取基本订单信息:', detailError);
      // 如果获取详情失败，尝试获取基本订单信息
      response = await orderApi.getOrderById(orderId);
    }
    
    if (response.code === 200) {
      // 更新订单详情数据
      const data = response.data;
      
      // 根据返回的数据结构设置订单详情
      Object.assign(orderDetail, {
        orderId: data.id || data.orderId,
        seatNo: data.seatNo || '',
        status: data.status,
        createdAt: data.createdAt,
        
        // 用户信息 - 可能来自关联数据或详情API
        userId: data.userId,
        username: data.username || (data.user ? data.user.username : `用户${data.userId}`),
        realName: data.realName || (data.user ? data.user.realName : ''),
        phone: data.phone || (data.user ? data.user.phone : ''),
        
        // 航班信息 - 可能来自关联数据或详情API
        flightId: data.flightId,
        flightNo: data.flightNo || (data.flight ? data.flight.flightNo : `航班${data.flightId}`),
        departureCity: data.departureCity || (data.flight ? data.flight.departureCity : ''),
        arrivalCity: data.arrivalCity || (data.flight ? data.flight.arrivalCity : ''),
        scheduledDepartureTime: data.scheduledDepartureTime || (data.flight ? data.flight.scheduledDepartureTime : ''),
        scheduledArrivalTime: data.scheduledArrivalTime || (data.flight ? data.flight.scheduledArrivalTime : ''),
        airline: data.airline || (data.flight ? data.flight.airline : ''),
        aircraftType: data.aircraftType || (data.flight ? data.flight.aircraftType : ''),
        price: data.price || (data.flight ? data.flight.price : 0)
      });
      
      console.log('订单详情:', orderDetail);
    } else {
      throw new Error(response.message || '获取订单详情失败');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败');
    detailDialogVisible.value = false;
  } finally {
    detailLoading.value = false;
  }
};

// 取消订单
const cancelOrder = (order) => {
  if (order.status !== 1) { // 只有购票成功的订单可以取消
    ElMessage.warning('只有购票成功的订单可以取消');
    return;
  }
  
  ElMessageBox.confirm(
    `确定要取消订单 #${order.id} 吗？`,
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      // 调用取消订单API
      await orderApi.cancelOrder(order.id);
      ElMessage.success('订单已取消');
      fetchOrders(); // 重新获取订单列表
    } catch (error) {
      ElMessage.error('取消订单失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 1: return 'success'; // 购票成功
    case 2: return 'danger';  // 已取消
    default: return 'info';
  }
};

// 获取状态名称
const getStatusName = (status) => {
  switch (status) {
    case 1: return '购票成功';
    case 2: return '已取消';
    default: return '未知';
  }
};

// 获取用户名
const getUserName = (order) => {
  return order.user ? order.user.username : `用户ID - ${order.userId}`;
};

// 获取航班号
const getFlightNo = (order) => {
  return order.flight ? order.flight.flightNo : `航班ID - ${order.flightId}`;
};

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchOrders();
});
</script>

<template>
  <div class="orders-management">
    <!-- 搜索和筛选 -->
    <div class="action-bar">
      <div class="status-filter">
        <el-radio-group v-model="currentStatus" @change="handleStatusChange">
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
        placeholder="搜索订单(ID/用户/航班/座位/城市)"
        prefix-icon="Search"
        @input="handleSearchInput"
        style="width: 350px; margin-left: auto;"
      />
    </div>
    
    <!-- 订单列表 -->
    <el-table
      :data="paginatedOrders"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="订单ID" min-width="80" />
      <el-table-column label="用户ID" min-width="120">
        <template #default="scope">
          {{ getUserName(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="航班ID" min-width="120">
        <template #default="scope">
          {{ getFlightNo(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="seatNo" label="座位号" min-width="80" />
      <el-table-column label="下单时间" min-width="150">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="160">
        <template #default="scope">
          <div class="action-buttons">
            <el-button 
              size="small" 
              type="primary" 
              @click="viewOrderDetail(scope.row.id)"
            >
              详情
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="cancelOrder(scope.row)"
              :disabled="scope.row.status !== 1"
            >
              取消
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
    
    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="650px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单ID" :span="2">{{ orderDetail.orderId }}</el-descriptions-item>
          <el-descriptions-item label="座位号">{{ orderDetail.seatNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(orderDetail.status)">{{ getStatusName(orderDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatDateTime(orderDetail.createdAt) }}</el-descriptions-item>
          
          <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ orderDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ orderDetail.realName || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.phone || '未提供' }}</el-descriptions-item>
          
          <el-descriptions-item label="航班信息" :span="2" class="flight-info-header">
            <div class="flight-header">
              <span class="flight-no">{{ orderDetail.flightNo }}</span>
              <span class="airline">{{ orderDetail.airline }}</span>
            </div>
          </el-descriptions-item>
          
          <el-descriptions-item label="出发城市">{{ orderDetail.departureCity }}</el-descriptions-item>
          <el-descriptions-item label="到达城市">{{ orderDetail.arrivalCity }}</el-descriptions-item>
          <el-descriptions-item label="计划出发时间">{{ formatDateTime(orderDetail.scheduledDepartureTime) }}</el-descriptions-item>
          <el-descriptions-item label="计划到达时间">{{ formatDateTime(orderDetail.scheduledArrivalTime) }}</el-descriptions-item>
          <el-descriptions-item label="机型">{{ orderDetail.aircraftType }}</el-descriptions-item>
          <el-descriptions-item label="票价">¥{{ orderDetail.price }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="danger" 
            @click="cancelOrder({id: orderDetail.orderId || orderDetail.id, status: orderDetail.status})"
            :disabled="orderDetail.status !== 1"
          >
            取消订单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.orders-management {
  width: 100%;
}

.action-bar {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.status-filter {
  margin-right: 20px;
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

.flight-info-header {
  font-weight: bold;
}

.flight-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.flight-no {
  font-size: 16px;
  font-weight: bold;
}

.airline {
  color: #666;
}
</style> 