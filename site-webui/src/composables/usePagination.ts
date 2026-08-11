import { ref } from 'vue'
import { normalizePageResponse, type PageResponse } from '@/types'

/**
 * 分页查询参数（由调用方维护业务参数，分页参数由此 composable 管理）
 */
export interface UsePaginationOptions {
  /** 初始页码，默认 1 */
  initialPage?: number
  /** 初始每页条数，默认 10 */
  initialPageSize?: number
  /** 立即执行首次查询，默认 false */
  immediate?: boolean
}

/**
 * 通用分页 composable
 *
 * 统一管理分页状态与查询逻辑，兼容 Jimmer Page / 传统分页 / 数组 等多种后端响应结构。
 *
 * @example
 * const { items, page, pageSize, itemCount, pageCount, loading, handlePageChange, handlePageSizeChange, refresh, reset } =
 *   usePagination<ArticleListItem>((p) => getArticles({ pageIndex: p.page, pageSize: p.pageSize, ...query.value }))
 */
export function usePagination<T>(
  fetchFn: (params: { page: number; pageSize: number }) => Promise<PageResponse<T> | T[] | unknown>,
  options: UsePaginationOptions = {}
) {
  const { initialPage = 1, initialPageSize = 10, immediate = false } = options

  const page = ref(initialPage)
  const pageSize = ref(initialPageSize)
  const itemCount = ref(0)
  const pageCount = ref(1)
  const loading = ref(false)
  const items = ref<T[]>([]) as ReturnType<typeof ref<T[]>>

  const fetch = async () => {
    loading.value = true
    try {
      const res = await fetchFn({ page: page.value, pageSize: pageSize.value })
      const normalized = normalizePageResponse<T>(res)
      items.value = normalized.list
      itemCount.value = normalized.total
      pageCount.value = normalized.pageCount
      return normalized
    } catch (e) {
      console.error('分页查询失败:', e)
      items.value = []
      itemCount.value = 0
      pageCount.value = 1
      return { list: [] as T[], total: 0, pageCount: 1 }
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (p: number) => {
    page.value = p
    return fetch()
  }

  const handlePageSizeChange = (size: number) => {
    pageSize.value = size
    page.value = 1
    return fetch()
  }

  /** 重置到第一页并查询 */
  const reset = () => {
    page.value = 1
    return fetch()
  }

  /** 不改变页码的刷新 */
  const refresh = () => fetch()

  if (immediate) {
    fetch()
  }

  return {
    items,
    page,
    pageSize,
    itemCount,
    pageCount,
    loading,
    fetch,
    refresh,
    reset,
    handlePageChange,
    handlePageSizeChange,
  }
}
