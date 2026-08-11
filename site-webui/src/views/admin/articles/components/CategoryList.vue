<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="openCategoryModal">
          <Plus class="w-4 h-4 mr-2" />
          添加分类
        </n-button>
        <n-button @click="fetchList" circle size="small" title="刷新">
          <Refresh class="w-4 h-4" />
        </n-button>
      </div>
      <n-button text size="small" :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'" @click="toggleViewMode">
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" />
        <Menu class="w-4 h-4" v-else />
      </n-button>
    </div>

    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="categoryColumns"
        :data="categories"
        :bordered="true"
        :loading="loading"
        :scroll-x="600"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无分类数据</p>
            <n-button type="primary" size="small" @click="openCategoryModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个分类
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="loading">
        <div v-if="categories.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无分类数据</p>
          <n-button type="primary" size="small" @click="openCategoryModal" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加一个分类
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="category in categories"
            :key="category.id"
            size="small"
            class="hover:shadow-md transition-shadow"
          >
            <h4 class="font-semibold text-sm mb-1">{{ category.name }}</h4>
            <p class="text-xs text-gray-400 mb-1">别名: {{ category.slug }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ category.description || '暂无描述' }}</p>
            <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
              <span>排序: {{ category.sortOrder }}</span>
            </div>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="openEdit(category)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="remove(category)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      :style="{ width: '500px' }"
    >
      <n-form :model="categoryForm" label-placement="top" class="space-y-4">
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="categoryForm.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="分类别名" path="slug">
          <n-input v-model:value="categoryForm.slug" placeholder="用于URL的别名" />
        </n-form-item>
        <n-form-item label="分类描述" path="description">
          <n-input v-model:value="categoryForm.description" type="textarea" placeholder="请输入分类描述" :rows="2" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="categoryForm.sortOrder" :min="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="saveCategory">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除分类「{{ deletingItemName() }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteModal = false">取消</n-button>
          <n-button type="error" @click="confirmDelete">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, watch } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NInputNumber, NSpin, useMessage } from 'naive-ui'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/articles'
import type { ArticleCategory } from '@/types'
import { useViewMode } from '@/composables/useViewMode'
import { useCrudList } from '@/composables/useCrudList'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-article-categories-view')

/** 创建分类时的表单数据类型（name 必填，其余可选） */
type CategoryCreateData = { name: string; slug?: string; description?: string; sortOrder?: number }

const categoryForm = ref({
  name: '',
  slug: '',
  description: '',
  sortOrder: 0
})

const {
  items: categories,
  loading,
  showModal,
  editingItem: editingCategory,
  deleteModal,
  fetchList,
  openCreate,
  openEdit,
  save,
  remove,
  confirmDelete,
  deletingItemName,
} = useCrudList<ArticleCategory, CategoryCreateData>({
  listFn: getCategories,
  createFn: createCategory,
  updateFn: (id, data) => updateCategory(id as number, data),
  deleteFn: (id) => deleteCategory(id as number),
  getName: (c) => c.name,
  itemName: '分类',
  message,
})

const categoryColumns = [
  { title: '分类名称', key: 'name', minWidth: 150 },
  { title: '别名', key: 'slug', minWidth: 120 },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: ArticleCategory) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => openEdit(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => remove(row) }, () => '删除')
      ])
    }
  }
]

/**
 * useCrudList 的 openEdit / openCreate 只管理 editingItem / showModal，
 * 表单数据（name/slug/description/sortOrder）由调用方维护，
 * 因此通过 watch editingCategory 在新建/编辑切换时同步表单值。
 */
watch(editingCategory, (val, _oldVal) => {
  if (val) {
    categoryForm.value = {
      name: val.name,
      slug: val.slug,
      description: val.description ?? '',
      sortOrder: val.sortOrder ?? 0
    }
  } else {
    categoryForm.value = { name: '', slug: '', description: '', sortOrder: 0 }
  }
})

const openCategoryModal = () => {
  openCreate()
}

const saveCategory = async () => {
  if (!categoryForm.value.name.trim()) {
    message.error('请输入分类名称')
    return
  }
  const ok = await save(categoryForm.value, () => {
    showModal.value = false
    categoryForm.value = { name: '', slug: '', description: '', sortOrder: 0 }
  })
  if (!ok && editingCategory.value) {
    // 编辑失败时同步本地表单（保持原逻辑）
    categoryForm.value = {
      name: editingCategory.value.name,
      slug: editingCategory.value.slug,
      description: editingCategory.value.description || '',
      sortOrder: editingCategory.value.sortOrder
    }
  }
}

onMounted(() => {
  fetchList()
})
</script>
