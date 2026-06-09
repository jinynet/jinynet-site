<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="openTagModal">
          <Plus class="w-4 h-4 mr-2" />
          添加标签
        </n-button>
        <n-button @click="fetchTags" circle size="small" title="刷新">
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
        :columns="tagColumns"
        :data="tags"
        :bordered="true"
        :loading="tagLoading"
        :scroll-x="700"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无标签数据</p>
            <n-button type="primary" size="small" @click="openTagModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个标签
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="tagLoading">
        <div v-if="tags.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无标签数据</p>
          <n-button type="primary" size="small" @click="openTagModal" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加一个标签
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="tag in tags"
            :key="tag.id"
            size="small"
            class="hover:shadow-md transition-shadow"
          >
            <div class="flex items-center gap-2 mb-2">
              <span class="w-3 h-3 rounded-full flex-shrink-0" :style="{ backgroundColor: tag.color || '#666' }"></span>
              <h4 class="font-semibold text-sm">{{ tag.name }}</h4>
            </div>
            <p class="text-xs text-gray-400 mb-1">别名: {{ tag.slug }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ tag.description || '暂无描述' }}</p>
            <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
              <span>排序: {{ tag.sortOrder }}</span>
            </div>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editTag(tag)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="deleteTagItem(tag)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="showTagModal"
      preset="card"
      :title="editingTag ? '编辑标签' : '添加标签'"
      :style="{ width: '500px' }"
    >
      <n-form :model="tagForm" label-placement="top" class="space-y-4">
        <n-form-item label="标签名称" path="name">
          <n-input v-model:value="tagForm.name" placeholder="请输入标签名称" />
        </n-form-item>
        <n-form-item label="标签别名" path="slug">
          <n-input v-model:value="tagForm.slug" placeholder="用于URL的别名" />
        </n-form-item>
        <n-form-item label="标签颜色" path="color">
          <n-input v-model:value="tagForm.color" placeholder="请输入颜色值" />
        </n-form-item>
        <n-form-item label="标签描述" path="description">
          <n-input v-model:value="tagForm.description" type="textarea" placeholder="请输入标签描述" :rows="2" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="tagForm.sortOrder" :min="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showTagModal = false">取消</n-button>
          <n-button type="primary" @click="saveTag">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteTagModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除标签「{{ deletingTagName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteTagModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteTag">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NInputNumber, NTag, NSpin, useMessage } from 'naive-ui'
import { getTags, createTag, updateTag, deleteTag, type ArticleTag } from '@/api/articles'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-article-tags-view')

const tags = ref<ArticleTag[]>([])
const tagLoading = ref(false)
const showTagModal = ref(false)
const editingTag = ref<ArticleTag | null>(null)
const deleteTagModal = ref(false)
const deletingTagId = ref<number | null>(null)
const deletingTagName = ref('')

const tagForm = ref({
  name: '',
  slug: '',
  color: '#666666',
  description: '',
  sortOrder: 0
})

const tagColumns = [
  { title: '标签名称', key: 'name', minWidth: 150 },
  { title: '别名', key: 'slug', minWidth: 120 },
  {
    title: '颜色',
    key: 'color',
    width: 100,
    render: (row: ArticleTag) => {
      return h(NTag, { type: 'default', style: { backgroundColor: row.color || '#666666', color: '#fff' } }, () => row.color || '#666666')
    }
  },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: ArticleTag) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editTag(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteTagItem(row) }, () => '删除')
      ])
    }
  }
]

const openTagModal = () => {
  editingTag.value = null
  tagForm.value = { name: '', slug: '', color: '#666666', description: '', sortOrder: 0 }
  showTagModal.value = true
}

const editTag = (tag: ArticleTag) => {
  editingTag.value = tag
  tagForm.value = {
    name: tag.name,
    slug: tag.slug,
    color: tag.color || '#666666',
    description: tag.description || '',
    sortOrder: tag.sortOrder
  }
  showTagModal.value = true
}

const deleteTagItem = (tag: ArticleTag) => {
  deletingTagId.value = tag.id
  deletingTagName.value = tag.name
  deleteTagModal.value = true
}

const confirmDeleteTag = async () => {
  if (deletingTagId.value) {
    await deleteTag(deletingTagId.value)
    tags.value = tags.value.filter(t => t.id !== deletingTagId.value)
    message.success('删除成功')
  }
  deleteTagModal.value = false
}

const saveTag = async () => {
  if (!tagForm.value.name.trim()) {
    message.error('请输入标签名称')
    return
  }

  try {
    if (editingTag.value) {
      await updateTag(editingTag.value.id, tagForm.value)
      const index = tags.value.findIndex(t => t.id === editingTag.value!.id)
      if (index !== -1) {
        tags.value[index] = { ...tags.value[index], ...tagForm.value }
      }
      message.success('更新成功')
    } else {
      const response = await createTag(tagForm.value)
      tags.value.push(response.data)
      message.success('创建成功')
    }
    showTagModal.value = false
  } catch (error) {
    console.error('保存标签失败:', error)
    message.error('保存失败')
  }
}

const fetchTags = async () => {
  tagLoading.value = true
  try {
    const response = await getTags()
    if (response.data) {
      tags.value = response.data
    }
  } catch (error) {
    console.error('获取标签失败:', error)
  } finally {
    tagLoading.value = false
  }
}

onMounted(() => {
  fetchTags()
})
</script>
