import { request } from './client'

export function getLabs() {
  return request('/labs')
}

export function getLabDetail(labId) {
  return request(`/labs/${labId}`)
}

export function getLabDocument(labId) {
  return request(`/documents/lab/${labId}`)
}
