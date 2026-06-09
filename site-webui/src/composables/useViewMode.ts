import { ref, onMounted } from 'vue'

export type ViewMode = 'table' | 'card'

/**
 * 列表视图模式切换 composable
 * - 记忆用户偏好到 localStorage
 * - 未设置时默认表格模式
 * - 提供手动切换功能
 */
export function useViewMode(storageKey: string = 'admin-view-mode') {
  const viewMode = ref<ViewMode>('card')

  const loadPreference = () => {
    const saved = localStorage.getItem(storageKey)
    if (saved === 'card' || saved === 'table') {
      viewMode.value = saved
    }
  }

  const toggleViewMode = () => {
    viewMode.value = viewMode.value === 'table' ? 'card' : 'table'
    localStorage.setItem(storageKey, viewMode.value)
  }

  const setViewMode = (mode: ViewMode) => {
    viewMode.value = mode
    localStorage.setItem(storageKey, mode)
  }

  onMounted(() => {
    loadPreference()
  })

  return {
    viewMode,
    toggleViewMode,
    setViewMode
  }
}
