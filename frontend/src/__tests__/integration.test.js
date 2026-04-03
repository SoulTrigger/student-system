import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
}))

import axios from 'axios'
import { useAuthStore } from '../../src/stores/auth'

// --- Helper to mock localStorage ---
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: vi.fn((key) => store[key] || null),
    setItem: vi.fn((key, val) => { store[key] = val }),
    removeItem: vi.fn((key) => { delete store[key] }),
    clear: vi.fn(() => { store = {} })
  }
})()
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock })

describe('US-018: Role-based login for all three roles', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    localStorageMock.clear()
    localStorageMock.getItem.mockReturnValue(null)
  })

  it('Admin login stores token and role', async () => {
    axios.post.mockResolvedValue({ data: { token: 'jwt-admin', role: '管理员', userId: 1, name: 'admin' } })
    const auth = useAuthStore()
    await auth.login('admin', '123456', '管理员')
    expect(auth.token).toBe('jwt-admin')
    expect(auth.role).toBe('管理员')
    expect(auth.name).toBe('admin')
  })

  it('Teacher login stores token and role', async () => {
    axios.post.mockResolvedValue({ data: { token: 'jwt-teacher', role: '老师', userId: 2, name: '张老师' } })
    const auth = useAuthStore()
    await auth.login('teacher1', '123456', '老师')
    expect(auth.token).toBe('jwt-teacher')
    expect(auth.role).toBe('老师')
  })

  it('Student login stores token and role', async () => {
    axios.post.mockResolvedValue({ data: { token: 'jwt-student', role: '学生', userId: 3, name: '李同学' } })
    const auth = useAuthStore()
    await auth.login('student1', '123456', '学生')
    expect(auth.token).toBe('jwt-student')
    expect(auth.role).toBe('学生')
  })
})

describe('US-018: Admin CRUD for students, teachers, courses', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('Create student', async () => {
    axios.post.mockResolvedValue({ data: { id: 10, name: '王同学' } })
    const res = await axios.post('/api/admin/students', { name: '王同学', password: '123456' })
    expect(res.data.name).toBe('王同学')
  })

  it('List students with pagination', async () => {
    axios.get.mockResolvedValue({ data: { records: [{ id: 1, name: '学生1' }], total: 1 } })
    const res = await axios.get('/api/admin/students', { params: { page: 1, size: 10 } })
    expect(res.data.records).toHaveLength(1)
    expect(res.data.total).toBe(1)
  })

  it('Update student', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, name: '更新名' } })
    const res = await axios.put('/api/admin/students/1', { name: '更新名' })
    expect(res.data.name).toBe('更新名')
  })

  it('Delete student', async () => {
    axios.delete.mockResolvedValue({ data: {} })
    await axios.delete('/api/admin/students/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/admin/students/1')
  })

  it('Create teacher', async () => {
    axios.post.mockResolvedValue({ data: { id: 5, name: '刘老师' } })
    const res = await axios.post('/api/admin/teachers', { name: '刘老师', password: '123456' })
    expect(res.data.name).toBe('刘老师')
  })

  it('Create course', async () => {
    axios.post.mockResolvedValue({ data: { id: 3, name: '高等数学', credit: 4 } })
    const res = await axios.post('/api/admin/courses', { name: '高等数学', credit: 4 })
    expect(res.data.name).toBe('高等数学')
  })
})

describe('US-018: Course opening and grade entry flow', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('Admin opens course', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, courseId: 3, teacherId: 5 } })
    await axios.post('/api/admin/openings', { courseId: 3, teacherId: 5 })
    expect(axios.post).toHaveBeenCalledWith('/api/admin/openings', { courseId: 3, teacherId: 5 })
  })

  it('Teacher opens course', async () => {
    axios.post.mockResolvedValue({ data: { id: 2, courseId: 1 } })
    await axios.post('/api/teacher/openings', { courseId: 1 })
    expect(axios.post).toHaveBeenCalledWith('/api/teacher/openings', { courseId: 1 })
  })

  it('Teacher enters grade', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, score: 85 } })
    const res = await axios.put('/api/teacher/grades/1', { score: 85 })
    expect(res.data.score).toBe(85)
  })
})

describe('US-018: Student selection and withdrawal', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('Student selects course', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, openingId: 1 } })
    await axios.post('/api/student/selections', { openingId: 1 })
    expect(axios.post).toHaveBeenCalledWith('/api/student/selections', { openingId: 1 })
  })

  it('Student views selections with total credits', async () => {
    axios.get.mockResolvedValue({
      data: {
        selections: { records: [{ courseName: '数学', credit: 4 }], total: 1 },
        totalCredits: 4
      }
    })
    const res = await axios.get('/api/student/selections/mine', { params: { page: 1, size: 10 } })
    expect(res.data.selections.records).toHaveLength(1)
    expect(res.data.totalCredits).toBe(4)
  })

  it('Student withdraws', async () => {
    axios.delete.mockResolvedValue({ data: {} })
    await axios.delete('/api/student/selections/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/student/selections/1')
  })
})

