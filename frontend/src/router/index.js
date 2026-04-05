import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/admin',
    component: () => import('../views/Layout.vue'),
    meta: { role: 'admin' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'students', name: 'AdminStudents', component: () => import('../views/admin/Students.vue') },
      { path: 'teachers', name: 'AdminTeachers', component: () => import('../views/admin/Teachers.vue') },
      { path: 'courses', name: 'AdminCourses', component: () => import('../views/admin/Courses.vue') },
      { path: 'openings', name: 'AdminOpenings', component: () => import('../views/admin/Openings.vue') },
      { path: 'grades', name: 'AdminGrades', component: () => import('../views/admin/Grades.vue') },
      { path: 'logs', name: 'AdminLogs', component: () => import('../views/admin/Logs.vue') },
    ]
  },
  {
    path: '/teacher',
    component: () => import('../views/Layout.vue'),
    meta: { role: 'teacher' },
    children: [
      { path: '', redirect: '/teacher/dashboard' },
      { path: 'dashboard', name: 'TeacherDashboard', component: () => import('../views/teacher/Dashboard.vue') },
      { path: 'openings', name: 'TeacherOpenings', component: () => import('../views/teacher/Openings.vue') },
      { path: 'grades', name: 'TeacherGrades', component: () => import('../views/teacher/Grades.vue') },
    ]
  },
  {
    path: '/student',
    component: () => import('../views/Layout.vue'),
    meta: { role: 'student' },
    children: [
      { path: '', redirect: '/student/dashboard' },
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('../views/student/Dashboard.vue') },
      { path: 'selections', name: 'StudentSelections', component: () => import('../views/student/Selections.vue') },
      { path: 'grades', name: 'StudentGrades', component: () => import('../views/student/Grades.vue') },
    ]
  },
  { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()
  const store = useUserStore()
  if (!store.token) return next('/login')
  const requiredRole = to.matched.find(r => r.meta?.role)?.meta?.role
  if (requiredRole && store.role !== requiredRole) {
    return next('/' + store.role)
  }
  next()
})

export default router
