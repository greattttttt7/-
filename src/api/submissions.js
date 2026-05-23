import { request } from './client'

export function getTask(taskId) {
  return request(`/tasks/${taskId}`)
}

export function getTaskSource(taskId, userId) {
  return request(`/tasks/${taskId}/source?userId=${userId}`)
}

export function getTaskAnswer(taskId) {
  return request(`/tasks/${taskId}/answer`)
}

export function saveSubmission(payload) {
  return request('/submissions/snapshot', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function runSubmission(payload) {
  return request('/submissions/judge', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getSubmissions(userId, labId) {
  return request(`/submissions?userId=${userId}&labId=${labId}`)
}
