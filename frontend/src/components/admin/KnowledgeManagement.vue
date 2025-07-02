<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const knowledgeList = ref([]);
const searchText = ref('');
const filteredKnowledge = ref([]);

// 分类选项
const categories = [
  { id: 1, name: '航班信息' },
  { id: 2, name: '退改政策' },
  { id: 3, name: '会员服务' },
  { id: 4, name: '购票指南' },
  { id: 5, name: '行李规定' }
];

// 选中的分类
const selectedCategory = ref('');

// 文章表单
const articleForm = reactive({
  id: null,
  title: '',
  content: '',
  category: '',
  priority: 0,
  status: 'active'
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加知识文章');

// 模拟知识库数据
const mockKnowledgeData = [
  { id: 101, title: '如何查询航班信息', content: '您可以通过输入航班号、出发地和目的地等信息查询航班...', category: 1, priority: 3, status: 'active', updatedTime: '2023-10-15 14:30:00' },
  { id: 102, title: '退票手续费如何计算', content: '退票手续费根据航班起飞前的时间和票价计算...', category: 2, priority: 5, status: 'active', updatedTime: '2023-10-20 09:45:00' },
  { id: 103, title: '会员积分规则说明', content: '会员可以通过购票、乘坐航班等方式获取积分...', category: 3, priority: 4, status: 'active', updatedTime: '2023-10-25 16:20:00' },
  { id: 104, title: '航班延误处理方案', content: '当航班因天气、机械故障等原因延误时...', category: 1, priority: 5, status: 'active', updatedTime: '2023-11-01 11:10:00' },
  { id: 105, title: '如何在线选座', content: '完成购票后，您可以通过以下步骤在线选择座位...', category: 4, priority: 2, status: 'inactive', updatedTime: '2023-11-05 10:30:00' }
];

// 获取知识库列表
const fetchKnowledgeList = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的API获取知识库列表
    // 模拟API调用
    setTimeout(() => {
      knowledgeList.value = mockKnowledgeData;
      filterKnowledge();
      loading.value = false;
    }, 500);
  } catch (error) {
    ElMessage.error('获取知识库列表失败');
    loading.value = false;
  }
};

// 筛选知识库内容
const filterKnowledge = () => {
  let filtered = knowledgeList.value;
  
  // 按分类筛选
  if (selectedCategory.value) {
    filtered = filtered.filter(item => item.category === parseInt(selectedCategory.value));
  }
  
  // 按搜索文本筛选
  if (searchText.value) {
    const searchValue = searchText.value.toLowerCase();
    filtered = filtered.filter(item => 
      item.title.toLowerCase().includes(searchValue) || 
      item.content.toLowerCase().includes(searchValue)
    );
  }
  
  filteredKnowledge.value = filtered;
};

// 监听搜索输入
const handleSearchInput = () => {
  filterKnowledge();
};

// 监听分类选择
const handleCategoryChange = () => {
  filterKnowledge();
};

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.find(c => c.id === categoryId);
  return category ? category.name : '未分类';
};

// 打开添加对话框
const openAddDialog = () => {
  // 重置表单
  Object.assign(articleForm, {
    id: null,
    title: '',
    content: '',
    category: '',
    priority: 0,
    status: 'active'
  });
  
  dialogTitle.value = '添加知识文章';
  dialogVisible.value = true;
};

// 打开编辑对话框
const openEditDialog = (article) => {
  // 填充表单数据
  Object.assign(articleForm, {
    id: article.id,
    title: article.title,
    content: article.content,
    category: article.category.toString(),
    priority: article.priority,
    status: article.status
  });
  
  dialogTitle.value = '编辑知识文章';
  dialogVisible.value = true;
};

// 保存知识文章
const saveArticle = () => {
  if (!articleForm.title.trim()) {
    ElMessage.warning('请输入文章标题');
    return;
  }
  
  if (!articleForm.content.trim()) {
    ElMessage.warning('请输入文章内容');
    return;
  }
  
  if (!articleForm.category) {
    ElMessage.warning('请选择文章分类');
    return;
  }
  
  // 表单验证通过，保存数据
  const formData = {
    ...articleForm,
    category: parseInt(articleForm.category)
  };
  
  if (articleForm.id) {
    // 更新现有文章
    const index = knowledgeList.value.findIndex(item => item.id === articleForm.id);
    if (index !== -1) {
      knowledgeList.value[index] = {
        ...knowledgeList.value[index],
        ...formData,
        updatedTime: new Date().toLocaleString()
      };
      ElMessage.success('文章已更新');
    }
  } else {
    // 添加新文章
    const newArticle = {
      ...formData,
      id: Date.now(), // 模拟生成ID
      updatedTime: new Date().toLocaleString()
    };
    knowledgeList.value.push(newArticle);
    ElMessage.success('文章已添加');
  }
  
  // 关闭对话框并刷新列表
  dialogVisible.value = false;
  filterKnowledge();
};

// 删除知识文章
const deleteArticle = (id) => {
  ElMessageBox.confirm(
    '确定要删除该文章吗？',
    '删除文章',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API删除文章
    const index = knowledgeList.value.findIndex(item => item.id === id);
    if (index !== -1) {
      knowledgeList.value.splice(index, 1);
      ElMessage.success('文章已删除');
      filterKnowledge();
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 修改文章状态
const toggleArticleStatus = (article) => {
  const newStatus = article.status === 'active' ? 'inactive' : 'active';
  const action = newStatus === 'active' ? '启用' : '禁用';
  
  ElMessageBox.confirm(
    `确定要${action}该文章吗？`,
    `${action}文章`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // 这里应该调用实际的API修改状态
    article.status = newStatus;
    ElMessage.success(`文章已${action}`);
    filterKnowledge();
  }).catch(() => {
    // 用户取消操作
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchKnowledgeList();
});
</script>

<template>
  <div class="knowledge-management">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="openAddDialog">添加知识文章</el-button>
      
      <div class="filters">
        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          clearable
          @change="handleCategoryChange"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id.toString()"
          />
        </el-select>
        
        <el-input
          v-model="searchText"
          placeholder="搜索文章标题或内容"
          prefix-icon="Search"
          @input="handleSearchInput"
          style="width: 250px; margin-left: 10px;"
        />
      </div>
    </div>
    
    <!-- 知识文章列表 -->
    <el-table
      :data="filteredKnowledge"
      style="width: 100%; margin-top: 20px;"
      v-loading="loading"
      border
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column prop="content" label="内容" min-width="250">
        <template #default="scope">
          <div class="content-preview">{{ scope.row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100">
        <template #default="scope">
          {{ getCategoryName(scope.row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
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
            @click="toggleArticleStatus(scope.row)"
            style="margin-left: 5px;"
          >
            {{ scope.row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            plain
            @click="deleteArticle(scope.row.id)"
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
      <el-form :model="articleForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input
            v-model="articleForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入文章内容"
          />
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select v-model="articleForm.category" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id.toString()"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级">
          <el-rate v-model="articleForm.priority" :max="5" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="articleForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveArticle">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.knowledge-management {
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