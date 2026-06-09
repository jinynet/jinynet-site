import { ref, type Ref } from 'vue'
import type { PageRequest, PageResult } from './types'

/**
 * 分页加载 Composable
 *
 * 封装列表页的分页、加载状态、数据管理等通用逻辑。
 * 直接对接后端 `Result<Page<T>>` 响应格式。
 *
 * @param fetchFn 数据加载函数，接收 PageRequest，返回 Promise<PageResult<T>>
 * @param pageSize 默认每页条数
 * @returns 分页状态和方法
 *
 * @example
 * ```ts
 * import { usePageable } from 'jinynet-frontend-common'
 * import { http } from 'jinynet-frontend-common'
 *
 * interface Article { id: number; title: string }
 *
 * const { data, loading, pagination, loadPage } = usePageable<Article>(
 *   (page) => http.get('/api/articles', { params: page }),
 *   10
 * )
 * ```
 */
export function usePageable<T>(
  fetchFn: (page: Required<PageRequest>) => Promise<PageResult<T>>,
  pageSize = 10,
) {
  /** 数据列表 */
  const data: Ref<T[]> = ref([])

  /** 加载状态 */
  const loading = ref(false)

  /** 分页信息（Naive UI DataTable 兼容格式） */
  const pagination = ref({
    page: 1,
    pageSize,
    itemCount: 0,
    showSizePicker: true,
    pageSizes: [5, 10, 20, 50],
  })

  /**
   * 加载指定页数据
   */
  const loadPage = async (page: number, size?: number) => {
    loading.value = true
    const currentSize = size ?? pagination.value.pageSize
    try {
      // 请求后端，返回 ApiResult<JimmerPage<T>>（已 unwrap）
      const result = await fetchFn({ pageIndex: page, pageSize: currentSize } as Required<PageRequest>)
      // result.data 是 JimmerPage<T>
      const pageData = result.data
      data.value = pageData.rows
      pagination.value.itemCount = pageData.totalRowCount
      pagination.value.page = page
      pagination.value.pageSize = currentSize
    } finally {
      loading.value = false
    }
  }

  /**
   * 刷新当前页
   */
  const refresh = () => loadPage(pagination.value.page)

  /**
   * 重置到第一页
   */
  const reset = () => loadPage(1)

  return {
    data,
    loading,
    pagination,
    loadPage,
    refresh,
    reset,
  }
}
