<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

const props = defineProps({
  menuItems: {
    type: Array,
    required: true
  },
  activeMenu: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['select']);

// 菜单项选择
const handleSelect = (index) => {
  emit('select', index);
};

// 检查是否是航班管理
const isFlightItem = (item) => {
  return item.index === 'flights' || item.title.includes('航班');
};
</script>

<template>
  <div class="side-menu">
    <el-menu
      :default-active="activeMenu"
      class="menu"
      @select="handleSelect"
    >
      <el-menu-item 
        v-for="item in menuItems" 
        :key="item.index" 
        :index="item.index"
      >
        <!-- 对航班管理使用自定义图标 -->
        <template v-if="isFlightItem(item)">
          <div class="custom-icon-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="custom-icon">
              <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z"></path>
            </svg>
          </div>
        </template>
        <el-icon v-else-if="item.icon">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped>
.side-menu {
  height: 100%;
}

.menu {
  height: 100%;
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-icon) {
  margin-right: 8px;
  font-size: 18px;
  color: #606266;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #409eff;
}

.custom-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  width: 24px;
  height: 24px;
}

.custom-icon {
  width: 18px;
  height: 18px;
  color: #606266;
}

:deep(.el-menu-item.is-active) .custom-icon {
  color: #409eff;
}
</style> 