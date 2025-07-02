<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const prompts = ref([]);
const searchText = ref('');
const filteredPrompts = ref([]);

// 场景选项
const scenarios = [
  { id: 1, name: '订票咨询' },
  { id: 2, name: '航班查询' },
  { id: 3, name: '退改签' },
  { id: 4, name: '会员服务' },
  { id: 5, name: '投诉处理' }
];

// 选中的场景
const selectedScenario = ref('');

// 提示词表单
const promptForm = reactive({
  id: null,
  title: '',
  content: '',
  scenario: '',
  priority: 0,
  status: 'active'
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加提示词');

// 模拟提示词数据
const mockPromptsData = [
  { id: 201, title: '航班查询引导', content: '您是智能客服助手，请根据用户的描述，帮助用户查询航班信息。如果用户未提供完整信息，请引导用户提供出发地、目的地和出行日期。', scenario: 2, priority: 5, status: 'active', updatedTime: '2023-10-10 09:30:00' },
  { id: 202, title: '订票流程指导', content: '您是智能客服助手，负责指导用户完成机票预订。请引导用户提供必要信息，并解释订票流程的各个步骤。', scenario: 1, priority: 4, status: 'active', updatedTime: '2023-10-15 14:20:00' },
  { id: 203, title: '退票政策解释', content: '您是智能客服助手，请根据用户的问题，解释机票退票的政策和流程。请说明退票手续费的计算方式，并引导用户按照正确流程办理退票。', scenario: 3, priority: 3, status: 'active', updatedTime: '2023-10-20 16:15:00' },
  { id: 204, title: '会员权益咨询', content: '您是智能客服助手，负责回答关于会员积分、会员等级和会员权益的问题。请向用户介绍不同会员等级的权益，并指导用户如何有效累积和使用积分。', scenario: 4, priority: 4, status: 'active', updatedTime: '2023-11-01 10:45:00' },
  { id: 205, title: '投诉处理引导', content: '您是智能客服助手，负责初步处理用户的投诉。请安抚用户情绪，收集投诉信息，并告知用户投诉处理的流程和预期。', scenario: 5, priority: 5, status: 'inactive', updatedTime: '2023-11-05 11:30:00' }
];

// 获取提示词列表
const fetchPrompts = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的API获取提示词列表
    // 模拟API调用
    setTimeout(() => {
      prompts.value = mockPromptsData;
      filterPrompts();
      loading.value = false;
    }, 500);
  } catch (error) {
    ElMessage.error('获取提示词列表失败');
    loading.value = false;
  }
};

// 筛选提示词
const filterPrompts = () => {
  let filtered = prompts.value;
  
  // 按场景筛选
  if (selectedScenario.value) {
    filtered = filtered.filter(item => item.scenario === parseInt(selectedScenario.value));
  }
  
  // 按搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(searchValue) || 
      item.content.toLowerCase().includes(searchValue)
    );
  }
  
  filteredPrompts.value = filtered;
};

// 监听搜索输入
const handleSearchInput = () => {
  filterPrompts();
};

// 监听场景选择
const handleScenarioChange = () => {
  filterPrompts();
};

// 获取场景名称
const getScenarioName = (scenarioId) => {
  const scenario = scenarios.find(s => s.id === scenarioId);
  return scenario ? scenario.name : '未分类';
};

