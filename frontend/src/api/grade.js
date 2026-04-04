import request from './request'

export function getAllGrades(params) {
  return request.get('/grades/all', { params })
}

export function updateGradeScore(id, score) {
  return request.put(`/grades/${id}`, { score })
}
