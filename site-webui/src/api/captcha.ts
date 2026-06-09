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
