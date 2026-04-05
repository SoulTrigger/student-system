import request from './request'

export function getAvailableCourses(params) {
  return request.get('/selections/available', { params })
}

export function selectCourse(data) {
  return request.post('/selections', data)
}

export function getMySelections(params) {
  return request.get('/selections/mine', { params })
}

export function dropCourse(id) {
  return request.delete(`/selections/${id}`)
}

export function getMyCredits() {
  return request.get('/selections/credits')
}
