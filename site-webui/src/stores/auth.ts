import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getProfile } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<{ username: string; email: string } | null>(null)

  const isAuthenticated = () => {
    return token.value !== ''
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const removeToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const handleLogin = async (username: string, password: string, verifyToken?: string) => {
    const response = await login(username, password, verifyToken)
    console.log(response)
    // 后端返回的 data 字段本身就是 token 字符串，不是对象
    setToken(response.data)
    return response
  }

  const handleLogout = async () => {
    await logout()
    removeToken()
    user.value = null
  }

  const handleGetProfile = async () => {
    const response = await getProfile()
    user.value = response.data
    return response
  }

  return {
    token,
    user,
    isAuthenticated,
    setToken,
    removeToken,
    handleLogin,
    handleLogout,
    handleGetProfile
  }
})