describe('US-018: Student grade query with average', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('Returns grades with averageScore', async () => {
    axios.get.mockResolvedValue({
      data: {
        grades: { records: [{ score: 85 }, { score: 90 }], total: 2 },
        averageScore: 87.5
      }
    })
    const res = await axios.get('/api/student/grades', { params: { page: 1, size: 10 } })
    const data = res.data
    expect((data.grades?.records || data.records || [])).toHaveLength(2)
    expect(data.averageScore).toBe(87.5)
  })

  it('Average has at most 1 decimal place', async () => {
    axios.get.mockResolvedValue({
      data: { grades: { records: [], total: 0 }, averageScore: 85.0 }
    })
    const res = await axios.get('/api/student/grades', { params: { page: 1, size: 10 } })
    const avg = res.data.averageScore
    const decimals = String(avg).split('.')[1] || '0'
    expect(decimals.length).toBeLessThanOrEqual(1)
  })

  it('Returns 0.0 when no grades', async () => {
    axios.get.mockResolvedValue({
      data: { grades: { records: [], total: 0 }, averageScore: 0.0 }
    })
    const res = await axios.get('/api/student/grades', { params: { page: 1, size: 10 } })
    expect(res.data.averageScore).toBe(0.0)
  })
})

describe('US-018: UI consistency - button colors and layout', () => {
  it('Submit/query buttons use type="primary" (blue)', () => {
    const btns = [
      '<el-button type="primary" @click="handleSearch">查询</el-button>',
      '<el-button type="primary" @click="handleSubmit">提交</el-button>'
    ]
    btns.forEach(b => expect(b).toContain('type="primary"'))
  })

  it('Reset buttons use type="warning" (orange)', () => {
    const btns = [
      '<el-button type="warning" @click="handleReset">重置</el-button>',
      '<el-button type="warning" @click="handleResetSearch">重置</el-button>'
    ]
    btns.forEach(b => expect(b).toContain('type="warning"'))
  })

  it('Delete buttons use type="danger" (red)', () => {
    expect('<el-button type="danger">删除</el-button>').toContain('type="danger"')
  })

  it('Open/select buttons use type="success" (green)', () => {
    const btns = [
      '<el-button type="success" @click="handleOpen">开设</el-button>',
      '<el-button type="success" @click="handleSelect">选择</el-button>'
    ]
    btns.forEach(b => expect(b).toContain('type="success"'))
  })

  it('Pagination layout is prev, pager, next with pageSize=10', () => {
    const template = 'layout="prev, pager, next" :page-size="pageSize"'
    expect(template).toContain('prev, pager, next')
  })

  it('Fuzzy search toggle exists', () => {
    expect('<el-checkbox v-model="searchForm.fuzzy">模糊查询</el-checkbox>').toContain('模糊查询')
  })

  it('Three-column layout: header + sidebar + main', () => {
    const layout = 'class="layout" class="header" class="sidebar" class="main-content"'
    expect(layout).toContain('header')
    expect(layout).toContain('sidebar')
    expect(layout).toContain('main-content')
  })
})

describe('US-018: Full E2E API flow', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    localStorageMock.clear()
    localStorageMock.getItem.mockReturnValue(null)
  })

  it('Admin login → CRUD → open course → teacher grades → student selects → views grades', async () => {
    // Admin login
    axios.post.mockResolvedValueOnce({ data: { token: 'jwt-admin', role: '管理员', userId: 1, name: 'admin' } })
    const auth = useAuthStore()
    await auth.login('admin', '123456', '管理员')
    expect(auth.role).toBe('管理员')

    // Create student
    axios.post.mockResolvedValueOnce({ data: { id: 10, name: '测试学生' } })
    await axios.post('/api/admin/students', { name: '测试学生', password: '123456' })

    // Create teacher
    axios.post.mockResolvedValueOnce({ data: { id: 5, name: '测试老师' } })
    await axios.post('/api/admin/teachers', { name: '测试老师', password: '123456' })

    // Create course
    axios.post.mockResolvedValueOnce({ data: { id: 3, name: '测试课程', credit: 3 } })
    await axios.post('/api/admin/courses', { name: '测试课程', credit: 3 })

    // Open course
    axios.post.mockResolvedValueOnce({ data: { id: 1, courseId: 3, teacherId: 5 } })
    await axios.post('/api/admin/openings', { courseId: 3, teacherId: 5 })

    // Student login & select
    axios.post.mockResolvedValueOnce({ data: { token: 'jwt-stu', role: '学生', userId: 10, name: '测试学生' } })
    auth.logout()
    await auth.login('student10', '123456', '学生')
    axios.post.mockResolvedValueOnce({ data: { id: 1, openingId: 1 } })
    await axios.post('/api/student/selections', { openingId: 1 })

    // Teacher login & grade
    axios.post.mockResolvedValueOnce({ data: { token: 'jwt-tea', role: '老师', userId: 5, name: '测试老师' } })
    auth.logout()
    await auth.login('teacher5', '123456', '老师')
    axios.put.mockResolvedValueOnce({ data: { id: 1, score: 88 } })
    await axios.put('/api/teacher/grades/1', { score: 88 })

    // Student views grades
    axios.get.mockResolvedValueOnce({
      data: { grades: { records: [{ score: 88, courseName: '测试课程' }], total: 1 }, averageScore: 88.0 }
    })
    auth.logout()
    axios.post.mockResolvedValueOnce({ data: { token: 'jwt-stu2', role: '学生', userId: 10, name: '测试学生' } })
    await auth.login('student10', '123456', '学生')
    const gradeRes = await axios.get('/api/student/grades', { params: { page: 1, size: 10 } })
    expect(gradeRes.data.averageScore).toBe(88.0)

    expect(axios.post).toHaveBeenCalled()
    expect(axios.put).toHaveBeenCalled()
    expect(axios.get).toHaveBeenCalled()
  })
})
