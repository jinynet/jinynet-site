<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
      <n-button type="primary" @click="openWorkModal">
        <Plus class="w-4 h-4 mr-2" />
        添加工作经验
      </n-button>
      <n-button @click="fetchWorks" circle size="small" title="刷新">
        <Refresh class="w-4 h-4" />
      </n-button>
      </div>
      <n-button text size="small" :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'" @click="toggleViewMode">
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" /><Menu class="w-4 h-4" v-else />
      </n-button>
    </div>
    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="workColumns"
        :data="works"
        :bordered="true"
        :loading="worksLoading"
        :scroll-x="900"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无工作经验</p>
            <n-button type="primary" size="small" @click="openWorkModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一条工作经验
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>
    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="worksLoading">
        <div v-if="works.length === 0" class="flex flex-col items-center justify-center py-12"><p class="text-gray-400">暂无工作经验</p></div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card v-for="work in works" :key="work.id" size="small" class="hover:shadow-md transition-shadow">
            <h4 class="font-semibold text-sm mb-1">{{ work.companyName }}</h4>
            <p class="text-xs" style="color: var(--primary-color)">{{ work.position }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ work.startDate }} ~ {{ work.endDate || '至今' }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mt-2">{{ work.description || '暂无描述' }}</p>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editWork(work)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="deleteWorkItem(work)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>
    <n-modal
      v-model:show="showWorkModal"
      preset="card"
      :title="editingWork ? '编辑工作经验' : '添加工作经验'"
      :style="{ width: '600px' }"
    >
      <n-form :model="workForm" label-placement="top">
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="公司名称" path="companyName">
              <n-input v-model:value="workForm.companyName" placeholder="请输入公司名称" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="职位" path="position">
              <n-input v-model:value="workForm.position" placeholder="请输入职位" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="开始时间" path="startDate">
              <n-date-picker v-model:formatted-value="workForm.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endDate">
              <n-date-picker v-model:formatted-value="workForm.endDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="排序" path="sortOrder">
              <n-input-number v-model:value="workForm.sortOrder" :min="0" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="工作描述" path="description">
          <n-input v-model:value="workForm.description" type="textarea" placeholder="请输入工作描述" :rows="2" />
        </n-form-item>
        <n-form-item label="工作成就" path="achievements">
          <n-input v-model:value="workForm.achievements" type="textarea" placeholder="请输入工作成就（可选）" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showWorkModal = false">取消</n-button>
          <n-button type="primary" @click="saveWork">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteWorkModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除工作经验「{{ deletingWorkName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteWorkModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteWork">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NDatePicker, NInputNumber, NGrid, NGridItem, NSpin, useMessage } from 'naive-ui'
import { getWorkExperiences, createWorkExperience, updateWorkExperience, deleteWorkExperience, type UserWork } from '@/api/profile'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-profile-works-view')

const works = ref<UserWork[]>([])
const worksLoading = ref(false)
const showWorkModal = ref(false)
const editingWork = ref<UserWork | null>(null)
const deleteWorkModal = ref(false)
const deletingWorkId = ref<number | null>(null)
const deletingWorkName = ref('')

const workForm = ref({
  companyName: '',
  position: '',
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  description: '',
  achievements: '',
  sortOrder: 0
})

const workColumns = [
  { title: '公司', key: 'companyName', minWidth: 150 },
  { title: '职位', key: 'position', minWidth: 120 },
  { title: '开始时间', key: 'startDate', width: 120 },
  { title: '结束时间', key: 'endDate', width: 120 },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: UserWork) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editWork(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteWorkItem(row) }, () => '删除')
      ])
    }
  }
]

const openWorkModal = () => {
  editingWork.value = null
  workForm.value = { companyName: '', position: '', startDate: undefined, endDate: undefined, description: '', achievements: '', sortOrder: 0 }
  showWorkModal.value = true
}

const editWork = (work: UserWork) => {
  editingWork.value = work
  workForm.value = {
    companyName: work.companyName,
    position: work.position,
    startDate: work.startDate || '',
    endDate: work.endDate || '',
    description: work.description || '',
    achievements: work.achievements || '',
    sortOrder: work.sortOrder
  }
  showWorkModal.value = true
}

const deleteWorkItem = (work: UserWork) => {
  deletingWorkId.value = work.id
  deletingWorkName.value = work.companyName
  deleteWorkModal.value = true
}

const confirmDeleteWork = async () => {
  if (deletingWorkId.value) {
    await deleteWorkExperience(deletingWorkId.value)
    works.value = works.value.filter(w => w.id !== deletingWorkId.value)
    message.success('删除成功')
  }
  deleteWorkModal.value = false
}

const saveWork = async () => {
  if (!workForm.value.companyName.trim() || !workForm.value.position.trim()) {
    message.error('请填写公司和职位')
    return
  }
  if (!workForm.value.startDate) {
    message.error('请选择开始时间')
    return
  }

  try {
    const formData = {
      ...workForm.value,
      startDate: workForm.value.startDate!,
      endDate: workForm.value.endDate || null,
      description: workForm.value.description || null,
      achievements: workForm.value.achievements || null
    }
    
    if (editingWork.value) {
      await updateWorkExperience({ id: editingWork.value.id, ...formData })
      const index = works.value.findIndex(w => w.id === editingWork.value!.id)
      if (index !== -1) {
        works.value[index] = { ...works.value[index], ...formData }
      }
      message.success('更新成功')
    } else {
      const response = await createWorkExperience(formData)
      works.value.push(response.data)
      message.success('创建成功')
    }
    showWorkModal.value = false
  } catch (error) {
    console.error('保存工作经验失败:', error)
    message.error('保存失败')
  }
}

const fetchWorks = async () => {
  worksLoading.value = true
  try {
    const response = await getWorkExperiences()
    if (response.data) {
      works.value = response.data
    }
  } catch (error) {
    console.error('获取工作经验失败:', error)
  } finally {
    worksLoading.value = false
  }
}

onMounted(() => {
  fetchWorks()
})
</script>
