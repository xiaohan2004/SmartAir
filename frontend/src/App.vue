<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* 全局样式 - 这些样式会覆盖main.css中的样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background-color: #f5f7fa; /* 统一背景色 */
}

#app {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

/* Element Plus组件样式 */
.el-card {
  width: 100%;
}

.el-menu {
  width: 100%;
  height: 100%;
}

.el-table {
  width: 100%;
}

/* 布局容器样式 */
.user-container,
.service-container,
.admin-container,
.profile-container,
.login-container {
  width: 100%;
  height: 100%;
  padding: 0;
  margin: 0;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.main-content {
  width: 100%;
  height: calc(100% - 60px); /* 顶部导航栏高度为60px */
  margin: 0;
  overflow: auto;
  position: relative;
}

.el-container {
  margin: 0;
  padding: 0;
  border: none;
  height: 100%;
  width: 100%;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}
</style>
