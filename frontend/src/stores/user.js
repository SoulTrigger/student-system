import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userId = ref(localStorage.getItem('userId') || '')

  function setLogin(data) {
    token.value = data.token
    role.value = data.role
    userId.value = String(data.userId)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', String(data.userId))
  }

  function logout() {
    token.value = ''
    role.value = ''
    userId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
  }

  return { token, role, userId, setLogin, logout }
})
