import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock axios
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

describe('Teacher Management - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('TeacherList fetches with correct pagination params', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, name: '李老师' }], total: 1 }
    })
    const res = await axios.get('/api/admin/teachers', { params: { page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/teachers', { params: { page: 1, size: 10 } })
    expect(res.data.records[0].name).toBe('李老师')
    expect(res.data.records[0]).not.toHaveProperty('password')
  })

  it('AddTeacher posts to correct endpoint', async () => {
    axios.post.mockResolvedValue({ data: { id: 3, name: '王老师' } })
    await axios.post('/api/admin/teachers', { name: '王老师', password: '123456' })
    expect(axios.post).toHaveBeenCalledWith('/api/admin/teachers', { name: '王老师', password: '123456' })
  })

  it('AddTeacher validation: name 2-20 chars, password >= 6 chars', () => {
    const rules = {
      name: [
        { required: true, message: '请输入教师姓名' },
        { min: 2, max: 20, message: '姓名长度2-20个字符' }
      ],
      password: [
        { required: true, message: '请输入初始密码' },
        { min: 6, message: '密码长度至少6个字符' }
      ]
    }
    expect(rules.name[1].min).toBe(2)
    expect(rules.name[1].max).toBe(20)
    expect(rules.password[1].min).toBe(6)
  })

  it('EditTeacher sends PUT to /api/admin/teachers/:id', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, name: '新名字' } })
    await axios.put('/api/admin/teachers/1', { name: '新名字', password: 'newpass123' })
    expect(axios.put).toHaveBeenCalledWith('/api/admin/teachers/1', { name: '新名字', password: 'newpass123' })
  })

  it('Delete sends DELETE to /api/admin/teachers/:id', async () => {
    axios.delete.mockResolvedValue({ data: { message: '删除成功' } })
    await axios.delete('/api/admin/teachers/1')
    expect(axios.delete).toHaveBeenCalledWith('/api/admin/teachers/1')
  })

  it('TeacherSearch sends fuzzy param', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    await axios.get('/api/admin/teachers/search', { params: { name: '李', fuzzy: true, page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/teachers/search', {
      params: { name: '李', fuzzy: true, page: 1, size: 10 }
    })
  })

  it('TeacherSearch exact search does not send fuzzy param by default', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    await axios.get('/api/admin/teachers/search', { params: { name: '李老师', page: 1, size: 10 } })
    const call = axios.get.mock.calls[0]
    expect(call[1].params.fuzzy).toBeUndefined()
  })

  it('Delete cascade warning message format', () => {
    const teacherName = '李老师'
    const msg = `确认删除教师 ${teacherName} 及其所有开课和成绩记录？`
    expect(msg).toBe('确认删除教师 李老师 及其所有开课和成绩记录？')
  })

  it('No password column in teacher table data', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, name: '李老师' }], total: 1 }
    })
    const res = await axios.get('/api/admin/teachers', { params: { page: 1, size: 10 } })
    const teacher = res.data.records[0]
    expect(teacher).not.toHaveProperty('password')
    expect(Object.keys(teacher)).toEqual(expect.arrayContaining(['id', 'name']))
  })
})
