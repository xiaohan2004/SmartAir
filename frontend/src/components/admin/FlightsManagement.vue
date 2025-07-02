<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { flightApi } from '@/api';

const flights = ref([]);
const loading = ref(true);
const searchText = ref('');
const filteredFlights = ref([]);

// 当前选中的状态
const currentStatus = ref('all');

// 日期范围
const dateRange = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const paginatedFlights = ref([]);

// 航班表单
const flightForm = reactive({
  id: null,
  flightNo: '',
  airline: '',
  departureCity: '',
  arrivalCity: '',
  scheduledDepartureTime: '',
  scheduledArrivalTime: '',
  aircraftType: '',
  price: 0
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加航班');

// 获取航班列表
const fetchFlights = async () => {
  loading.value = true;
  try {
    // 从API获取航班列表
    const response = await flightApi.listFlights();
    if (response.code === 200) {
      // 将后端数据映射到前端需要的格式
      flights.value = response.data.map(flight => ({
        id: flight.id,
        flightNo: flight.flightNo,
        airline: flight.airline,
        departureCity: flight.departureCity,
        arrivalCity: flight.arrivalCity,
        scheduledDepartureTime: flight.scheduledDepartureTime,
        scheduledArrivalTime: flight.scheduledArrivalTime,
        aircraftType: flight.aircraftType,
        price: flight.price,
        createdAt: flight.createdAt,
        updatedAt: flight.updatedAt
      }));
      filterFlights();
    } else {
      throw new Error(response.message || '获取航班列表失败');
    }
  } catch (error) {
    console.error('获取航班列表失败:', error);
    ElMessage.error('获取航班列表失败');
  } finally {
    loading.value = false;
  }
};

// 筛选航班
const filterFlights = () => {
  // 根据日期范围筛选
  let filteredResults = flights.value;
  
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    
    filteredResults = filteredResults.filter(flight => {
      const departureDate = new Date(flight.scheduledDepartureTime);
      return departureDate >= startDate && departureDate <= endDate;
    });
  }
  
  // 根据搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filteredFlights.value = filteredResults.filter(flight => 
      flight.flightNo.toLowerCase().includes(searchValue) ||
      flight.airline.toLowerCase().includes(searchValue) ||
      flight.departureCity.toLowerCase().includes(searchValue) ||
      flight.arrivalCity.toLowerCase().includes(searchValue)
    );
  } else {
    filteredFlights.value = filteredResults;
  }
  
  // 更新总数
  total.value = filteredFlights.value.length;
  
  // 更新分页数据
  updatePaginatedData();
};

// 更新分页后的数据
const updatePaginatedData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  paginatedFlights.value = filteredFlights.value.slice(startIndex, endIndex);
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
  filterFlights();
};

// 日期范围变化
const handleDateRangeChange = () => {
  currentPage.value = 1; // 筛选时重置为第一页
  filterFlights();
};

