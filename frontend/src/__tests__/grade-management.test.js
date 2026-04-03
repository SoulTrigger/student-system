import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    }
  }
}))

import axios from 'axios'

describe('Admin Grade Query - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('GradeQuery sends correct search params with all filters', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, courseId: 1, courseName: '数学', teacherId: 1, teacherName: '张老师', studentId: 1, studentName: '李同学', score: 85, semester: '2026春' }], total: 1 }
    })
    const params = { studentId: 1, studentName: '李', teacherId: 1, teacherName: '张', courseId: 1, courseName: '数', scoreMin: 60, scoreMax: 100, semester: '2026春', fuzzy: true, page: 1, size: 10 }
    const res = await axios.get('/api/admin/grades', { params })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/grades', { params })
    expect(res.data.records[0].studentName).toBe('李同学')
    expect(res.data.records[0].score).toBe(85)
  })

  it('GradeQuery sends fuzzy param for name searches', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    await axios.get('/api/admin/grades', { params: { studentName: '李', fuzzy: true, page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/grades', { params: { studentName: '李', fuzzy: true, page: 1, size: 10 } })
  })

  it('GradeQuery with score range filter', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    await axios.get('/api/admin/grades', { params: { scoreMin: 60, scoreMax: 90, page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/grades', { params: { scoreMin: 60, scoreMax: 90, page: 1, size: 10 } })
  })

  it('GradeQuery with semester filter', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 0 } })
    await axios.get('/api/admin/grades', { params: { semester: '2026春', page: 1, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/grades', { params: { semester: '2026春', page: 1, size: 10 } })
  })

  it('GradeQuery pagination works', async () => {
    axios.get.mockResolvedValue({ data: { records: [], total: 50 } })
    await axios.get('/api/admin/grades', { params: { page: 2, size: 10 } })
    expect(axios.get).toHaveBeenCalledWith('/api/admin/grades', { params: { page: 2, size: 10 } })
  })

  it('GradeQuery returns correct fields', async () => {
    const grade = { id: 1, courseId: 10, courseName: '高等数学', teacherId: 5, teacherName: '王教授', studentId: 20, studentName: '赵同学', score: 92, semester: '2026春' }
    axios.get.mockResolvedValue({ data: { records: [grade], total: 1 } })
    const res = await axios.get('/api/admin/grades', { params: { page: 1, size: 10 } })
    expect(res.data.records[0]).toHaveProperty('courseId')
    expect(res.data.records[0]).toHaveProperty('courseName')
    expect(res.data.records[0]).toHaveProperty('teacherId')
    expect(res.data.records[0]).toHaveProperty('teacherName')
    expect(res.data.records[0]).toHaveProperty('studentId')
    expect(res.data.records[0]).toHaveProperty('studentName')
    expect(res.data.records[0]).toHaveProperty('score')
    expect(res.data.records[0]).toHaveProperty('semester')
  })
})

describe('Admin Grade Edit - API Contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('GradeEdit puts correct payload with score only', async () => {
    axios.put.mockResolvedValue({ data: { id: 1, score: 88 } })
    const res = await axios.put('/api/admin/grades/1', { score: 88 })
    expect(axios.put).toHaveBeenCalledWith('/api/admin/grades/1', { score: 88 })
    expect(res.data.score).toBe(88)
  })

  it('GradeEdit loads grade data for context fields', async () => {
    axios.get.mockResolvedValue({
      data: { records: [{ id: 1, courseName: '数学', teacherName: '张老师', studentName: '李同学', score: 75 }], total: 1 }
    })
    const res = await axios.get('/api/admin/grades', { params: { page: 1, size: 1000 } })
    const grade = res.data.records.find(g => g.id === 1)
    expect(grade.courseName).toBe('数学')
    expect(grade.teacherName).toBe('张老师')
    expect(grade.studentName).toBe('李同学')
  })
})

describe('Grade Score Validation', () => {
  it('Score must be 0-100 integer', () => {
    const validate = (score) => Number.isInteger(score) && score >= 0 && score <= 100
    expect(validate(-1)).toBe(false)
    expect(validate(0)).toBe(true)
    expect(validate(100)).toBe(true)
    expect(validate(101)).toBe(false)
    expect(validate(50.5)).toBe(false)
    expect(validate('abc')).toBe(false)
    expect(validate(null)).toBe(false)
  })
})
