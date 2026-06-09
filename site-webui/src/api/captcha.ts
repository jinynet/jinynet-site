import axios from './axios'
import type { ApiResult } from '@jinynet/webui-comm'

export interface CaptchaVO {
  id: string
  background: string
  slider: string
  x: number
  y: number
  sliderWidth?: number
  sliderHeight?: number
}

export const getCaptcha = () => {
  return axios.get<ApiResult<CaptchaVO>>('/auth/captcha')
}

export const verifyCaptcha = (token: string, sliderPosition: number, verifyData?: Record<string, any>) => {
  return axios.post<ApiResult<string>>('/auth/captcha/verify', verifyData, {
    params: { token, sliderPosition }
  })
}

/**
 * 发送邮箱验证码（需先通过滑块验证）
 */
export const sendEmailCaptcha = (email: string, captchaToken: string, captcha: string) => {
  return axios.post<ApiResult<string>>('/auth/captcha/email', {
    email,
    captchaToken,
    captcha
  })
}

/**
 * 验证邮箱验证码
 */
export const verifyEmailCaptcha = (captchaId: string, code: string) => {
  return axios.post<ApiResult<void>>('/auth/captcha/email/verify', null, {
    params: { captchaId, code }
  })
}