// 打开添加对话框
const openAddDialog = () => {
  // 重置表单
  Object.assign(promptForm, {
    id: null,
    title: '',
    content: '',
    scenario: '',
    priority: 0,
    status: 'active'
  });
  
  dialogTitle.value = '添加提示词';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = (prompt) => {
  // 填充表单数据
  Object.assign(promptForm, {
    id: prompt.id,
    title: prompt.title,
    content: prompt.content,
    scenario: prompt.scenario.toString(),
    priority: prompt.priority,
    status: prompt.status
  });
  
  dialogTitle.value = '编辑提示词';
  dialogVisible.value = true;
};

// 保存提示词
const savePrompt = () => {
  if (!promptForm.title.trim()) {
    ElMessage.warning('请输入提示词标题');
    return;
  }
  
  if (!promptForm.content.trim()) {
    ElMessage.warning('请输入提示词内容');
    return;
  }
  
  if (!promptForm.scenario) {
    ElMessage.warning('请选择提示词场景');
    return;
  }
  
  // 表单验证通过，保存数据
  const formData = {
    ...promptForm,
    scenario: parseInt(promptForm.scenario)
  };
  
  if (promptForm.id) {
    // 更新现有提示词
    const index = prompts.value.findIndex(item => item.id === promptForm.id);
    if (index !== -1) {
      prompts.value[index] = {
        ...prompts.value[index],
        ...formData,
        updatedTime: new Date().toLocaleString()
      };
      ElMessage.success('提示词已更新');
    }
  } else {
    // 添加新提示词
    const newPrompt = {
      ...formData,
      id: Date.now(), // 模拟生成ID
      updatedTime: new Date().toLocaleString()
    };
    prompts.value.push(newPrompt);
    ElMessage.success('提示词已添加');
  }
  
  // 关闭对话框并刷新列表
  dialogVisible.value = false;
  filterPrompts();
};

// 删除提示词
const deletePrompt = (id) => {
  ElMessageBox.confirm(
    '确定要删除该提示词吗？',
    '删除提示词',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API删除提示词
    const index = prompts.value.findIndex(item => item.id === id);
    if (index !== -1) {
      prompts.value.splice(index, 1);
      ElMessage.success('提示词已删除');
      filterPrompts();
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 修改提示词状态
const togglePromptStatus = (prompt) => {
  const newStatus = prompt.status === 'active' ? 'inactive' : 'active';
  const action = newStatus === 'active' ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确定要${action}该提示词吗？`,
    `${action}提示词`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API修改状态
    prompt.status = newStatus;
    ElMessage.success(`提示词已${action}`);
    filterPrompts();
  }).catch(() => {
    // 用户取消操作
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchPrompts();
});
</script>

<template>
  <div class="prompts-management">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="openAddDialog">添加提示词</el-button>
      
      <div class="filters">
        <el-select
          v-model="selectedScenario"
          placeholder="选择场景"
          clearable
          @change="handleScenarioChange"
        >
          <el-option
            v-for="scenario in scenarios"
            :key="scenario.id"
            :label="scenario.name"
            :value="scenario.id.toString()"
          />
        </el-select>
        
        <el-input
          v-model="searchText"
          placeholder="搜索提示词标题或内容"
          prefix-icon="Search"
          @input="handleSearchInput"
          style="width: 250px; margin-left: 10px;"
        />
      </div>
    </div>
    
    <!-- 提示词列表 -->
    <el-table
      :data="filteredPrompts"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="150" />
      <el-table-column prop="content" label="内容" min-width="300">
        <template #default="scope">
          <div class="content-preview">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="scenario" label="场景" width="100">
        <template #default="scope">
          {{ getScenarioName(scope.row.scenario) }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="scope">
          <el-rate :model-value="scope.row.priority" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
            {{ scope.row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedTime" label="更新时间" width="150" />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            plain
            @click="openEditDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            size="small" 
            :type="scope.row.status === 'active' ? 'warning' : 'success'" 
            plain
            @click="togglePromptStatus(scope.row)"
            style="margin-left: 5px;"
          >
            {{ scope.row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            plain
            @click="deletePrompt(scope.row.id)"
            style="margin-left: 5px;"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
    >
      <el-form :model="promptForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="promptForm.title" placeholder="请输入提示词标题" />
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input
            v-model="promptForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入提示词内容"
          />
        </el-form-item>
        
        <el-form-item label="场景">
          <el-select v-model="promptForm.scenario" placeholder="请选择场景">
            <el-option
              v-for="scenario in scenarios"
              :key="scenario.id"
              :label="scenario.name"
              :value="scenario.id.toString()"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级">
          <el-rate v-model="promptForm.priority" :max="5" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="promptForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePrompt">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.prompts-management {
  width: 100%;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
}

.filters {
  display: flex;
  align-items: center;
}

.content-preview {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style> 