// 打开添加对话框
const openAddDialog = () => {
  // 重置表单
  Object.assign(flightForm, {
    id: null,
    flightNo: '',
    airline: '',
    departureCity: '',
    arrivalCity: '',
    scheduledDepartureTime: '',
    scheduledArrivalTime: '',
    aircraftType: '',
    price: 0
  });
  
  dialogTitle.value = '添加航班';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = (flight) => {
  // 填充表单数据
  Object.assign(flightForm, { ...flight });
  
  dialogTitle.value = '编辑航班';
  dialogVisible.value = true;
};

// 保存航班
const saveFlight = async () => {
  // 表单验证
  if (!flightForm.flightNo.trim()) {
    ElMessage.warning('请输入航班号');
    return;
  }
  
  if (!flightForm.airline.trim()) {
    ElMessage.warning('请输入航空公司');
    return;
  }
  
  if (!flightForm.departureCity.trim() || !flightForm.arrivalCity.trim()) {
    ElMessage.warning('请输入出发地和目的地');
    return;
  }
  
  if (!flightForm.scheduledDepartureTime || !flightForm.scheduledArrivalTime) {
    ElMessage.warning('请选择出发时间和到达时间');
    return;
  }
  
  try {
    loading.value = true;
    
    if (flightForm.id) {
      // 更新航班
      const response = await flightApi.updateFlight(flightForm.id, flightForm);
      if (response.code === 200) {
        ElMessage.success('航班信息已更新');
      } else {
        throw new Error(response.message || '更新航班失败');
      }
    } else {
      // 添加航班
      const response = await flightApi.addFlight(flightForm);
      if (response.code === 200) {
        ElMessage.success('航班已添加');
      } else {
        throw new Error(response.message || '添加航班失败');
      }
    }
    
    // 关闭对话框并刷新列表
    dialogVisible.value = false;
    fetchFlights(); // 重新加载数据
  } catch (error) {
    console.error('保存航班失败:', error);
    ElMessage.error(error.message || '操作失败');
  } finally {
    loading.value = false;
  }
};

// 删除航班
const deleteFlight = (id) => {
  ElMessageBox.confirm('确定要删除这个航班吗？此操作不可逆。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      loading.value = true;
      
      // 调用API删除航班
      const response = await flightApi.deleteFlight(id);
      
      if (response.code === 200) {
        ElMessage.success('航班已删除');
        fetchFlights(); // 重新加载数据
      } else {
        throw new Error(response.message || '删除航班失败');
      }
    } catch (error) {
      console.error('删除航班失败:', error);
      ElMessage.error(error.message || '删除失败');
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 组件挂载时获取航班列表
onMounted(fetchFlights);
</script>

<template>
  <div class="flights-management">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="openAddDialog" class="add-button">
        <el-icon><Plus /></el-icon> 添加航班
      </el-button>
      
      <el-input 
        v-model="searchText" 
        placeholder="搜索航班号、航空公司、城市" 
        clearable 
        @input="handleSearchInput"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="handleDateRangeChange"
        class="date-picker"
      />
    </div>
    
    <!-- 航班表格 -->
    <el-table 
      :data="paginatedFlights" 
      style="width: 100%" 
      border 
      v-loading="loading"
    >
      <el-table-column prop="flightNo" label="航班号" min-width="80" />
      <el-table-column prop="airline" label="航空公司" min-width="120" />
      <el-table-column prop="departureCity" label="出发城市" min-width="90" />
      <el-table-column prop="arrivalCity" label="到达城市" min-width="90" />
      <el-table-column prop="scheduledDepartureTime" label="出发时间" min-width="160" />
      <el-table-column prop="scheduledArrivalTime" label="到达时间" min-width="160" />
      <el-table-column prop="aircraftType" label="机型" min-width="90" />
      <el-table-column prop="price" label="票价" min-width="90">
        <template #default="scope">
          {{ scope.row.price.toFixed(2) }} 元
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="140" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="deleteFlight(scope.row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
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
      />
    </div>
    
    <!-- 添加/编辑航班对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
    >
      <el-form :model="flightForm" label-width="100px">
        <el-form-item label="航班号">
          <el-input v-model="flightForm.flightNo" />
        </el-form-item>
        <el-form-item label="航空公司">
          <el-input v-model="flightForm.airline" />
        </el-form-item>
        <el-form-item label="出发城市">
          <el-input v-model="flightForm.departureCity" />
        </el-form-item>
        <el-form-item label="到达城市">
          <el-input v-model="flightForm.arrivalCity" />
        </el-form-item>
        <el-form-item label="出发时间">
          <el-date-picker
            v-model="flightForm.scheduledDepartureTime"
            type="datetime"
            placeholder="选择出发时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到达时间">
          <el-date-picker
            v-model="flightForm.scheduledArrivalTime"
            type="datetime"
            placeholder="选择到达时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="机型">
          <el-input v-model="flightForm.aircraftType" />
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="flightForm.price" :min="0" :precision="2" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveFlight">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.flights-management {
  width: 100%;
}

.toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.search-input {
  width: 240px;
  margin-right: 0;
}

.date-picker {
  width: 260px;
  margin-right: 0;
}

.add-button {
  margin-left: 0;
  margin-right: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式处理 */
@media (max-width: 992px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-input,
  .date-picker {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style> 