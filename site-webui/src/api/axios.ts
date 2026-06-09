import { createRequest } from '@jinynet/webui-comm'

/**
 * PTech Axios 实例
 *
 * 基于 @jinynet/webui-comm 的 createRequest 工厂，
 * 添加 Jimmer Long ID 的 BigInt 安全转换和自定义登录路径。
 */
const instance = createRequest({
  axiosOptions: {
    baseURL: import.meta.env.VITE_API_BASE_URL || '/',
    timeout: 10000,
  },
  loginPath: '/admin/login',
  safeBigInt: true,
})

export default instance
