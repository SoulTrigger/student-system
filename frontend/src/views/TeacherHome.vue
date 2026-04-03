<template>
  <div class="teacher-home">
    <el-card class="welcome-card">
      <template #header>
        <span>教师首页</span>
      </template>
      <div class="info-list">
        <div class="info-item">
          <span class="label">工号：</span>
          <span class="value">{{ auth.userId }}</span>
        </div>
        <div class="info-item">
          <span class="label">姓名：</span>
          <span class="value">{{ auth.name }}</span>
        </div>
        <div class="info-item">
          <span class="label">用户类型：</span>
          <span class="value">{{ auth.role }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.welcome-card {
  max-width: 500px;
}
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-item {
  font-size: 16px;
}
.info-item .label {
  color: #666;
  margin-right: 8px;
}
.info-item .value {
  font-weight: bold;
}
</style>
