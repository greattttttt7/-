import { request } from './client'

export function getProgress(userId) {
  return request(`/progress/${userId}`)
}

export function saveProgress(payload) {
  return request('/progress', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
