import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, emailLogin, logout, getProfile } from '@/api/auth'

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

  // 密码登录
  const handleLogin = async (username: string, password: string, verifyToken?: string) => {
    const response = await login(username, password, verifyToken)
    setToken(response.data)
    return response
  }

  // 邮箱登录
  const handleEmailLogin = async (email: string, captchaToken: string, captcha: string) => {
    const response = await emailLogin(email, captchaToken, captcha)
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
    handleEmailLogin,
    handleLogout,
    handleGetProfile
  }
})
