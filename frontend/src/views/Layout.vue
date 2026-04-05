<template>
  <el-container style="min-height: 100vh">
    <el-aside width="200px">
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <div style="padding: 20px; text-align: center; color: #fff; font-weight: bold;">
          {{ roleLabel }}
        </div>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          {{ item.label }}
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          退出登录
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[store.role] || ''
})

const menuItems = computed(() => {
  const base = '/' + store.role
  const menus = {
    admin: [
      { path: base + '/dashboard', label: '首页' },
      { path: base + '/students', label: '学生管理' },
      { path: base + '/teachers', label: '教师管理' },
      { path: base + '/courses', label: '课程管理' },
      { path: base + '/openings', label: '开课管理' },
      { path: base + '/grades', label: '成绩管理' },
      { path: base + '/logs', label: '操作日志' },
    ],
    teacher: [
      { path: base + '/dashboard', label: '首页' },
      { path: base + '/openings', label: '我的课程' },
      { path: base + '/grades', label: '成绩管理' },
    ],
    student: [
      { path: base + '/dashboard', label: '首页' },
      { path: base + '/selections', label: '选课管理' },
      { path: base + '/grades', label: '我的成绩' },
    ]
  }
  return menus[store.role] || []
})

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
.el-aside {
  background-color: #304156;
}
.el-menu {
  border-right: none;
}
</style>
