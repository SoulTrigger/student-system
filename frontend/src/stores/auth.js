import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userId: localStorage.getItem('userId') || '',
    name: localStorage.getItem('name') || ''
  }),
  actions: {
    async login(userId, password, role) {
      const res = await axios.post('/api/auth/login', { userId, password, role })
      const data = res.data
      this.token = data.token
      this.role = data.role
      this.userId = String(data.userId)
      this.name = data.name
      localStorage.setItem('token', this.token)
      localStorage.setItem('role', this.role)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('name', this.name)
    },
    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.name = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      localStorage.removeItem('name')
    }
  }
})

// Axios interceptor
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response && err.response.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)
