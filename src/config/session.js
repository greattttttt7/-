const STORAGE_KEY = 'oslab_current_user'

export function getCurrentUser() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function setCurrentUser(user) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
}

export function clearCurrentUser() {
  localStorage.removeItem(STORAGE_KEY)
}

export function getCurrentUserId() {
  return getCurrentUser()?.userId ?? null
}
