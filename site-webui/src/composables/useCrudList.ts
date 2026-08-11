import { ref, type Ref } from 'vue'

type MessageApi = {
  success: (msg: string) => void
  error: (msg: string) => void
}

export interface UseCrudListOptions<T, C = Partial<T>, U = Partial<T>> {
  /** 列表查询函数 */
  listFn: () => Promise<{ data?: T[] } | T[] | unknown>
  /** 创建函数，返回新建的条目 */
  createFn: (data: C) => Promise<{ data?: T } | T | unknown>
  /** 更新函数 */
  updateFn: (id: unknown, data: U) => Promise<unknown>
  /** 删除函数 */
  deleteFn: (id: unknown) => Promise<unknown>
  /** 从条目中获取 id */
  getId?: (item: T) => unknown
  /** 从条目中获取展示名称（用于删除确认提示） */
  getName?: (item: T) => string
  /** 实体名称（用于消息提示，如"分类"/"标签"） */
  itemName?: string
  /** 是否在创建/更新成功后自动重新加载列表，默认 false（本地更新更高效） */
  reloadAfterSave?: boolean
  /** 是否在删除成功后自动重新加载列表，默认 false */
  reloadAfterDelete?: boolean
  /** Naive UI message API（由调用方 useMessage() 注入） */
  message?: MessageApi
}

/**
 * 通用 CRUD 列表 composable
 *
 * 封装列表加载、创建/编辑模态框、删除确认模态框的通用状态与操作，
 * 消除各 admin List 组件重复的 ref 声明与 try/catch 模板。
 *
 * 表单数据（formData）由调用方自行管理，因为不同实体的表单结构差异较大。
 *
 * @example
 * const message = useMessage()
 * const {
 *   items, loading, showModal, editingItem, deleteModal, deletingItem,
 *   fetchList, openCreate, openEdit, save, confirmDelete
 * } = useCrudList({
 *   listFn: getCategories,
 *   createFn: createCategory,
 *   updateFn: (id, data) => updateCategory(id, data),
 *   deleteFn: deleteCategory,
 *   itemName: '分类',
 *   message
 * })
 */
export function useCrudList<T extends { id?: unknown }, C = Partial<T>, U = Partial<T>>(
  options: UseCrudListOptions<T, C, U>
) {
  const {
    listFn,
    createFn,
    updateFn,
    deleteFn,
    getId = (item: T) => (item as { id?: unknown }).id,
    getName = (_item: T) => '',
    itemName = '项',
    reloadAfterSave = false,
    reloadAfterDelete = false,
    message,
  } = options

  const items = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)

  // 编辑/创建模态框
  const showModal = ref(false)
  const editingItem = ref<T | null>(null)

  // 删除确认模态框
  const deleteModal = ref(false)
  const deletingItem = ref<T | null>(null)

  const notifySuccess = (msg: string) => message?.success(msg)
  const notifyError = (msg: string) => message?.error(msg)

  const fetchList = async () => {
    loading.value = true
    try {
      const res = await listFn()
      const root = (res as { data?: T[] }).data
      items.value = Array.isArray(root) ? root : Array.isArray(res) ? (res as T[]) : []
    } catch (e) {
      console.error(`获取${itemName}列表失败:`, e)
      items.value = []
    } finally {
      loading.value = false
    }
  }

  const openCreate = () => {
    editingItem.value = null
    showModal.value = true
  }

  const openEdit = (item: T) => {
    editingItem.value = item
    showModal.value = true
  }

  /**
   * 保存（创建或更新）
   * @param formData 表单数据
   * @param onSuccess 创建/更新成功后的回调（通常用于重置表单、关闭模态框）
   */
  const save = async (
    formData: C | U,
    onSuccess?: (createdItem?: T) => void
  ): Promise<boolean> => {
    try {
      if (editingItem.value) {
        const id = getId(editingItem.value)
        await updateFn(id, formData as U)
        if (reloadAfterSave) {
          await fetchList()
        } else {
          const idx = items.value.findIndex(it => getId(it) === id)
          if (idx !== -1) {
            items.value[idx] = { ...items.value[idx], ...(formData as object) } as T
          }
        }
        notifySuccess(`更新${itemName}成功`)
        onSuccess?.()
        return true
      } else {
        const res = await createFn(formData as C)
        const created = (res as { data?: T }).data
        if (reloadAfterSave) {
          await fetchList()
        } else if (created) {
          items.value.push(created)
        }
        notifySuccess(`创建${itemName}成功`)
        onSuccess?.(created)
        return true
      }
    } catch (e) {
      console.error(`保存${itemName}失败:`, e)
      notifyError(`保存${itemName}失败`)
      return false
    }
  }

  const remove = (item: T) => {
    deletingItem.value = item
    deleteModal.value = true
  }

  const confirmDelete = async (): Promise<boolean> => {
    if (!deletingItem.value) return false
    const item = deletingItem.value
    const id = getId(item)
    try {
      await deleteFn(id)
      if (reloadAfterDelete) {
        await fetchList()
      } else {
        items.value = items.value.filter(it => getId(it) !== id)
      }
      notifySuccess(`删除${itemName}成功`)
      deleteModal.value = false
      deletingItem.value = null
      return true
    } catch (e) {
      console.error(`删除${itemName}失败:`, e)
      notifyError(`删除${itemName}失败`)
      return false
    }
  }

  /** 删除确认模态框中展示的名称 */
  const deletingItemName = () => (deletingItem.value ? getName(deletingItem.value) : '')

  return {
    items,
    loading,
    showModal,
    editingItem,
    deleteModal,
    deletingItem,
    fetchList,
    openCreate,
    openEdit,
    save,
    remove,
    confirmDelete,
    deletingItemName,
  }
}
