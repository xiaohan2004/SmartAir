<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import promptApi from '@/api/prompt';

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
  scenario: ''
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加提示词');

// 获取提示词列表
const fetchPrompts = async () => {
  loading.value = true;
  try {
    const response = await promptApi.getAllPrompts();
    if (response.code === 200) {
      // 转换API返回的数据格式为组件所需的格式
      prompts.value = response.data.map(item => {
        // 解析描述中的场景信息
        let scenarioId = 1;
        try {
          if (item.description && item.description.includes(':')) {
            scenarioId = parseInt(item.description.split(':')[0]) || 1;
          }
        } catch (e) {
          console.warn('无法解析场景ID:', e);
        }
        
        return {
          id: item.id,
          title: item.name,
          content: item.template,
          scenario: scenarioId,
          updatedTime: item.updatedAt ? new Date(item.updatedAt).toLocaleString() : '-'
        };
      });
      filterPrompts();
    } else {
      throw new Error(response.message || '获取提示词列表失败');
    }
  } catch (error) {
    console.error('获取提示词列表失败:', error);
    ElMessage.error('获取提示词列表失败: ' + (error.message || '未知错误'));
  } finally {
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
    scenario: ''
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
    scenario: prompt.scenario.toString()
  });
  
  dialogTitle.value = '编辑提示词';
  dialogVisible.value = true;
};

// 保存提示词
const savePrompt = async () => {
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
  
  // 准备API所需的数据格式
  const apiData = {
    name: promptForm.title,
    template: promptForm.content,
    description: `${promptForm.scenario}:${getScenarioName(parseInt(promptForm.scenario))}`
  };
  
  try {
    loading.value = true;
    
    if (promptForm.id) {
      // 更新现有提示词
      const response = await promptApi.updatePrompt(promptForm.id, apiData);
      if (response.code === 200) {
        ElMessage.success('提示词已更新');
      } else {
        throw new Error(response.message || '更新提示词失败');
      }
    } else {
      // 添加新提示词
      const response = await promptApi.createPrompt(apiData);
      if (response.code === 200) {
        ElMessage.success('提示词已添加');
      } else {
        throw new Error(response.message || '添加提示词失败');
      }
    }
    
    // 关闭对话框并刷新列表
    dialogVisible.value = false;
    fetchPrompts();
  } catch (error) {
    console.error('保存提示词失败:', error);
    ElMessage.error('保存提示词失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
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
  ).then(async () => {
    try {
      loading.value = true;
      const response = await promptApi.deletePrompt(id);
      
      if (response.code === 200) {
        ElMessage.success('提示词已删除');
        fetchPrompts();
      } else {
        throw new Error(response.message || '删除提示词失败');
      }
    } catch (error) {
      console.error('删除提示词失败:', error);
      ElMessage.error('删除提示词失败: ' + (error.message || '未知错误'));
    } finally {
      loading.value = false;
    }
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
          style="width: 500px; margin-left: 10px;"
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