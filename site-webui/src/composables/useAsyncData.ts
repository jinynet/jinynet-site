import { ref, shallowRef, type Ref } from 'vue'

/**
 * 通用异步数据加载 composable
 *
 * 统一管理 loading / error / data 状态，消除各组件重复的 try/catch/finally 模板代码。
 *
 * @example
 * const { data, loading, error, execute } = useAsyncData(() => getArticles(params))
 */
export function useAsyncData<T>(
  fn: () => Promise<T>,
  options: { immediate?: boolean; initialData?: T } = {}
) {
  const { immediate = false, initialData } = options

  const data: Ref<T | null> = shallowRef(initialData ?? null) as Ref<T | null>
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const execute = async (): Promise<T | null> => {
    loading.value = true
    error.value = null
    try {
      const result = await fn()
      data.value = result as T
      return result
    } catch (e) {
      error.value = e instanceof Error ? e : new Error(String(e))
      return null
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute()
  }

  return { data, loading, error, execute }
}
