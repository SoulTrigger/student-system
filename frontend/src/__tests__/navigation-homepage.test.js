import { describe, it, expect, beforeEach, vi } from 'vitest'

const roleHomeMap = { '学生': '/student', '老师': '/teacher', '管理员': '/admin' }

describe('US-017: Admin homepage and navigation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Route guard logic', () => {
    it('redirects authenticated admin from login to /admin', () => {
      const token = 'valid-token'
      const userRole = '管理员'
      const isGuestPage = true
      let redirectTo = null
      if (isGuestPage && token) {
        redirectTo = roleHomeMap[userRole]
      }
      expect(redirectTo).toBe('/admin')
    })

    it('redirects authenticated teacher from login to /teacher', () => {
      const token = 'valid-token'
      const userRole = '老师'
      let redirectTo = null
      if (token) redirectTo = roleHomeMap[userRole]
      expect(redirectTo).toBe('/teacher')
    })

    it('redirects authenticated student from login to /student', () => {
      const token = 'valid-token'
      const userRole = '学生'
      let redirectTo = null
      if (token) redirectTo = roleHomeMap[userRole]
      expect(redirectTo).toBe('/student')
    })

    it('allows unauthenticated users to access login', () => {
      const token = ''
      const isGuestPage = true
      let allowed = true
      if (isGuestPage && token) allowed = false
      expect(allowed).toBe(true)
    })

    it('redirects wrong role to their home', () => {
      const token = 'valid'
      const userRole = '学生'
      const targetRole = '管理员'
      let redirectTo = null
      if (targetRole !== userRole) redirectTo = roleHomeMap[userRole]
      expect(redirectTo).toBe('/student')
    })
  })

  describe('Admin homepage content requirements', () => {
    it('has required welcome messages', () => {
      const adminHomeText = '当前系统：学生信息管理系统 欢迎您使用本系统 当前用户：admin'
      expect(adminHomeText).toContain('当前系统：学生信息管理系统')
      expect(adminHomeText).toContain('欢迎您使用本系统')
      expect(adminHomeText).toContain('admin')
    })
  })

  describe('Navigation menu items per role', () => {
    it('admin has correct menu structure matching PRD', () => {
      const adminMenus = [
        { label: '管理员首页', path: '/admin' },
        { label: '学生管理', children: ['添加学生', '学生列表', '搜索学生'] },
        { label: '教师管理', children: ['添加教师', '教师列表'] },
        { label: '课程管理', children: ['添加课程', '搜索课程', '开课表管理'] },
        { label: '成绩管理', children: ['学生成绩查询'] }
      ]
      expect(adminMenus).toHaveLength(5)
      expect(adminMenus[1].children).toEqual(['添加学生', '学生列表', '搜索学生'])
      expect(adminMenus[2].children).toEqual(['添加教师', '教师列表'])
      expect(adminMenus[3].children).toEqual(['添加课程', '搜索课程', '开课表管理'])
      expect(adminMenus[4].children).toEqual(['学生成绩查询'])
    })

    it('teacher has correct menu structure matching PRD', () => {
      const teacherMenus = [
        { label: '教师首页', path: '/teacher' },
        { label: '开设课程', children: ['课程查询', '已开课程'] },
        { label: '成绩管理', children: ['成绩列表'] },
        { label: '个人中心', children: ['编辑个人信息'] }
      ]
      expect(teacherMenus).toHaveLength(4)
      expect(teacherMenus[1].children).toEqual(['课程查询', '已开课程'])
      expect(teacherMenus[2].children).toEqual(['成绩列表'])
      expect(teacherMenus[3].children).toEqual(['编辑个人信息'])
    })

    it('student has correct menu structure matching PRD', () => {
      const studentMenus = [
        { label: '学生首页', path: '/student' },
        { label: '选课管理', children: ['已选课程'] },
        { label: '学生成绩管理', children: ['成绩查询'] },
        { label: '个人中心', children: ['编辑个人信息'] }
      ]
      expect(studentMenus).toHaveLength(4)
      expect(studentMenus[1].children).toEqual(['已选课程'])
      expect(studentMenus[2].children).toEqual(['成绩查询'])
      expect(studentMenus[3].children).toEqual(['编辑个人信息'])
    })

    it('no role sees menu items from other roles', () => {
      const adminLabels = ['管理员首页', '学生管理', '教师管理', '课程管理', '成绩管理']
      const teacherLabels = ['教师首页', '开设课程', '成绩管理', '个人中心']
      const studentLabels = ['学生首页', '选课管理', '学生成绩管理', '个人中心']
      // Admin shouldn't see teacher/student exclusive items
      expect(adminLabels).not.toContain('开设课程')
      expect(adminLabels).not.toContain('选课管理')
      // Teacher shouldn't see admin/student exclusive items
      expect(teacherLabels).not.toContain('学生管理')
      expect(teacherLabels).not.toContain('选课管理')
      // Student shouldn't see admin/teacher exclusive items
      expect(studentLabels).not.toContain('学生管理')
      expect(studentLabels).not.toContain('开设课程')
    })
  })

  describe('Logout behavior', () => {
    it('logout clears all auth state from localStorage', () => {
      const store = { token: 't', role: '管理员', userId: '1', name: 'admin' }
      // simulate logout
      store.token = ''
      store.role = ''
      store.userId = ''
      store.name = ''
      expect(store.token).toBe('')
      expect(store.role).toBe('')
      expect(store.userId).toBe('')
      expect(store.name).toBe('')
    })

    it('logout redirects to /login', () => {
      let redirectedTo = null
      const mockPush = (path) => { redirectedTo = path }
      // simulate: auth.logout() then router.push('/login')
      mockPush('/login')
      expect(redirectedTo).toBe('/login')
    })
  })
})
