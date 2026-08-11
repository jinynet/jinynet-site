<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center gap-3 mb-4">
      <n-button type="primary" @click="openUploadModal">
        <Upload class="w-4 h-4 mr-2" />
        上传文件
      </n-button>
      <n-button @click="fetchFiles" circle size="small" title="刷新">
        <Refresh class="w-4 h-4" />
      </n-button>

      <div class="flex-1"></div>

      <n-select
        v-model:value="selectedCategory"
        placeholder="选择分类"
        :options="categoryOptions"
        style="width: 180px"
        @update:value="handleCategoryChange"
      />

      <n-input
        v-model:value="searchKeyword"
        placeholder="搜索文件名..."
        style="width: 200px"
        @keyup.enter="handleSearch"
      >
        <template #suffix>
          <Search class="w-4 h-4" />
        </template>
      </n-input>

      <n-button @click="handleSearch">搜索</n-button>

      <n-button 
        text 
        :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'"
        @click="toggleViewMode"
      >
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" />
        <Menu class="w-4 h-4" v-else />
      </n-button>
    </div>

    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="fileColumns"
        :data="files"
        :bordered="true"
        :loading="fileLoading"
        :row-key="(row: FileInfo) => row.id"
        :scroll-x="1200"
        :row-props="rowProps"
        :pagination="displayPagination"
        :remote="true"
        :page-count="pageCount"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <FileText class="w-16 h-16 text-gray-300 mb-4" />
            <p class="text-gray-400">暂无文件数据</p>
            <n-button type="primary" size="small" @click="openUploadModal" class="mt-4">
              <Upload class="w-4 h-4 mr-2" />
              上传文件
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="fileLoading">
        <div v-if="files.length === 0" class="flex flex-col items-center justify-center py-12">
          <FileText class="w-16 h-16 text-gray-300 mb-4" />
          <p class="text-gray-400">暂无文件数据</p>
          <n-button type="primary" size="small" @click="openUploadModal" class="mt-4">
            <Upload class="w-4 h-4 mr-2" />
            上传文件
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="file in files"
            :key="file.id"
            size="small"
            class="hover:shadow-md transition-shadow cursor-pointer file-card"
            @click="previewFile(file)"
          >
            <div class="flex items-start gap-3">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0" 
                :class="'bg-gray-100'"
              >
                <FileText class="w-6 h-6 text-blue-500" />
              </div>
              <div class="flex-1 min-w-0">
                <h4 class="font-semibold text-sm truncate mb-1">{{ file.originalFilename || file.filename }}</h4>
                <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
                  <span>{{ file.fileType }}</span>
                  <span>{{ formatFileSize(file.fileSize) }}</span>
                  <span v-if="file.categoryName">{{ file.categoryName }}</span>
                </div>
                <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700" @click.stop>
                  <n-button text size="small" @click="downloadFile(file)"><Download class="w-4 h-4" /></n-button>
                  <n-button text size="small" @click="editFile(file)"><Edit class="w-4 h-4" /></n-button>
                  <n-button text size="small" type="error" @click="deleteFileItem(file)"><Trash2 class="w-4 h-4" /></n-button>
                </div>
              </div>
            </div>
          </n-card>
        </div>
      </n-spin>
      <div v-if="files.length > 0" class="flex justify-center mt-4">
        <n-pagination
          :page="page"
          :page-size="pageSize"
          :item-count="totalCount || files.length"
          :page-slot="5"
          show-size-picker
          :page-sizes="[10, 20, 50]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-card>

    <FileUploadModal v-model:show="showUploadModal" :categories="categories" @success="onUploadSuccess" />

    <n-modal
      v-model:show="showEditModal"
      preset="card"
      title="编辑文件信息"
      :style="{ width: '700px' }"
    >
      <n-form :model="editForm" label-placement="top" class="space-y-4">
        <n-form-item label="文件名" path="originalFilename">
          <n-input v-model:value="editForm.originalFilename" placeholder="请输入原始文件名" />
        </n-form-item>
        <n-form-item label="所属分类" path="categoryId">
          <n-select
            v-model:value="editForm.categoryId"
            placeholder="选择分类"
            :options="categoryOptions"
          />
        </n-form-item>
        <n-form-item label="文件描述" path="description">
          <n-input
            v-model:value="editForm.description"
            type="textarea"
            placeholder="请输入文件描述"
            :rows="3"
          />
        </n-form-item>
        <n-form-item>
          <n-switch v-model:value="editForm.isPublic" />
          <span class="ml-2">公开访问</span>
        </n-form-item>
        <n-form-item label="文件信息" class="text-gray-400 text-sm">
          <p>存储文件名: {{ editingFile?.filename }}</p>
          <p>文件大小: {{ formatFileSize(editingFile?.fileSize || 0) }}</p>
          <p>上传时间: {{ editingFile?.createdAt }}</p>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" @click="saveFileInfo">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteFileModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除文件「{{ deletingFileName }}」吗？此操作不可恢复。</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteFileModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteFile">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Upload, Refresh, Search, FileText, Download, Edit, Trash2, Grid, Menu } from '@/icons'
