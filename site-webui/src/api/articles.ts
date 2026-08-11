import axios from './axios'
import type {
  ArticleListItem,
  ArticleDetail,
  ArticleCategory,
  ArticleTag,
  ArticleForm,
  ArticleQuery,
  CategoryInput,
  TagInput,
} from '@/types'

// 类型重新导出（向后兼容），实际定义见 @/types
export type {
  ArticleListItem,
  ArticleDetail,
  ArticleCategory,
  ArticleTag,
  ArticleForm,
  ArticleQuery,
  CategoryInput,
  TagInput,
}

/** @deprecated 请使用 ArticleListItem */
export type ArticleList = ArticleListItem

// 文章管理 API - 完整路径: /api/admin/articles
export const getArticles = (params: ArticleQuery) => {
  return axios.get('/admin/articles', { params })
}

export const getArticleById = (id: string | number) => {
  return axios.get(`/admin/articles/${id}`)
}

export const createArticle = (data: ArticleForm) => {
  return axios.post('/admin/articles', data)
}

export const updateArticle = (id: string | number, data: Partial<ArticleForm>) => {
  return axios.put(`/admin/articles/${id}`, data)
}

export const publishArticle = (data: ArticleForm) => {
  return axios.post('/admin/articles/publish', data)
}

export const updateAndPublishArticle = (id: string | number, data: Partial<ArticleForm>) => {
  return axios.put(`/admin/articles/${id}/publish`, data)
}

export const deleteArticle = (id: string | number) => {
  return axios.delete(`/admin/articles/${id}`)
}

// 分类管理 API - 完整路径: /api/admin/categories
export const getCategories = () => {
  return axios.get('/admin/articles/categories')
}

export const createCategory = (data: { name: string; slug?: string; description?: string; sortOrder?: number }) => {
  return axios.post('/admin/articles/categories', data)
}

export const updateCategory = (id: number, data: Partial<ArticleCategory>) => {
  return axios.put(`/admin/articles/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return axios.delete(`/admin/articles/categories/${id}`)
}

// 标签管理 API - 完整路径: /api/admin/tags
export const getTags = () => {
  return axios.get('/admin/articles/tags')
}

export const createTag = (data: { name: string; slug?: string; color?: string; description?: string; sortOrder?: number }) => {
  return axios.post('/admin/articles/tags', data)
}

export const updateTag = (id: number, data: Partial<ArticleTag>) => {
  return axios.put(`/admin/articles/tags/${id}`, data)
}

export const deleteTag = (id: number) => {
  return axios.delete(`/admin/articles/tags/${id}`)
}

// ==================== AI 帮写 API ====================
// 以下接口为 SSE 流式响应，使用原生 fetch 实现（axios 不支持流式读取）

// AI 帮写请求类型
export interface AiGenerateRequest {
  topic: string
  keywords?: string
  style?: string
}

export interface AiContinueRequest {
  content: string
  direction?: string
}

export interface AiOptimizeRequest {
  content: string
  type?: string
}

export interface AiSimpleRequest {
  content: string
}

// AI 帮写回调接口
export interface AiStreamHandler {
  onContent: (chunk: string) => void
  onComplete: (fullContent: string) => void
  onError: (error: Error) => void
}

// 通用 AI 流式请求函数
export async function aiStreamRequest(
  url: string,
  body: Record<string, unknown>,
  handler: AiStreamHandler,
  signal?: AbortSignal
): Promise<void> {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/'
  try {
    const response = await fetch(`${baseURL}${url}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
      },
      body: JSON.stringify(body),
      signal,
    })

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('无法读取响应流')
    }

    const decoder = new TextDecoder()
    let buffer = ''
    let fullContent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6).trim()
          if (data === '[DONE]') {
            handler.onComplete(fullContent)
            return
          }
          try {
            const parsed = JSON.parse(data)
            if (parsed.content) {
              fullContent += parsed.content
              handler.onContent(parsed.content)
            }
          } catch {
            // 忽略解析失败的行
          }
        }
      }
    }
    handler.onComplete(fullContent)
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') {
      handler.onComplete('')
      return
    }
    handler.onError(error instanceof Error ? error : new Error(String(error)))
  }
}

// 生成文章
export const generateArticle = (req: AiGenerateRequest, handler: AiStreamHandler, signal?: AbortSignal) =>
  aiStreamRequest('/admin/articles/ai/generate', req as unknown as Record<string, unknown>, handler, signal)

// 续写内容
export const continueArticle = (req: AiContinueRequest, handler: AiStreamHandler, signal?: AbortSignal) =>
  aiStreamRequest('/admin/articles/ai/continue', req as unknown as Record<string, unknown>, handler, signal)

// 润色优化
export const optimizeArticle = (req: AiOptimizeRequest, handler: AiStreamHandler, signal?: AbortSignal) =>
  aiStreamRequest('/admin/articles/ai/optimize', req as unknown as Record<string, unknown>, handler, signal)

// 生成摘要
export const summarizeArticle = (req: AiSimpleRequest, handler: AiStreamHandler, signal?: AbortSignal) =>
  aiStreamRequest('/admin/articles/ai/summarize', req as unknown as Record<string, unknown>, handler, signal)

// 生成标题
export const generateTitle = (req: AiSimpleRequest, handler: AiStreamHandler, signal?: AbortSignal) =>
  aiStreamRequest('/admin/articles/ai/title', req as unknown as Record<string, unknown>, handler, signal)
