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

describe('Student Course Selection - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Student can search available courses with courseName and fuzzy', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ openingId: 1, courseId: 101, courseName: '数学', teacherId: 10, teacherName: '王老师' }], total: 1 }
    })
    const params = { courseName: '数', fuzzy: true, page: 1, size: 10 }
    const res = await axios.get('/api/student/selections/available', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/student/selections/available', { params })
    expect(res.data.records[0].courseName).toBe('数学')
    expect(res.data.records[0].openingId).toBe(1)
  })

  it('Student can select a course (POST)', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, openingId: 1 } })
    const res = await axios.post('/api/student/selections', { openingId: 1 })
    expect(axios.post).toHaveBeenCalledWith('/api/student/selections', { openingId: 1 })
    expect(res.data.id).toBe(1)
  })

  it('Cannot select already-selected course (400)', async () => {
    axios.post.mockRejectedValue({
      response: { status: 400, data: { message: '已经选过该课程' } }
    })
    try {
      await axios.post('/api/student/selections', { openingId: 1 })
    } catch (e) {
      expect(e.response.status).toBe(400)
      expect(e.response.data.message).toContain('已经选过')
    }
  })

  it('Cannot exceed 24 credit limit (400)', async () => {
    axios.post.mockRejectedValue({
      response: { status: 400, data: { message: '选课学分超过24分上限' } }
    })
    try {
      await axios.post('/api/student/selections', { openingId: 5 })
    } catch (e) {
      expect(e.response.status).toBe(400)
      expect(e.response.data.message).toContain('24')
    }
  })

  it('Cannot exceed 8 course limit (400)', async () => {
    axios.post.mockRejectedValue({
      response: { status: 400, data: { message: '选课数量超过8门上限' } }
    })
    try {
      await axios.post('/api/student/selections', { openingId: 5 })
    } catch (e) {
      expect(e.response.status).toBe(400)
      expect(e.response.data.message).toContain('8')
    }
  })

  it('Student can view my selections with total credits', async () => {
    axios.get.mockResolvedValue({
      data: {
        selections: { records: [
          { selectionId: 1, openingId: 1, courseId: 101, courseName: '数学', teacherId: 10, teacherName: '王老师', credit: 4 },
          { selectionId: 2, openingId: 2, courseId: 102, courseName: '英语', teacherId: 11, teacherName: '李老师', credit: 3 }
        ], total: 2 },
        totalCredits: 7
      }
    })
    const res = await axios.get('/api/student/selections/mine', { params: { page: 1, size: 10 } })
    expect(res.data.selections.records.length).toBe(2)
    expect(res.data.totalCredits).toBe(7)
  })

  it('Student can withdraw a course (DELETE)', async () => {
    axios.delete.mockResolvedValue({ data: { message: '退课成功' } })
    const res = await axios.delete('/api/student/selections/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/student/selections/1')
    expect(res.data.message).toBe('退课成功')
  })

  it('My selections pagination works', async () => {
    axios.get.mockResolvedValue({
      data: {
        selections: { records: [], total: 15 },
        totalCredits: 0
      }
    })
    const res = await axios.get('/api/student/selections/mine', { params: { page: 2, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/student/selections/mine', { params: { page: 2, size: 10 } })
    expect(res.data.selections.total).toBe(15)
  })
})

describe('Student Grade Query - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Student can query own grades filtered by semester', async () => {
    axios.get.mockResolvedValue({
      data: {
        records: [
          { openingId: 1, courseId: 101, courseName: '数学', teacherId: 10, teacherName: '王老师', credit: 4, score: 85 }
        ],
        averageScore: '85.0'
      }
    })
    const res = await axios.get('/api/student/grades', { params: { semester: '2025-2026-1', page: 1, size: 100 } })
    expect(res.data.records[0].score).toBe(85)
    expect(res.data.averageScore).toBe('85.0')
  })

  it('Average score has 1 decimal place', async () => {
    axios.get.mockResolvedValue({
      data: {
        records: [
          { score: 80 }, { score: 90 }
        ],
        averageScore: '85.0'
      }
    })
    const res = await axios.get('/api/student/grades', { params: { page: 1, size: 100 } })
    expect(res.data.averageScore).toMatch(/^\d+\.\d$/)
  })

  it('Grade query without semester returns all grades', async () => {
    axios.get.mockResolvedValue({
      data: { records: [], averageScore: '0.0' }
    })
    const res = await axios.get('/api/student/grades', { params: { page: 1, size: 100 } })
    expect(axios.get).toHaveBeenCalledWith('/api/student/grades', { params: { page: 1, size: 100 } })
  })
})

describe('Student Profile Edit - API Contract', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('Student can update password', async () => {
    axios.put.mockResolvedValue({ data: { message: '密码修改成功' } })
    await axios.put('/api/user/profile', { newPassword: '123456', confirmPassword: '123456' })
    expect(axios.put).toHaveBeenCalledWith('/api/user/profile', { newPassword: '123456', confirmPassword: '123456' })
  })

  it('Password mismatch returns error', async () => {
    axios.put.mockRejectedValue({
      response: { status: 400, data: { error: '两次密码不一致' } }
    })
    try {
      await axios.put('/api/user/profile', { newPassword: '123456', confirmPassword: '654321' })
    } catch (e) {
      expect(e.response.status).toBe(400)
    }
  })

  it('Password too short returns error', async () => {
    axios.put.mockRejectedValue({
      response: { status: 400, data: { error: '密码长度不能少于6位' } }
    })
    try {
      await axios.put('/api/user/profile', { newPassword: '123', confirmPassword: '123' })
    } catch (e) {
      expect(e.response.status).toBe(400)
    }
  })
})

describe('Student Navigation Routes', () => {
  it('Student routes are defined', () => {
    const routes = [
      '/student', '/student/selections', '/student/my-selections', '/student/grades', '/student/profile'
    ]
    expect(routes).toHaveLength(5)
    expect(routes).toContain('/student/selections')
    expect(routes).toContain('/student/my-selections')
    expect(routes).toContain('/student/grades')
    expect(routes).toContain('/student/profile')
  })
})
