import axios from './axios'

export const login = (username: string, password: string, verifyToken?: string) => {
  return axios.post('/auth/login', { 
    account: username, 
    password, 
    captchaToken: verifyToken
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