import request from './request'

export function getAllGrades(params) {
  return request.get('/grades/all', { params })
}

export function getTeacherGrades(params) {
  return request.get('/grades/teacher', { params })
}

export function updateGradeScore(id, score) {
  return request.put(`/grades/${id}`, { score })
}

export function batchSaveGrades(grades) {
  return request.post('/grades/batch', grades)
}
