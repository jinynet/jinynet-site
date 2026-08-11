/**
 * 通用类型定义
 */

/**
 * Jimmer 分页响应结构
 */
export interface PageResponse<T> {
  content?: T[]
  rows?: T[]
  totalRowCount?: number | string
  totalElements?: number | string
  totalPages?: number
  totalPageCount?: number
}

/**
 * 分页查询基础参数
 */
export interface PageQuery {
  pageIndex: number
  pageSize: number
  orderBy?: string
}

/**
 * 通用 API 响应包装
 */
export interface ApiResult<T> {
  code?: number
  msg?: string
  data: T
}

/**
 * 从 PageResponse 中标准化提取列表与分页信息
 */
export function normalizePageResponse<T>(data: unknown): {
  list: T[]
  total: number
  pageCount: number
} {
  const empty = { list: [] as T[], total: 0, pageCount: 1 }
  if (!data) return empty

  // 兼容 Result 包裹未完全解包的情况
  const root = (data as { data?: unknown }).data ?? data
  if (!root) return empty

  // 数组格式
  if (Array.isArray(root)) {
    return { list: root, total: root.length, pageCount: 1 }
  }

  const obj = root as Record<string, unknown>
  const list = (obj.content as T[]) || (obj.rows as T[]) || []
  const total =
    Number(obj.totalRowCount) || Number(obj.totalElements) || 0
  const totalPages = Number(obj.totalPages) || Number(obj.totalPageCount) || 0

  return {
    list,
    total,
    pageCount: totalPages > 0 ? totalPages : 1,
  }
}
