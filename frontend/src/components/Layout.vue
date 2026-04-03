<template>
  <div class="layout">
    <div class="header">
      <div class="header-left">欢迎访问学生信息管理系统</div>
      <div class="header-right">
        <span class="username">{{ auth.name || auth.userId }}</span>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    <div class="body">
      <div class="sidebar">
        <el-menu :default-active="activeMenu" background-color="#1a1a2e" text-color="#fff" active-text-color="#409eff" router>
          <template v-if="auth.role === '学生'">
            <el-menu-item index="/student">学生首页</el-menu-item>
            <el-sub-menu index="student-selection">
              <template #title><span>选课管理</span></template>
              <el-menu-item index="/student/selections">选课查询</el-menu-item>
              <el-menu-item index="/student/my-selections">已选课程</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="student-grades">
              <template #title><span>学生成绩管理</span></template>
              <el-menu-item index="/student/grades">成绩查询</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="student-profile">
              <template #title><span>个人中心</span></template>
              <el-menu-item index="/student/profile">信息修改</el-menu-item>
            </el-sub-menu>
          </template>
          <template v-else-if="auth.role === '老师'">
            <el-menu-item index="/teacher">教师首页</el-menu-item>
            <el-sub-menu index="teacher-courses">
              <template #title><span>开设课程</span></template>
              <el-menu-item index="/teacher/courses">课程查询</el-menu-item>
              <el-menu-item index="/teacher/my-courses">已开课程</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="teacher-grades">
              <template #title><span>成绩管理</span></template>
              <el-menu-item index="/teacher/grades">成绩查询</el-menu-item>
              <el-menu-item index="/teacher/grade-entry">成绩录入</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="teacher-profile">
              <template #title><span>个人中心</span></template>
              <el-menu-item index="/teacher/profile">信息修改</el-menu-item>
            </el-sub-menu>
          </template>
          <template v-else-if="auth.role === '管理员'">
            <el-menu-item v-for="item in adminMenus" :key="item.path" :index="item.path">
              <span>{{ item.label }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const adminMenus = [
  { path: '/admin', label: '首页' },
  { path: '/admin/students', label: '学生管理' },
  { path: '/admin/teachers', label: '教师管理' },
  { path: '/admin/courses', label: '课程管理' },
  { path: '/admin/openings', label: '开课管理' },
  { path: '/admin/grades', label: '成绩管理' }
]

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  height: 60px;
  background-color: #1a1a2e;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}
.header-left {
  font-size: 18px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
.username {
  color: #ddd;
}
.body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.sidebar {
  width: 200px;
  background-color: #1a1a2e;
  flex-shrink: 0;
  overflow-y: auto;
}
.sidebar .el-menu {
  border-right: none;
}
.main-content {
  flex: 1;
  background-color: #fff;
  padding: 20px;
  overflow-y: auto;
}
</style>
