/**
 * Jinynet 通用 API 响应结构
 * 与后端 starter-common 的 `Result<T>` 对应
 */
export interface ApiResult<T = unknown> {
  /** 状态码 */
  code: number
  /** 提示信息 */
  msg: string
  /** 数据 */
  data: T
}

/**
 * 分页请求参数
 * 与后端 starter-common 的 `PageRequest` 对应
 */
export interface PageRequest {
  /** 当前页码（从1开始） */
  pageIndex?: number
  /** 每页条数（5-1000） */
  pageSize?: number
  /** 排序字段 */
  orderBy?: string
}

/**
 * Jimmer 分页对象
 * 对应后端 org.babyfish.jimmer.Page<T> 的 Jackson 序列化结果
 *
 * JSON 结构：
 * {
 *   "rows": [...],           // 当前页数据
 *   "totalRowCount": 100,    // 总记录数
 *   "totalPageCount": 10     // 总页数
 * }
 */
export interface JimmerPage<T> {
  /** 当前页数据列表 */
  rows: T[]
  /** 总记录数 */
  totalRowCount: number
  /** 总页数 */
  totalPageCount: number
}

/**
 * 分页响应 = Result<Page<T>>
 * 与后端 `Result<Page<T>>` 完全对应
 */
export type PageResult<T> = ApiResult<JimmerPage<T>>