import {
  NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput,
  NSelect, NSwitch, NSpin, NPagination, useMessage
} from 'naive-ui'
import {
  getFileList, updateFile, deleteFile, getCategories,
  type FileInfo, type FileCategory
} from '@/api/files'
import { useViewMode } from '@/composables/useViewMode'
import FileUploadModal from './FileUploadModal.vue'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-files-view')

const files = ref<FileInfo[]>([])
const fileLoading = ref(false)
const showUploadModal = ref(false)
const showEditModal = ref(false)
const deleteFileModal = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const pageCount = ref(1)

const categories = ref<FileCategory[]>([])
const categoryOptions = computed(() => [
  { label: '全部', value: undefined },
  ...categories.value.map(c => ({ label: c.name, value: c.id }))
])

const editingFile = ref<FileInfo | null>(null)
const editForm = ref({
  originalFilename: '',
  categoryId: undefined as number | undefined,
  description: '',
  isPublic: false
})

const deletingFileId = ref<number | null>(null)
const deletingFileName = ref('')

const displayPagination = computed(() => ({
  page: page.value,
  pageSize: pageSize.value,
  itemCount: totalCount.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const rowProps = (row: FileInfo) => {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      previewFile(row)
    }
  }
}

const fileColumns = [
  // { title: '文件名', key: 'filename', ellipsis: true, minWidth: 180 },
  { title: '文件名', key: 'originalFilename', ellipsis: true, minWidth: 180 },
  { title: '类型', key: 'fileType', width: 100 },
  { title: '大小', key: 'fileSize', width: 100, render: (row: FileInfo) => formatFileSize(row.fileSize) },
  { title: '分类', key: 'categoryName', width: 120, render: (row: FileInfo) => row.categoryName || '-' },
  { title: '公开', key: 'isPublic', width: 80, render: (row: FileInfo) => row.isPublic ? '是' : '否' },
  { title: '上传时间', key: 'createdAt', width: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right' as const,
    render: (row: FileInfo) => {
      return h('div', { class: 'flex gap-1', onClick: (e: Event) => e.stopPropagation() }, [
        h(NButton, { text: true, size: 'tiny', onClick: () => downloadFile(row) }, () => h(Download, { class: 'w-4 h-4' })),
        h(NButton, { text: true, size: 'tiny', onClick: () => editFile(row) }, () => h(Edit, { class: 'w-4 h-4' })),
        h(NButton, { text: true, size: 'tiny', status: 'error', onClick: () => deleteFileItem(row) }, () => h(Trash2, { class: 'w-4 h-4' }))
      ])
    }
  }
]

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const openUploadModal = () => {
  showUploadModal.value = true
}
const onUploadSuccess = () => { fetchFiles() }

const previewFile = (file: FileInfo) => { window.open(file.url, '_blank') }

const editFile = (file: FileInfo) => {
  editingFile.value = file
  editForm.value = {
    originalFilename: file.originalFilename,
    categoryId: file.categoryId ?? undefined,
    description: file.description || '',
    isPublic: file.isPublic
  }
  showEditModal.value = true
}

const saveFileInfo = async () => {
  if (!editingFile.value) return

  try {
    await updateFile(editingFile.value.id, {
      originalFilename: editForm.value.originalFilename,
      categoryId: editForm.value.categoryId,
      description: editForm.value.description,
      isPublic: editForm.value.isPublic
    })

    const index = files.value.findIndex(f => f.id === editingFile.value!.id)
    if (index !== -1) {
      files.value[index] = {
        ...files.value[index],
        ...editForm.value,
        categoryId: editForm.value.categoryId || null,
        categoryName: categories.value.find(c => c.id === editForm.value.categoryId)?.name || null
      }
    }

    message.success('更新成功')
    showEditModal.value = false
  } catch (error) {
    console.error('更新失败:', error)
    message.error('更新失败')
  }
}

const deleteFileItem = (file: FileInfo) => {
  deletingFileId.value = file.id
  deletingFileName.value = file.filename
  deleteFileModal.value = true
}

const confirmDeleteFile = async () => {
  if (deletingFileId.value) {
    await deleteFile(deletingFileId.value)
    files.value = files.value.filter(f => f.id !== deletingFileId.value)
    message.success('删除成功')
    totalCount.value--
  }
  deleteFileModal.value = false
}

const downloadFile = (file: FileInfo) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.originalFilename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleSearch = () => {
  page.value = 1
  fetchFiles()
}

const handleCategoryChange = () => {
  page.value = 1
  fetchFiles()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchFiles()
}

const handlePageSizeChange = (newPageSize: number) => {
  pageSize.value = newPageSize
  page.value = 1
  fetchFiles()
}

const fetchFiles = async () => {
  fileLoading.value = true
  try {
    const response = await getFileList(
      selectedCategory.value || undefined,
      searchKeyword.value || undefined,
      page.value,
      pageSize.value
    )
    if (response.data) {
      const data = response.data
      // 兼容多种分页数据结构
      if (data.content) {
        // Jimmer Page 对象格式
        files.value = data.content || []
        const totalPages = data.totalPages || data.totalPageCount || 0
        const totalRows = Number(data.totalRowCount) || Number(data.totalElements) || 0
        pageCount.value = totalPages > 0 ? totalPages : 1
        totalCount.value = totalRows > 0 ? totalRows : 0
      } else if (data.rows) {
        // 传统分页格式（Jimmer Page）
        files.value = data.rows || []
        const totalPages = data.totalPages || data.totalPageCount || 0
        const totalRows = Number(data.totalRowCount) || Number(data.totalElements) || 0
        pageCount.value = totalPages > 0 ? totalPages : 1
        totalCount.value = totalRows > 0 ? totalRows : 0
      } else if (Array.isArray(data)) {
        // 数组格式
        files.value = data
        totalCount.value = data.length
        pageCount.value = 1
      } else if (data.data && data.data.rows) {
        // Result 包裹未完全解包的情况
        const inner = data.data
        files.value = inner.rows || inner.content || []
        const totalPages = inner.totalPages || inner.totalPageCount || 0
        const totalRows = Number(inner.totalRowCount) || Number(inner.totalElements) || 0
        pageCount.value = totalPages > 0 ? totalPages : 1
        totalCount.value = totalRows > 0 ? totalRows : 0
      } else {
        console.warn('未知的数据结构:', data)
        files.value = []
        totalCount.value = 0
        pageCount.value = 1
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组
      files.value = response
      totalCount.value = response.length
      pageCount.value = 1
    } else {
      files.value = []
      totalCount.value = 0
      pageCount.value = 1
    }
    // 兜底：如果后端未返回分页信息，至少根据当前页数据量推断
    if (totalCount.value === 0 && files.value.length > 0) {
      totalCount.value = files.value.length
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    files.value = []
    totalCount.value = 0
    pageCount.value = 1
  } finally {
    fileLoading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await getCategories()
    if (Array.isArray(response)) {
      categories.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      categories.value = response.data
    } else {
      console.warn('分类数据格式异常:', response)
      categories.value = []
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const route = useRoute()
const router = useRouter()

// 任务管理器"继续"触发：自动打开上传弹窗
function checkAutoOpenUpload() {
  // 路由 query 触发（跨页面跳转）
  if (route.query.resume === '1') {
    localStorage.setItem('ptech_upload_autoopen', '1')
    showUploadModal.value = true
    router.replace({ query: {} })
  }
}

let openUploadListener: (() => void) | null = null

onMounted(() => {
  fetchCategories()
  fetchFiles()
  checkAutoOpenUpload()
  // 监听自定义事件（已在文件管理页时触发）
  openUploadListener = () => { showUploadModal.value = true }
  window.addEventListener('ptech:open-upload', openUploadListener)
})

onUnmounted(() => {
  if (openUploadListener) window.removeEventListener('ptech:open-upload', openUploadListener)
})
</script>