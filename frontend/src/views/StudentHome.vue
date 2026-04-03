<template>
  <div class="student-home">
    <el-card class="welcome-card">
      <template #header>
        <span>学生首页</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学号">{{ auth.userId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ auth.name }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ auth.role }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.student-home {
  max-width: 600px;
}
.welcome-card {
  margin-top: 20px;
}
</style>
