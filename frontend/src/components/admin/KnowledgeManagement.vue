<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import knowledgeApi from '@/api/knowledge';

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
  tags: []
});

// 对话框可见性
const dialogVisible = ref(false);
const dialogTitle = ref('添加知识文章');

// 获取知识库列表
const fetchKnowledgeList = async () => {
  loading.value = true;
  try {
    const response = await knowledgeApi.getAllKnowledge();
    if (response.code === 200) {
      // 转换API返回的数据格式为组件所需的格式
      knowledgeList.value = response.data.map(item => {
        // 从tags中提取分类信息，如果没有则默认为未分类
        let categoryId = 1; // 默认分类
        try {
          if (item.tags && item.tags.length > 0) {
            const categoryTag = item.tags.find(tag => tag.startsWith('category:'));
            if (categoryTag) {
              // 支持两种格式：category:id 或 category:id:name
              const parts = categoryTag.split(':');
              if (parts.length >= 2) {
                categoryId = parseInt(parts[1]) || 1;
              }
            }
          }
        } catch (e) {
          console.warn('无法解析分类ID:', e);
        }
        
        return {
          id: item.id,
          title: item.title,
          content: item.content,
          category: categoryId,
          tags: item.tags || [],
          updatedTime: item.lastUpdated ? new Date(item.lastUpdated).toLocaleString() : '-'
        };
      });
      filterKnowledge();
    } else {
      throw new Error(response.message || '获取知识库列表失败');
    }
  } catch (error) {
    console.error('获取知识库列表失败:', error);
    ElMessage.error('获取知识库列表失败: ' + (error.message || '未知错误'));
  } finally {
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
    tags: []
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
    tags: article.tags || []
  });
  
  dialogTitle.value = '编辑知识文章';
  dialogVisible.value = true;
};

// 保存知识文章
const saveArticle = async () => {
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
  
  // 准备API所需的数据格式
  const categoryId = parseInt(articleForm.category);
  const categoryName = getCategoryName(categoryId);
  
  // 构建标签数组，确保包含分类标签
  const tags = [...(articleForm.tags || [])];
  // 移除旧的分类标签
  const filteredTags = tags.filter(tag => !tag.startsWith('category:'));
  // 添加新的分类标签，包含分类ID和名称
  filteredTags.push(`category:${categoryId}:${categoryName}`);
  
  const apiData = {
    title: articleForm.title,
    content: articleForm.content,
    tags: filteredTags
  };
  
  try {
    loading.value = true;
    
    if (articleForm.id) {
      // 更新现有文章
      const response = await knowledgeApi.updateKnowledge(articleForm.id, apiData);
      if (response.code === 200) {
        ElMessage.success('文章已更新');
      } else {
        throw new Error(response.message || '更新文章失败');
      }
    } else {
      // 添加新文章
      const response = await knowledgeApi.createKnowledge(apiData);
      if (response.code === 200) {
        ElMessage.success('文章已添加');
      } else {
        throw new Error(response.message || '添加文章失败');
      }
    }
    
    // 关闭对话框并刷新列表
    dialogVisible.value = false;
    fetchKnowledgeList();
  } catch (error) {
    console.error('保存文章失败:', error);
    ElMessage.error('保存文章失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
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
  ).then(async () => {
    try {
      loading.value = true;
      const response = await knowledgeApi.deleteKnowledge(id);
      
      if (response.code === 200) {
        ElMessage.success('文章已删除');
        fetchKnowledgeList();
      } else {
        throw new Error(response.message || '删除文章失败');
      }
    } catch (error) {
      console.error('删除文章失败:', error);
      ElMessage.error('删除文章失败: ' + (error.message || '未知错误'));
    } finally {
      loading.value = false;
    }
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
          style="width: 500px; margin-left: 10px;"
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