<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4 flex-wrap">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="handleAdd">
          <Plus class="w-4 h-4 mr-2" />
          创建文章
        </n-button>
        <n-button text @click="handleRebuildIndex">重建索引</n-button>
      </div>
      <div class="flex items-center gap-3 flex-wrap">

        <n-input
          v-model:value="keyword"
          placeholder="搜索文章标题..."
          style="min-width: 200px; width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <Search class="w-4 h-4" />
          </template>
        </n-input>
        <n-select
          v-model:value="selectedStatus"
          placeholder="选择状态"
          :options="statusOptions"
          style="min-width: 140px; width: 140px"
          clearable
        />
        <n-button @click="handleSearch">搜索</n-button>
        <n-button text @click="resetFilters">重置</n-button>
        <n-button text :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'" @click="toggleViewMode">
          <Grid class="w-4 h-4" v-if="viewMode === 'table'" />
          <Menu class="w-4 h-4" v-else />
        </n-button>
      </div>
    </div>

    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        ref="dataTableRef"
        :columns="articleColumns"
        :data="articles"
        :pagination="displayPagination"
        :remote="true"
        :page-count="pagination.pageCount"
        :show-header="true"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:sorter="handleSorterChange"
        :bordered="true"
        :loading="isLoading"
        :scroll-x="1200"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无文章数据</p>
            <n-button type="primary" size="small" @click="handleAdd" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一篇文章
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="isLoading">
        <div v-if="articles.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无文章数据</p>
          <n-button type="primary" size="small" @click="handleAdd" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加一篇文章
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="article in articles"
            :key="article.id"
            size="small"
            class="hover:shadow-md transition-shadow cursor-pointer article-card"
          >
            <div class="flex items-start gap-3">
              <div v-if="article.coverImage" class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0">
                <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" loading="lazy" />
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="font-semibold text-sm truncate flex-1" @click.stop="editArticle(article.id)">{{ article.title }}</h4>
                  <n-tag :type="getStatusType(article.status)" size="small">{{ getStatusText(article.status) }}</n-tag>
                </div>
                <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ article.excerpt || '暂无摘要' }}</p>
                <div class="flex items-center gap-3 text-xs text-gray-400 mb-2">
                  <span>👁 {{ article.viewCount || 0 }}</span>
                  <span>❤ {{ article.likeCount || 0 }}</span>
                  <span v-if="article.publishedAt">{{ formatDate(article.publishedAt) }}</span>
                </div>
                <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                  <n-button text size="small" @click.stop="editArticle(article.id)"><Edit class="w-4 h-4" /></n-button>
                  <n-button text size="small" @click.stop="viewArticle(article.id)"><Eye class="w-4 h-4" /></n-button>
                  <n-button text size="small" type="error" @click.stop="deleteArticle(article.id)"><Trash2 class="w-4 h-4" /></n-button>
                </div>
              </div>
            </div>
          </n-card>
        </div>
        <div v-if="pagination.itemCount > pagination.pageSize" class="flex justify-center mt-4">
          <n-pagination
            :page="pagination.page"
            :page-size="pagination.pageSize"
            :item-count="pagination.itemCount"
            :page-slot="5"
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="deleteModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除该文章吗？此操作无法撤销。</p>
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
import { ref, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Grid, Menu, Eye, Edit, Trash2 } from '@/icons'
import { NButton, NInput, NSelect, NCard, NDataTable, NModal, NSpace, NTag, NSpin, NPagination, useMessage, type DataTableInst, type DataTableSortState } from 'naive-ui'
import { getArticles, deleteArticle as deleteArticleApi, type ArticleList, type ArticleQuery } from '@/api/articles'
import { rebuildArticleIndex } from '@/api/search'
import { statusOptions, getStatusType, getStatusText } from '../config'
import { useViewMode } from '@/composables/useViewMode'

const router = useRouter()
const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-articles-view')

const formatDate = (dateString: string | null) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const keyword = ref('')
const selectedStatus = ref<string | undefined>(undefined)
const deleteModal = ref(false)
const deleteId = ref<number | null>(null)

// DataTable 实例引用
const dataTableRef = ref<DataTableInst | null>(null)

