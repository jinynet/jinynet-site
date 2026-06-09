import axios, { type AxiosInstance, type CreateAxiosDefaults } from 'axios'
import type { ApiResult } from './types'

/**
 * createRequest 配置选项
 */
export interface RequestOptions {
  /** Axios 原生配置（baseURL、timeout 等） */
  axiosOptions?: CreateAxiosDefaults
  /** 401 跳转的登录页路径，默认 '/login' */
  loginPath?: string
  /** 是否启用 BigInt 安全转换（Jimmer Long ID 可能超 JS 安全整数），默认 false */
  safeBigInt?: boolean
}

/**
 * 创建 Axios 实例工厂
 *
 * 自动处理：
 * - Token 注入（从 localStorage 读取）
 * - BigInt 安全解析（Jimmer Long ID → String）
 * - 业务错误码检查（code !== 200 → reject）
 * - 401 自动跳转登录页
 *
 * @example
 * ```ts
 * import { createRequest } from 'jinynet-frontend-common'
 *
 * const http = createRequest({
 *   axiosOptions: { baseURL: '/api', timeout: 15000 },
 *   safeBigInt: true,
 *   loginPath: '/admin/login'
 * })
 * ```
 */
export function createRequest(options?: RequestOptions): AxiosInstance {
  const { axiosOptions, loginPath = '/login', safeBigInt = false } = options ?? {}

  // 构建 transformResponse：BigInt 安全 + 默认 JSON 解析
  const transforms: CreateAxiosDefaults['transformResponse'] = []
  if (safeBigInt) {
    transforms.push((data: string) => {
      try {
        return JSON.parse(data, (_key: string, value: unknown) =>
          typeof value === 'number' && Number.isInteger(value) && !Number.isSafeInteger(value)
            ? (BigInt(value) as unknown as number).toString()
            : value,
        )
      } catch {
        return data
      }
    })
  }

  const instance = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
    ...axiosOptions,
    transformResponse: transforms.length > 0 ? transforms : undefined,
  })

  // ========== 请求拦截器：自动注入 Token ==========
  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => Promise.reject(error),
  )

  // ========== 响应拦截器：业务错误码 + 401 处理 ==========
  instance.interceptors.response.use(
    (response) => {
      const data = response.data as ApiResult
      if (data && data.code !== undefined && data.code !== 200) {
        const error = new Error(data.msg || '请求失败') as Error & {
          code: number
          response: typeof response
        }
        error.code = data.code
        error.response = response
        return Promise.reject(error)
      }
      // 成功：返回 unwrap 后的 data（直接拿到 ApiResult<T> / PageResult<T>）
      // Axios 类型签名要求 AxiosResponse，但 unwrap 后实际返回 ApiResult
      // 这是有意的设计选择：调用方少一层 .data，组合函数更简洁
      return data as any
    },
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')

        const currentPath = window.location.pathname
        if (!currentPath.includes(loginPath)) {
          setTimeout(() => {
            window.location.href = loginPath
          }, 500)
        }
      }
      return Promise.reject(error)
    },
  )

  return instance
}

/**
 * 默认 Axios 实例（开箱即用，无 BigInt 转换）
 */
export const http = createRequest()
