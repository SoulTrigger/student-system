import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Layout from '../components/Layout.vue'
import StudentHome from '../views/StudentHome.vue'
import TeacherHome from '../views/TeacherHome.vue'
import AdminHome from '../views/AdminHome.vue'
import StudentList from '../views/admin/StudentList.vue'
import AddStudent from '../views/admin/AddStudent.vue'
import EditStudent from '../views/admin/EditStudent.vue'
import StudentSearch from '../views/admin/StudentSearch.vue'
import TeacherList from '../views/admin/TeacherList.vue'
import AddTeacher from '../views/admin/AddTeacher.vue'
import EditTeacher from '../views/admin/EditTeacher.vue'
import TeacherSearch from '../views/admin/TeacherSearch.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guest: true }
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/login' },
      { path: 'student', name: 'StudentHome', component: StudentHome, meta: { role: '学生' } },
      { path: 'teacher', name: 'TeacherHome', component: TeacherHome, meta: { role: '老师' } },
      { path: 'admin', name: 'AdminHome', component: AdminHome, meta: { role: '管理员' } },
      { path: 'admin/students', name: 'StudentList', component: StudentList, meta: { role: '管理员' } },
      { path: 'admin/students/add', name: 'AddStudent', component: AddStudent, meta: { role: '管理员' } },
      { path: 'admin/students/edit/:id', name: 'EditStudent', component: EditStudent, meta: { role: '管理员' } },
      { path: 'admin/students/search', name: 'StudentSearch', component: StudentSearch, meta: { role: '管理员' } },
      { path: 'admin/teachers', name: 'TeacherList', component: TeacherList, meta: { role: '管理员' } },
      { path: 'admin/teachers/add', name: 'AddTeacher', component: AddTeacher, meta: { role: '管理员' } },
      { path: 'admin/teachers/edit/:id', name: 'EditTeacher', component: EditTeacher, meta: { role: '管理员' } },
      { path: 'admin/teachers/search', name: 'TeacherSearch', component: TeacherSearch, meta: { role: '管理员' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const roleHomeMap = { '学生': '/student', '老师': '/teacher', '管理员': '/admin' }

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.matched.some(r => r.meta.requiresAuth)) {
    if (!auth.token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else if (to.meta.role && to.meta.role !== auth.role) {
      next(roleHomeMap[auth.role] || '/login')
    } else {
      next()
    }
  } else if (to.matched.some(r => r.meta.guest)) {
    if (auth.token) {
      next(roleHomeMap[auth.role] || '/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
export { roleHomeMap }
