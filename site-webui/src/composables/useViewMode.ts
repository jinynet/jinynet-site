import { ref } from 'vue'

export type ViewMode = 'table' | 'card'

/**
 * 列表视图模式切换 composable
 * - 记忆用户偏好到 localStorage
 * - 未设置时默认表格模式
 * - 同步读取 localStorage，避免首屏闪烁
 * - 提供手动切换功能
 */
export function useViewMode(storageKey: string = 'admin-view-mode') {
  const readSaved = (): ViewMode | null => {
    try {
      const saved = localStorage.getItem(storageKey)
      return saved === 'card' || saved === 'table' ? saved : null
    } catch {
      return null
    }
  }

  const viewMode = ref<ViewMode>(readSaved() ?? 'table')

  const toggleViewMode = () => {
    viewMode.value = viewMode.value === 'table' ? 'card' : 'table'
    try {
      localStorage.setItem(storageKey, viewMode.value)
    } catch {
      // 忽略隐私模式写入失败
    }
  }

  const setViewMode = (mode: ViewMode) => {
    viewMode.value = mode
    try {
      localStorage.setItem(storageKey, mode)
    } catch {
      // 忽略隐私模式写入失败
    }
  }

  return {
    viewMode,
    toggleViewMode,
    setViewMode
  }
}
