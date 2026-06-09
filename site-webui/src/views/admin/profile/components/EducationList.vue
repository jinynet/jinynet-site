<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
      <n-button type="primary" @click="openEducationModal">
        <Plus class="w-4 h-4 mr-2" />
        添加教育经历
      </n-button>
      <n-button @click="fetchEducations" circle size="small" title="刷新">
        <Refresh class="w-4 h-4" />
      </n-button>
      </div>
      <n-button text size="small" :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'" @click="toggleViewMode">
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" /><Menu class="w-4 h-4" v-else />
      </n-button>
    </div>
    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="educationColumns"
        :data="educations"
        :bordered="true"
        :loading="educationsLoading"
        :scroll-x="800"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无教育经历</p>
            <n-button type="primary" size="small" @click="openEducationModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一条教育经历
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>
    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="educationsLoading">
        <div v-if="educations.length === 0" class="flex flex-col items-center justify-center py-12"><p class="text-gray-400">暂无教育经历</p></div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card v-for="edu in educations" :key="edu.id" size="small" class="hover:shadow-md transition-shadow">
            <h4 class="font-semibold text-sm mb-1">{{ edu.schoolName }}</h4>
            <p class="text-xs text-gray-400">{{ edu.major }} · {{ getDegreeLabel(edu.degree) }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ edu.startDate }} ~ {{ edu.endDate || '至今' }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mt-2">{{ edu.description || '暂无描述' }}</p>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editEducation(edu)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="deleteEducationItem(edu)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>
    <n-modal
      v-model:show="showEducationModal"
      preset="card"
      :title="editingEducation ? '编辑教育经历' : '添加教育经历'"
      :style="{ width: '600px' }"
    >
      <n-form :model="educationForm" label-placement="top">
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="学校名称" path="schoolName">
              <n-input v-model:value="educationForm.schoolName" placeholder="请输入学校名称" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="学历" path="degree">
              <n-select v-model:value="educationForm.degree" :options="degreeOptions" placeholder="请选择学历" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="专业" path="major">
              <n-input v-model:value="educationForm.major" placeholder="请输入专业" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="排序" path="sortOrder">
              <n-input-number v-model:value="educationForm.sortOrder" :min="0" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="开始时间" path="startDate">
              <n-date-picker v-model:formatted-value="educationForm.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endDate">
              <n-date-picker v-model:formatted-value="educationForm.endDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="educationForm.description" type="textarea" placeholder="请输入描述" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEducationModal = false">取消</n-button>
          <n-button type="primary" @click="saveEducation">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteEducationModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除教育经历「{{ deletingEducationName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteEducationModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteEducation">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NDatePicker, NSelect, NInputNumber, NGrid, NGridItem, NSpin, useMessage } from 'naive-ui'
import { getEducations, createEducation, updateEducation, deleteEducation, type UserEducation } from '@/api/profile'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-profile-educations-view')

const getDegreeLabel = (degree: string | null) => {
  const found = degreeOptions.find(d => d.value === degree)
  return found?.label || degree || ''
}

const educations = ref<UserEducation[]>([])
const educationsLoading = ref(false)
const showEducationModal = ref(false)
const editingEducation = ref<UserEducation | null>(null)
const deleteEducationModal = ref(false)
const deletingEducationId = ref<number | null>(null)
const deletingEducationName = ref('')

const degreeOptions = [
  { label: '本科', value: 'bachelor' },
  { label: '硕士', value: 'master' },
  { label: '博士', value: 'doctor' },
  { label: '其他', value: 'other' }
]

const educationForm = ref({
  schoolName: '',
  major: '',
  degree: '' as 'bachelor' | 'master' | 'doctor' | 'other' | null,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  description: '',
  sortOrder: 0
})

const educationColumns = [
  { title: '学校', key: 'schoolName', minWidth: 150 },
  {
    title: '学历',
    key: 'degree',
    width: 100,
    render: (row: UserEducation) => {
      const degreeMap: Record<string, string> = {
        bachelor: '本科',
        master: '硕士',
        doctor: '博士',
        other: '其他'
      }
      return degreeMap[row.degree || ''] || row.degree
    }
  },
  { title: '专业', key: 'major', minWidth: 150 },
  { title: '开始时间', key: 'startDate', width: 120 },
  { title: '结束时间', key: 'endDate', width: 120 },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: UserEducation) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editEducation(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteEducationItem(row) }, () => '删除')
      ])
    }
  }
]

const openEducationModal = () => {
  editingEducation.value = null
  educationForm.value = { schoolName: '', major: '', degree: null, startDate: undefined, endDate: undefined, description: '', sortOrder: 0 }
  showEducationModal.value = true
}

const editEducation = (education: UserEducation) => {
  editingEducation.value = education
  educationForm.value = {
    schoolName: education.schoolName,
    major: education.major || '',
    degree: education.degree,
    startDate: education.startDate || '',
    endDate: education.endDate || '',
    description: education.description || '',
    sortOrder: education.sortOrder
  }
  showEducationModal.value = true
}

const deleteEducationItem = (education: UserEducation) => {
  deletingEducationId.value = education.id
  deletingEducationName.value = education.schoolName
  deleteEducationModal.value = true
}

const confirmDeleteEducation = async () => {
  if (deletingEducationId.value) {
    await deleteEducation(deletingEducationId.value)
    educations.value = educations.value.filter(e => e.id !== deletingEducationId.value)
    message.success('删除成功')
  }
  deleteEducationModal.value = false
}

const saveEducation = async () => {
  if (!educationForm.value.schoolName.trim() || !educationForm.value.degree) {
    message.error('请填写学校和学历')
    return
  }
  if (!educationForm.value.startDate) {
    message.error('请选择开始时间')
    return
  }

  try {
    const formData = {
      ...educationForm.value,
      startDate: educationForm.value.startDate!,
      endDate: educationForm.value.endDate || null,
      major: educationForm.value.major || null,
      description: educationForm.value.description || null
    }
    
    if (editingEducation.value) {
      await updateEducation({ id: editingEducation.value.id, ...formData })
      const index = educations.value.findIndex(e => e.id === editingEducation.value!.id)
      if (index !== -1) {
        educations.value[index] = { ...educations.value[index], ...formData }
      }
      message.success('更新成功')
    } else {
      const response = await createEducation(formData)
      educations.value.push(response.data)
      message.success('创建成功')
    }
    showEducationModal.value = false
  } catch (error) {
    console.error('保存教育经历失败:', error)
    message.error('保存失败')
  }
}

const fetchEducations = async () => {
  educationsLoading.value = true
  try {
    const response = await getEducations()
    if (response.data) {
      educations.value = response.data
    }
  } catch (error) {
    console.error('获取教育经历失败:', error)
  } finally {
    educationsLoading.value = false
  }
}

onMounted(() => {
  fetchEducations()
})
</script>
