<template>
  <div class="admin-home">
    <el-card class="welcome-card">
      <template #header>
        <span>管理员首页</span>
      </template>
      <div class="welcome-content">
        <p class="system-name">当前系统：学生信息管理系统</p>
        <p class="welcome-msg">欢迎您使用本系统</p>
        <p class="username">当前用户：{{ auth.name || auth.userId }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.admin-home {
  max-width: 600px;
}
.welcome-card {
  margin-top: 20px;
}
.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.system-name {
  font-size: 18px;
  font-weight: bold;
}
.welcome-msg {
  font-size: 16px;
  color: #666;
}
.username {
  font-size: 14px;
  color: #409eff;
}
</style>
