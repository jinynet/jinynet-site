<template>
  <div class="space-y-4">
    <div class="flex items-center gap-3 mb-4">
      <n-button type="primary" @click="openCategoryModal">
        <Plus class="w-4 h-4 mr-2" />
        添加分类
      </n-button>
      <n-button @click="fetchCategories" circle size="small" title="刷新">
        <Refresh class="w-4 h-4" />
      </n-button>
      <div class="flex-1"></div>
      <n-button 
        text size="small"
        :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'"
        @click="toggleViewMode"
      >
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" />
        <Menu class="w-4 h-4" v-else />
      </n-button>
    </div>

    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="categoryColumns"
        :data="categories"
        :bordered="true"
        :loading="categoryLoading"
        :scroll-x="1200"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <Folder class="w-16 h-16 text-gray-300 mb-4" />
            <p class="text-gray-400">暂无分类数据</p>
            <n-button type="primary" size="small" @click="openCategoryModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加分类
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="categoryLoading">
        <div v-if="categories.length === 0" class="flex flex-col items-center justify-center py-12">
          <Folder class="w-16 h-16 text-gray-300 mb-4" />
          <p class="text-gray-400">暂无分类数据</p>
          <n-button type="primary" size="small" @click="openCategoryModal" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加分类
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
            <p class="text-xs text-gray-400 mb-1">编码: {{ category.code }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ category.description || '暂无描述' }}</p>
            <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
              <span v-if="category.icon">图标: {{ category.icon }}</span>
              <span>排序: {{ category.sortOrder }}</span>
            </div>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editCategory(category)">
                <Edit class="w-4 h-4" />
              </n-button>
              <n-button text size="small" type="error" @click.stop="deleteCategoryItem(category)">
                <Trash2 class="w-4 h-4" />
              </n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="showCategoryModal"
      preset="card"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      :style="{ width: '500px' }"
    >
      <n-form :model="categoryForm" label-placement="top" class="space-y-4">
        <n-form-item label="分类名称" path="name" required>
          <n-input v-model:value="categoryForm.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="分类编码" path="code" required>
          <n-input v-model:value="categoryForm.code" placeholder="用于系统标识的唯一编码" />
        </n-form-item>
        <n-form-item label="分类描述" path="description">
          <n-input v-model:value="categoryForm.description" type="textarea" placeholder="请输入分类描述" :rows="2" />
        </n-form-item>
        <n-form-item label="图标" path="icon">
          <n-input v-model:value="categoryForm.icon" placeholder="图标名称（如 folder）" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="categoryForm.sortOrder" :min="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCategoryModal = false">取消</n-button>
          <n-button type="primary" @click="saveCategory">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteCategoryModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除分类「{{ deletingCategoryName }}」吗？该分类下的文件将变为无分类状态。</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteCategoryModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteCategory">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Folder, Edit, Trash2, Grid, Menu } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NInputNumber, NSpin, useMessage } from 'naive-ui'
import { getCategories, createCategory, updateCategory, deleteCategory, type FileCategory } from '@/api/files'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-file-categories-view')

const categories = ref<FileCategory[]>([])
const categoryLoading = ref(false)
const showCategoryModal = ref(false)
const editingCategory = ref<FileCategory | null>(null)
const deleteCategoryModal = ref(false)
const deletingCategoryId = ref<number | null>(null)
const deletingCategoryName = ref('')

const categoryForm = ref({
  name: '',
  code: '',
  description: '',
  icon: '',
  sortOrder: 0
})

const categoryColumns = [
  { title: '分类名称', key: 'name', minWidth: 150 },
  { title: '编码', key: 'code', minWidth: 120 },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  { title: '图标', key: 'icon', width: 100, default: '-' },
  { title: '排序', key: 'sortOrder', width: 80 },
  { title: '创建时间', key: 'createdAt', width: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right' as const,
    render: (row: FileCategory) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editCategory(row) }, () => h(Edit, { class: 'w-4 h-4' })),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteCategoryItem(row) }, () => h(Trash2, { class: 'w-4 h-4' }))
      ])
    }
  }
]

const openCategoryModal = () => {
  editingCategory.value = null
  categoryForm.value = { name: '', code: '', description: '', icon: '', sortOrder: 0 }
  showCategoryModal.value = true
}

const editCategory = (category: FileCategory) => {
  editingCategory.value = category
  categoryForm.value = {
    name: category.name,
    code: category.code,
    description: category.description || '',
    icon: category.icon || '',
    sortOrder: category.sortOrder
  }
  showCategoryModal.value = true
}

const deleteCategoryItem = (category: FileCategory) => {
  deletingCategoryId.value = category.id
  deletingCategoryName.value = category.name
  deleteCategoryModal.value = true
}

const confirmDeleteCategory = async () => {
  if (deletingCategoryId.value) {
    await deleteCategory(deletingCategoryId.value)
    categories.value = categories.value.filter(c => c.id !== deletingCategoryId.value)
    message.success('删除成功')
  }
  deleteCategoryModal.value = false
}

const saveCategory = async () => {
  if (!categoryForm.value.name.trim()) {
    message.error('请输入分类名称')
    return
  }
  if (!categoryForm.value.code.trim()) {
    message.error('请输入分类编码')
    return
  }

  try {
    const data = {
      name: categoryForm.value.name,
      code: categoryForm.value.code,
      description: categoryForm.value.description || undefined,
      icon: categoryForm.value.icon || undefined,
      sortOrder: categoryForm.value.sortOrder
    }

    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, data)
      const index = categories.value.findIndex(c => c.id === editingCategory.value!.id)
      if (index !== -1) {
        categories.value[index] = { ...categories.value[index], ...categoryForm.value }
      }
      message.success('更新成功')
    } else {
      const response = await createCategory(data)
      categories.value.push(response.data)
      message.success('创建成功')
    }
    showCategoryModal.value = false
    categoryForm.value = { name: '', code: '', description: '', icon: '', sortOrder: 0 }
  } catch (error) {
    console.error('保存分类失败:', error)
    message.error('保存失败')
  }
}

const fetchCategories = async () => {
  categoryLoading.value = true
  try {
    const response = await getCategories()
    if (response.data) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>