import axios from './axios'

// 登录类型
export type LoginType = 'PASSWORD' | 'EMAIL'

// 密码登录
export const login = (username: string, password: string, verifyToken?: string) => {
  return axios.post('/auth/login', {
    loginType: 'PASSWORD',
    account: username,
    password,
    captchaToken: verifyToken
  })
}

// 邮箱登录
export const emailLogin = (email: string, captchaToken: string, captcha: string) => {
  return axios.post('/auth/login', {
    loginType: 'EMAIL',
    email,
    captchaToken,
    captcha
  })
}

export const logout = () => {
  return axios.post('/auth/logout')
}

export const getProfile = () => {
  return axios.get('/auth/profile')
}

export const changePassword = (username: string, oldPassword: string, newPassword: string, captchaToken?: string) => {
  return axios.post('/auth/change-password', {
    username,
    oldPassword,
    newPassword,
    captchaToken
  })
}

export const getCaptchaConfig = () => {
  return axios.get('/auth/captcha-config')
}
