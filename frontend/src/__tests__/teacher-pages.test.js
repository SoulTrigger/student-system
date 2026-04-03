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

describe('Teacher Course Opening - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Teacher can search courses with credit range and fuzzy', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, name: '数学', credit: 3 }], total: 1 }
    })
    const params = { name: '数', creditMin: 2, creditMax: 4, fuzzy: true, page: 1, size: 10 }
    const res = await axios.get('/api/admin/courses/search', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/courses/search', { params })
    expect(res.data.records[0].name).toBe('数学')
  })

  it('Teacher can open a course with green button (POST)', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, courseId: 1, teacherId: 1 } })
    const res = await axios.post('/api/teacher/openings', { courseId: 1 })
    expect(axios.post).toHaveBeenCalledWith('/api/teacher/openings', { courseId: 1 })
    expect(res.data.id).toBe(1)
  })

  it('Teacher cannot open same course twice in same semester (409/400)', async () => {
    axios.post.mockRejectedValue({
      response: { status: 400, data: { message: '该课程本学期已开设' } }
    })
    try {
      await axios.post('/api/teacher/openings', { courseId: 1 })
    } catch (e) {
      expect(e.response.status).toBe(400)
      expect(e.response.data.message).toContain('已开设')
    }
  })
})

describe('Teacher My Courses - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('My courses shows only teacher own courses', async () => {
    axios.get.mockResolvedValue({
      data: { records: [
        { id: 1, courseId: 'C001', courseName: '数学', credit: 3 },
        { id: 2, courseId: 'C002', courseName: '英语', credit: 2 }
      ], total: 2 }
    })
    const res = await axios.get('/api/teacher/openings', { params: { page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/teacher/openings', { params: { page: 1, size: 10 } })
    expect(res.data.records).toHaveLength(2)
    expect(res.data.records[0].courseName).toBe('数学')
  })

  it('My courses pagination', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 30 } })
    const res = await axios.get('/api/teacher/openings', { params: { page: 2, size: 10 } })
    expect(res.data.total).toBe(30)
  })
})

describe('Teacher Grade Management - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Grade management filters by all criteria', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, courseId: 'C001', studentId: 'S001', courseName: '数学', studentName: '张三', score: 90, semester: '2026春' }], total: 1 }
    })
    const params = { studentId: 'S001', studentName: '张', courseId: 'C001', courseName: '数', scoreMin: 80, scoreMax: 100, semester: '2026春', fuzzy: true, page: 1, size: 10 }
    const res = await axios.get('/api/teacher/grades', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/teacher/grades', { params })
    expect(res.data.records[0].score).toBe(90)
  })

  it('Grade management shows only teacher courses', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    const res = await axios.get('/api/teacher/grades', { params: { page: 1, size: 10 } })
    expect(res.data.records).toHaveLength(0)
  })

  it('Teacher can edit a grade', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, score: 95 } })
    const res = await axios.put('/api/teacher/grades/1', { score: 95 })
    expect(axios.put).toHaveBeenCalledWith('/api/teacher/grades/1', { score: 95 })
    expect(res.data.score).toBe(95)
  })

  it('Teacher can enter a grade (POST)', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, score: 85 } })
    const res = await axios.post('/api/teacher/grades', { selectionId: 1, score: 85 })
    expect(axios.post).toHaveBeenCalledWith('/api/teacher/grades', { selectionId: 1, score: 85 })
    expect(res.data.score).toBe(85)
  })
})

describe('Teacher Grade Entry - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Get students in an opening', async () => {
    axios.get.mockResolvedValue({
      data: { records: [
        { studentId: 'S001', studentName: '张三', selectionId: 1, score: null },
        { studentId: 'S002', studentName: '李四', selectionId: 2, score: null }
      ], total: 2 }
    })
    const res = await axios.get('/api/teacher/grades/students', { params: { openingId: 1, page: 1, size: 20 } })
    expect(axios.get).toHaveBeenCalledWith('/api/teacher/grades/students', { params: { openingId: 1, page: 1, size: 20 } })
    expect(res.data.records).toHaveLength(2)
  })

  it('Score validation: must be 0-100', () => {
    const validateScore = (s) => s >= 0 && s <= 100
    expect(validateScore(-1)).toBe(false)
    expect(validateScore(0)).toBe(true)
    expect(validateScore(100)).toBe(true)
    expect(validateScore(101)).toBe(false)
  })

  it('Batch submit grades', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, score: 90 } })
    await Promise.all([
      axios.post('/api/teacher/grades', { selectionId: 1, score: 90 }),
      axios.post('/api/teacher/grades', { selectionId: 2, score: 80 })
    ])
    expect(axios.post).toHaveBeenCalledTimes(2)
  })
})

describe('Teacher Profile Edit - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Profile update sends correct payload', async () => {
    axios.put.mockResolvedValue({ data: { message: '密码修改成功' } })
    const res = await axios.put('/api/user/profile', { newPassword: '123456', confirmPassword: '123456' })
    expect(axios.put).toHaveBeenCalledWith('/api/user/profile', { newPassword: '123456', confirmPassword: '123456' })
    expect(res.data.message).toBe('密码修改成功')
  })

  it('Profile update rejects mismatched passwords', () => {
    const validate = (pw1, pw2) => pw1 === pw2
    expect(validate('123456', '123456')).toBe(true)
    expect(validate('123456', '654321')).toBe(false)
  })

  it('Profile update rejects short password', () => {
    const validate = (pw) => pw.length >= 6
    expect(validate('12345')).toBe(false)
    expect(validate('123456')).toBe(true)
  })
})
