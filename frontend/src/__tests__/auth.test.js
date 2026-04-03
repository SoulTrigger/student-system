import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../stores/auth'

// Mock localStorage
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: vi.fn(key => store[key] || null),
    setItem: vi.fn((key, val) => { store[key] = val }),
    removeItem: vi.fn(key => { delete store[key] }),
    clear: vi.fn(() => { store = {} })
  }
})()
Object.defineProperty(global, 'localStorage', { value: localStorageMock })

describe('Auth Store', () => {
  beforeEach(() => {
    localStorageMock.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('initializes with empty state when no localStorage', () => {
    const auth = useAuthStore()
    expect(auth.token).toBe('')
    expect(auth.role).toBe('')
    expect(auth.userId).toBe('')
    expect(auth.name).toBe('')
  })

  it('login sets state and localStorage', async () => {
    const auth = useAuthStore()
    // Mock axios
    const mockPost = vi.fn().mockResolvedValue({
      data: { token: 'test-jwt', role: '学生', name: '张三', userId: 1001 }
    })
    const axios = await import('axios')
    axios.default.post = mockPost

    await auth.login('1001', 'password123', '学生')

    expect(auth.token).toBe('test-jwt')
    expect(auth.role).toBe('学生')
    expect(auth.name).toBe('张三')
    expect(auth.userId).toBe('1001')
    expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'test-jwt')
  })

  it('logout clears state and localStorage', () => {
    const auth = useAuthStore()
    auth.token = 'test'
    auth.role = '学生'
    auth.userId = '1'
    auth.name = 'Test'

    auth.logout()

    expect(auth.token).toBe('')
    expect(auth.role).toBe('')
    expect(localStorageMock.removeItem).toHaveBeenCalled()
  })

  it('login failure does not change state', async () => {
    const auth = useAuthStore()
    const axios = await import('axios')
    axios.default.post = vi.fn().mockRejectedValue(new Error('Login failed'))

    await expect(auth.login('wrong', 'wrong', '学生')).rejects.toThrow()
    expect(auth.token).toBe('')
  })
})
