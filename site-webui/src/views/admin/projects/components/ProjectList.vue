<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-6 flex-wrap">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="handleAdd">
          <Plus class="w-4 h-4 mr-2" />
          添加项目
        </n-button>
        <n-button text @click="handleRebuildIndex">重建索引</n-button>
      </div>
      <div class="flex items-center gap-3 flex-wrap">
        <n-input
          v-model:value="keyword"
          placeholder="搜索项目名称..."
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
        :columns="computedProjectColumns"
        :data="projects"
        :pagination="displayPagination"
        :remote="true"
        :page-count="pagination.pageCount"
        :show-header="true"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        :bordered="true"
        :loading="isLoading"
        :scroll-x="1200"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无项目数据</p>
            <n-button type="primary" size="small" @click="handleAdd" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个项目
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="isLoading">
        <div v-if="projects.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无项目数据</p>
          <n-button type="primary" size="small" @click="handleAdd" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加一个项目
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="project in projects"
            :key="project.id"
            size="small"
            class="hover:shadow-md transition-shadow project-card"
          >
            <div class="flex items-start gap-3">
              <div v-if="project.coverImage" class="w-14 h-14 rounded-lg overflow-hidden flex-shrink-0">
                <img :src="project.coverImage" :alt="project.name" class="w-full h-full object-cover" loading="lazy" />
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="font-semibold text-sm truncate flex-1">{{ project.name }}</h4>
                  <n-tag :type="getStatusType(project.status)" size="small">{{ getStatusText(project.status) }}</n-tag>
                </div>
                <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ project.description || '暂无描述' }}</p>
                <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
                  <span v-if="project.role">角色: {{ project.role }}</span>
                  <span v-if="project.startDate">{{ project.startDate?.split('T')[0] }} ~ {{ project.endDate ? project.endDate.split('T')[0] : '至今' }}</span>
                </div>
                <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                  <n-button text size="small" @click.stop="editProject(project.id)"><Edit class="w-4 h-4" /></n-button>
                  <n-button text size="small" type="error" @click.stop="deleteProject(project.id)"><Trash2 class="w-4 h-4" /></n-button>
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
      :style="{ width: '400px', backgroundColor: '#ffffff' }"
    >
      <p>确定要删除该项目吗？此操作无法撤销。</p>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NInput, NSelect, NCard, NDataTable, NModal, NSpace, NTag, NSpin, NPagination, useMessage } from 'naive-ui'
import { getProjects, deleteProject as deleteProjectApi, type ProjectList, type ProjectQuery } from '@/api/projects'
import { rebuildProjectIndex } from '@/api/search'
import { statusOptions, projectColumns, projectActions, getStatusType, getStatusText } from '../config'
import { useViewMode } from '@/composables/useViewMode'

const router = useRouter()
const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-projects-view')

const keyword = ref('')
const selectedStatus = ref<string | undefined>(undefined)
const deleteModal = ref(false)
const deleteId = ref<number | string | null>(null)

const projects = ref<ProjectList[]>([])
const pagination = ref({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  itemCount: 0
})

const displayPagination = computed(() => ({
  page: pagination.value.page,
  pageSize: pagination.value.pageSize,
  itemCount: pagination.value.itemCount,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
}))

const isLoading = ref(false)

const computedProjectColumns = computed(() => [
  ...projectColumns,
  projectActions({
    onEdit: editProject,
    onDelete: deleteProject
  })
])

const handleAdd = () => {
  router.push('/admin/projects/add')
}

const handleRebuildIndex = async () => {
  try {
    await rebuildProjectIndex()
    message.success('项目搜索索引重建成功')
  } catch (error) {
    console.error('重建项目索引失败:', error)
    message.error('重建项目索引失败')
  }
}

const editProject = (id: number | string) => {
  router.push(`/admin/projects/edit/${id}`)
}

const deleteProject = (id: number | string) => {
  deleteId.value = id
  deleteModal.value = true
}

const confirmDelete = async () => {
  if (deleteId.value) {
    await deleteProjectApi(deleteId.value)
    const deleteIdStr = String(deleteId.value)
    projects.value = projects.value.filter(p => String(p.id) !== deleteIdStr)
    message.success('删除成功')
  }
  deleteModal.value = false
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchProjects()
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
  fetchProjects()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  fetchProjects()
}

const resetFilters = () => {
  keyword.value = ''
  selectedStatus.value = undefined
  pagination.value.page = 1
  fetchProjects()
}

const fetchProjects = async () => {
  isLoading.value = true
  const query: ProjectQuery = {
    pageIndex: pagination.value.page,
    pageSize: pagination.value.pageSize,
    name: keyword.value || undefined,
    status: selectedStatus.value || undefined
  }

  try {
    const response = await getProjects(query)
    if (response.data) {
      projects.value = response.data.content || response.data.rows || []
      const totalRows = Number(response.data.totalRowCount) || 0
      pagination.value.itemCount = totalRows > 0 ? totalRows : 0
    } else {
      projects.value = []
      pagination.value.pageCount = 1
      pagination.value.itemCount = 0
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    projects.value = []
    pagination.value.pageCount = 1
    pagination.value.itemCount = 0
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchProjects()
})
</script>
