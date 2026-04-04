import request from './request'

export function getOpenings(params) {
  return request.get('/openings', { params })
}

export function addOpening(data) {
  return request.post('/openings', data)
}

export function deleteOpening(id) {
  return request.delete(`/openings/${id}`)
}
