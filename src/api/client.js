export const API_BASE_URL = '/api'

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || ''

  if (contentType.includes('application/json')) {
    return response.json()
  }

  const text = await response.text()
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

async function request(path, options = {}) {
  const { headers: customHeaders, ...restOptions } = options
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...restOptions,
    headers: {
      Accept: 'application/json',
      ...(restOptions.body ? { 'Content-Type': 'application/json' } : {}),
      ...(customHeaders || {}),
    },
  })

  const payload = await parseResponse(response)

  if (!response.ok) {
    if (typeof payload === 'string') {
      throw new Error(payload || response.statusText || '请求失败')
    }
    throw new Error(payload?.msg || payload?.message || response.statusText || '请求失败')
  }

  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code !== 200) {
      throw new Error(payload.msg || payload.message || '请求失败')
    }
    return payload.data
  }

  return payload
}

export { request }