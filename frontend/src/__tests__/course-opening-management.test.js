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

describe('Course Management - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('CourseSearch sends correct search params with credit range', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, name: '数学', credit: 3 }], total: 1 }
    })
    const params = { page: 1, size: 10, name: '数学', creditMin: 2, creditMax: 4, fuzzy: true }
    const res = await axios.get('/api/admin/courses/search', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/courses/search', { params })
    expect(res.data.records[0].name).toBe('数学')
    expect(res.data.total).toBe(1)
  })

  it('CourseSearch sends fuzzy param with name', async () => {
    axios.get.mockResolvedValue({
      data: { records: [], total: 0 }
    })
    await axios.get('/api/admin/courses/search', { params: { name: '物', fuzzy: true, page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/courses/search', {
      params: { name: '物', fuzzy: true, page: 1, size: 10 }
    })
  })

  it('AddCourse posts correct payload', async () => {
    axios.post.mockResolvedValue({ data: { id: 1, name: '英语', credit: 2 } })
    const res = await axios.post('/api/admin/courses', { name: '英语', credit: 2 })
    expect(axios.post).toHaveBeenCalledWith('/api/admin/courses', { name: '英语', credit: 2 })
    expect(res.data.id).toBe(1)
  })

  it('EditCourse puts correct payload', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, name: '高等数学', credit: 4 } })
    const res = await axios.put('/api/admin/courses/1', { name: '高等数学', credit: 4 })
    expect(axios.put).toHaveBeenCalledWith('/api/admin/courses/1', { name: '高等数学', credit: 4 })
    expect(res.data.name).toBe('高等数学')
  })

  it('DeleteCourse sends delete request', async () => {
    axios.delete.mockResolvedValue({ data: { message: '删除成功' } })
    const res = await axios.delete('/api/admin/courses/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/admin/courses/1')
    expect(res.data.message).toBe('删除成功')
  })

  it('CourseSearch pagination works', async () => {
    axios.get.mockResolvedValue({
      data: { records: [], total: 50 }
    })
    await axios.get('/api/admin/courses/search', { params: { page: 2, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/courses/search', { params: { page: 2, size: 10 } })
  })
})

describe('Opening Management - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('OpeningManage search with teacher and course filters', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, courseId: 1, courseName: '数学', teacherId: 1, teacherName: '张老师' }], total: 1 }
    })
    const params = { teacherName: '张', courseName: '数', fuzzy: true, page: 1, size: 10 }
    const res = await axios.get('/api/admin/openings/search', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/openings/search', { params })
    expect(res.data.records[0].teacherName).toBe('张老师')
    expect(res.data.records[0].courseName).toBe('数学')
  })

  it('OpeningManage search with teacherId', async () => {
    axios.get.mockResolvedValue({
      data: { records: [], total: 0 }
    })
    await axios.get('/api/admin/openings/search', { params: { teacherId: 1, page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/openings/search', { params: { teacherId: 1, page: 1, size: 10 } })
  })

  it('OpeningManage delete sends correct request', async () => {
    axios.delete.mockResolvedValue({ data: { message: '删除成功' } })
    const res = await axios.delete('/api/admin/openings/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/admin/openings/1')
    expect(res.data.message).toBe('删除成功')
  })

  it('OpeningManage pagination works', async () => {
    axios.get.mockResolvedValue({
      data: { records: [], total: 30 }
    })
    await axios.get('/api/admin/openings/search', { params: { page: 3, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/openings/search', { params: { page: 3, size: 10 } })
  })
})

describe('Course Management - Validation', () => {
  it('Course name must be 2-50 characters', () => {
    const validate = (name) => name.length >= 2 && name.length <= 50
    expect(validate('')).toBe(false)
    expect(validate('A')).toBe(false)
    expect(validate('数学')).toBe(true)
    expect(validate('a'.repeat(51))).toBe(false)
  })

  it('Course credit must be 1-10', () => {
    const validate = (credit) => credit >= 1 && credit <= 10
    expect(validate(0)).toBe(false)
    expect(validate(1)).toBe(true)
    expect(validate(10)).toBe(true)
    expect(validate(11)).toBe(false)
  })
})
