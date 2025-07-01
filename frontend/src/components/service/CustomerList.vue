<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  customers: {
    type: Array,
    default: () => []
  },
  currentCustomerId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['select']);

// 选择客户
const selectCustomer = (customer) => {
  emit('select', customer);
};
</script>

<template>
  <div class="customer-list">
    <div
      v-for="customer in customers"
      :key="customer.id"
      class="customer-item"
      :class="{ active: currentCustomerId === customer.id }"
      @click="selectCustomer(customer)"
    >
      <el-avatar :size="40" icon="User" />
      <div class="customer-info">
        <div class="customer-name">{{ customer.name }}</div>
        <div class="customer-message">{{ customer.lastMessage }}</div>
      </div>
      <div class="customer-meta">
        <div class="customer-time">{{ customer.time }}</div>
        <el-badge v-if="customer.unread > 0" :value="customer.unread" class="unread-badge" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.customer-list {
  height: 100%;
  overflow-y: auto;
}

.customer-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}

.customer-item:hover {
  background-color: #f5f7fa;
}

.customer-item.active {
  background-color: #ecf5ff;
}

.customer-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.customer-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.customer-message {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.customer-time {
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.unread-badge {
  margin-top: 2px;
}
</style> 