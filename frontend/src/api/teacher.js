import request from './request'

export function getTeachers(params) {
  return request.get('/teachers', { params })
}

export function addTeacher(data) {
  return request.post('/teachers', data)
}

export function updateTeacher(id, data) {
  return request.put(`/teachers/${id}`, data)
}

export function deleteTeacher(id) {
  return request.delete(`/teachers/${id}`)
}