// 排序状态
const sorters = ref<DataTableSortState[]>([{ columnKey: 'updatedAt', order: 'descend', sorter: true }])

const articles = ref<ArticleList[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  itemCount: 1
})

const displayPagination = computed(() => ({
  page: pagination.value.page,
  pageSize: pagination.value.pageSize,
  itemCount: pagination.value.itemCount,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const isLoading = ref(false)

const articleColumns = [
  {
    title: '标题',
    key: 'title',
    ellipsis: true,
    minWidth: 200,
    sorter: true
  },
  { title: '摘要', key: 'excerpt', ellipsis: true, minWidth: 200 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    sorter: true,
    render: (row: ArticleList) => {
      const type = getStatusType(row.status)
      const text = getStatusText(row.status)
      return h(NTag, { type, size: 'small' }, () => text)
    }
  },
  {
    title: '阅读量',
    key: 'viewCount',
    width: 100,
    sorter: true
  },
  {
    title: '点赞数',
    key: 'likeCount',
    width: 100,
    sorter: true
  },
  {
    title: '发布时间',
    key: 'publishedAt',
    width: 180,
    sorter: true,
    render: (row: ArticleList) => formatDate(row.publishedAt)
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    width: 180,
    sorter: true,
    render: (row: ArticleList) => formatDate(row.updatedAt)
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    fixed: 'right' as const,
    render: (row: ArticleList) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editArticle(row.id) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => viewArticle(row.id) }, () => '预览'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteArticle(row.id) }, () => '删除')
      ])
    }
  }
]

const handleAdd = () => {
  router.push('/admin/articles/add')
}

const handleRebuildIndex = async () => {
  try {
    await rebuildArticleIndex()
    message.success('文章搜索索引重建成功')
  } catch (error) {
    console.error('重建文章索引失败:', error)
    message.error('重建文章索引失败')
  }
}

const editArticle = (id: number) => {
  router.push(`/admin/articles/edit/${id}`)
}

const viewArticle = (id: number) => {
  router.push(`/articles/${id}`)
}

const deleteArticle = (id: number) => {
  deleteId.value = id
  deleteModal.value = true
}

const confirmDelete = async () => {
  if (deleteId.value) {
    await deleteArticleApi(deleteId.value)
    articles.value = articles.value.filter(a => a.id !== deleteId.value)
    message.success('删除成功')
  }
  deleteModal.value = false
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchArticles()
}

const resetFilters = () => {
  keyword.value = ''
  selectedStatus.value = undefined
  sorters.value = [{ columnKey: 'updatedAt', order: 'descend', sorter: true }]
  dataTableRef.value?.clearSorter()
  dataTableRef.value?.sort('updatedAt', 'descend')
  pagination.value.page = 1
  fetchArticles()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  fetchArticles()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  fetchArticles()
}

const handleSorterChange = (newSorters: DataTableSortState[] | DataTableSortState | null) => {
  if (!newSorters) {
    sorters.value = []
  } else if (Array.isArray(newSorters)) {
    sorters.value = newSorters
  } else {
    sorters.value = [newSorters]
  }
  pagination.value.page = 1
  fetchArticles()
}

const fetchArticles = async () => {
  isLoading.value = true
  
  const orderBy = sorters.value.map(s => `${s.columnKey} ${s.order === 'ascend' ? 'asc' : 'desc'}`).join(', ')
  
  const query: ArticleQuery = {
    pageIndex: pagination.value.page,
    pageSize: pagination.value.pageSize,
    title: keyword.value || undefined,
    status: selectedStatus.value || undefined,
    orderBy: orderBy || undefined
  }

  try {
    const response = await getArticles(query)
    if (response.data && response.data) {
      articles.value = response.data.content || response.data.rows || []
      const totalPages = response.data.totalPages || response.data.totalPageCount || 0
      const totalRows = Number(response.data.totalRowCount) || 0
      pagination.value.pageCount = totalPages > 0 ? totalPages : 1
      pagination.value.itemCount = totalRows > 0 ? totalRows : 1
    } else {
      articles.value = []
      pagination.value.pageCount = 1
      pagination.value.itemCount = 0
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
    articles.value = []
    pagination.value.pageCount = 1
    pagination.value.itemCount = 0
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchArticles()
})
</script>