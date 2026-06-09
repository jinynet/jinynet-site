<template>
  <div class="project-form">
    <div class="sticky top-10 lg:top-6 z-20 bg-white dark:bg-gray-800 py-3 sm:py-4 -mx-4 sm:-mx-6 px-4 sm:px-6 border-b border-gray-300 dark:border-gray-700 mb-3 sm:mb-6">
      <div class="flex items-center justify-between flex-wrap gap-3">
        <h1 class="text-lg sm:text-xl font-bold text-dark dark:text-gray-100">{{ isEdit ? '编辑项目' : '添加项目' }}</h1>
        <div class="flex items-center gap-2">
          <n-button size="small" sm-size="medium" @click="handleCancel">取消</n-button>
          <n-button size="small" sm-size="medium" type="primary" :loading="isSubmitting" @click="handleSubmit">{{ isEdit ? '更新项目' : '添加项目' }}</n-button>
        </div>
      </div>
    </div>

    <div class="grid-wrapper">
      <div class="grid-main">
        <n-card title="基本信息" :bordered="false">
          <n-form :model="form" :rules="rules" ref="formRef" label-placement="top">
            <n-form-item label="项目名称" path="name">
              <n-input v-model:value="form.name" placeholder="请输入项目名称" @blur="handleNameBlur" />
            </n-form-item>

            <n-form-item label="项目别名" path="slug">
              <n-input v-model:value="form.slug" placeholder="请输入URL友好的别名（英文、数字、连字符）" />
            </n-form-item>

            <n-form-item label="项目描述" path="description">
              <n-input v-model:value="form.description" placeholder="请输入项目简短描述（用于列表展示）" :rows="2" type="textarea" />
            </n-form-item>

            <n-form-item label="项目详情">
              <n-input v-model:value="form.content" placeholder="请输入项目详细介绍（Markdown格式）" :rows="6" type="textarea" />
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="项目时间与链接" :bordered="false" class="mt-4 sm:mt-6">
          <div class="time-links-grid">
            <div>
              <n-form-item label="开始时间" path="startDate">
                <n-date-picker v-model:formatted-value="form.startDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" />
              </n-form-item>
            </div>
            <div>
              <n-form-item label="结束时间" path="endDate">
                <n-date-picker v-model:formatted-value="form.endDate" type="date" value-format="yyyy-MM-dd" style="width: 100%" :is-date-disabled="(date: number) => form.startDate != null ? date < new Date(form.startDate).getTime() : false" />
              </n-form-item>
            </div>
            <div>
              <n-form-item label="项目链接">
                <n-input v-model:value="form.projectUrl" placeholder="请输入项目在线地址" />
              </n-form-item>
            </div>
            <div>
              <n-form-item label="代码仓库">
                <n-input v-model:value="form.repoUrl" placeholder="请输入代码仓库地址（GitHub/GitLab/Gitee等）" />
              </n-form-item>
            </div>
          </div>
        </n-card>

        <n-card title="技术栈" :bordered="false" class="mt-4 sm:mt-6">
          <n-form-item label="选择技术栈">
            <n-checkbox-group v-model:value="form.stackIds">
              <div class="flex flex-wrap gap-2 sm:gap-3">
                <n-checkbox v-for="stack in availableStacks" :key="stack.id" :value="stack.id" :label="stack.name" />
              </div>
            </n-checkbox-group>
          </n-form-item>
          <template #footer v-if="availableStacks.length === 0">
            <div class="text-center text-gray-400 py-2">
              <p class="text-sm">暂无技术栈</p>
              <n-button text type="primary" size="small" @click="router.push('/admin/projects?tab=stacks')">
                去添加技术栈
              </n-button>
            </div>
          </template>
        </n-card>
      </div>

      <div class="grid-sidebar">
        <div class="sidebar-sticky">
          <n-card title="其他设置" :bordered="false">
            <n-form :model="form" label-placement="top">
              <n-form-item label="项目状态" path="status">
                <n-select v-model:value="form.status" :options="projectStatusOptions" placeholder="请选择项目状态" />
              </n-form-item>

              <n-form-item label="排序权重">
                <n-input-number v-model:value="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
                <template #feedback>
                  <span class="text-gray-400 text-xs">数值越小排序越靠前</span>
                </template>
              </n-form-item>

              <n-form-item label="是否公开">
                <div class="flex flex-col gap-2">
                  <n-switch v-model:value="form.published" />
                  <span class="text-xs text-gray-400">公开后将在首页展示</span>
                </div>
              </n-form-item>

              <n-form-item label="封面图片">
                <div class="flex flex-col gap-2">
                  <div v-if="form.coverImage" class="relative w-full h-20 sm:h-24 md:h-32 rounded border border-gray-200 overflow-hidden">
                    <img :src="form.coverImage" class="w-full h-full object-cover" />
                    <n-button
                      text
                      size="tiny"
                      class="absolute top-1 right-1 bg-black/50 text-white rounded px-2 py-0.5"
                      @click="form.coverImage = null"
                    >
                      删除
                    </n-button>
                  </div>
                  <n-input v-model:value="form.coverImage" placeholder="请输入封面图片URL" />
                </div>
              </n-form-item>

              <n-form-item label="我的角色">
                <n-input v-model:value="form.role" placeholder="如：技术负责人、全栈开发" />
              </n-form-item>

              <n-form-item label="项目贡献">
                <n-input v-model:value="form.contribution" placeholder="请描述您在项目中的主要贡献" :rows="4" type="textarea" />
              </n-form-item>
            </n-form>
          </n-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, NDatePicker, NSelect, NInputNumber, NCheckbox, NCheckboxGroup, NSwitch, useMessage } from 'naive-ui'
import { projectStatusOptions } from '../config'
import { getProjectById, updateProject, createProject, getProjectStacks } from '@/api/projects'
import type { ProjectForm, ProjectStack } from '@/api/projects'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const formRef = ref<any>(null)

const isEdit = computed(() => !!route.params.id)
const isSubmitting = ref(false)

const form = reactive({
  name: '',
  slug: '',
  description: null as string | null,
  content: null as string | null,
  coverImage: null as string | null,
  projectUrl: null as string | null,
  repoUrl: null as string | null,
  status: 'active',
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
  role: null as string | null,
  contribution: null as string | null,
  sortOrder: 0,
  published: true,
  stackIds: [] as number[]
})

const availableStacks = ref<ProjectStack[]>([])

const rules = {
  name: { required: true, message: '请输入项目名称', trigger: 'blur' },
  slug: { required: true, message: '请输入项目别名', trigger: 'blur' },
  status: { required: true, message: '请选择项目状态', trigger: 'change' }
}

const generateSlug = (name: string): string => {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

const handleNameBlur = () => {
  if (form.name && !form.slug) {
    form.slug = generateSlug(form.name)
  }
}

const fetchAvailableStacks = async () => {
  try {
    const response = await getProjectStacks()
    if (response.data) {
      availableStacks.value = response.data
    }
  } catch (error) {
    console.error('获取技术栈列表失败:', error)
  }
}

const fetchProjectDetail = async (id: number | string) => {
  try {
    const response = await getProjectById(id)
    if (response.data) {
      const data = response.data
      form.name = data.name || ''
      form.slug = data.slug || ''
      form.description = data.description || null
      form.content = data.content || null
      form.coverImage = data.coverImage || null
      form.projectUrl = data.projectUrl || null
      form.repoUrl = data.repoUrl || null
      form.status = data.status || 'active'
      form.startDate = data.startDate || null
      form.endDate = data.endDate || null
      form.role = data.role || null
      form.contribution = data.contribution || null
      form.sortOrder = data.sortOrder || 0
      form.published = data.published !== undefined ? data.published : true
      form.stackIds = data.stacks?.map((s: ProjectStack) => s.id) || []
    }
  } catch (error) {
    console.error('获取项目详情失败:', error)
    message.error('获取项目详情失败')
  }
}

const handleCancel = () => {
  router.push('/admin/projects')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    message.error('请完善表单信息')
    return
  }

  isSubmitting.value = true

  const submitData: ProjectForm = {
    name: form.name,
    slug: form.slug,
    description: form.description,
    content: form.content,
    coverImage: form.coverImage,
    projectUrl: form.projectUrl,
    repoUrl: form.repoUrl,
    status: form.status as 'active' | 'completed' | 'paused',
    startDate: form.startDate,
    endDate: form.endDate,
    role: form.role,
    contribution: form.contribution,
    sortOrder: form.sortOrder,
    published: form.published,
    stacks: form.stackIds.map(id => ({ id }))
  }

  try {
    if (isEdit.value) {
      await updateProject(route.params.id as string, submitData)
      message.success('更新成功')
    } else {
      await createProject(submitData)
      message.success('创建成功')
      router.push('/admin/projects')
    }
  } catch (error) {
    console.error('保存项目失败:', error)
    message.error('保存失败')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  fetchAvailableStacks()
  if (isEdit.value) {
    fetchProjectDetail(route.params.id as string)
  }
})
</script>

<style scoped>
.project-form {
  min-height: 100%;
  padding: 0 8px;
  max-width: 100%;
  box-sizing: border-box;
}

.grid-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (min-width: 1024px) {
  .grid-wrapper {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
  }

  .sidebar-sticky {
    position: sticky;
    top: 120px;
  }
}

.time-links-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (min-width: 640px) {
  .time-links-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
}

:deep(.n-card) {
  --n-padding-top: 12px;
  --n-padding-bottom: 12px;
  --n-padding-left: 12px;
  --n-padding-right: 12px;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

:deep(.n-card-header) {
  padding-bottom: 10px;
  margin-bottom: 4px;
  border-bottom: 1px solid #f3f4f6;
}

html.dark :deep(.n-card-header) {
  border-bottom-color: #374151;
}

:deep(.n-card-header-title) {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

html.dark :deep(.n-card-header-title) {
  color: #e5e7eb;
}

:deep(.n-card-body) {
  padding-top: 10px;
}

:deep(.n-form-item) {
  margin-bottom: 12px;
}

:deep(.n-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.n-form-item-label) {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 5px;
}

html.dark :deep(.n-form-item-label) {
  color: #d1d5db;
}

:deep(.n-form-item-feedback) {
  font-size: 11px;
  margin-top: 3px;
}

:deep(.n-input),
:deep(.n-select),
:deep(.n-date-picker),
:deep(.n-input-number) {
  font-size: 13px;
  border-radius: 6px;
}

:deep(.n-input-textarea) {
  resize: vertical;
  min-height: 80px;
}

:deep(.n-switch) {
  --n-active-color: #10b981;
}

:deep(.n-checkbox) {
  --n-label-text-color: #374151;
  font-size: 13px;
}

html.dark :deep(.n-checkbox) {
  --n-label-text-color: #d1d5db;
}

@media (min-width: 640px) {
  .project-form {
    padding: 0 12px;
  }

  :deep(.n-card) {
    --n-padding-top: 16px;
    --n-padding-bottom: 16px;
    --n-padding-left: 16px;
    --n-padding-right: 16px;
    border-radius: 12px;
  }

  :deep(.n-card-header) {
    padding-bottom: 12px;
  }

  :deep(.n-card-header-title) {
    font-size: 14px;
  }

  :deep(.n-form-item) {
    margin-bottom: 16px;
  }

  :deep(.n-form-item-label) {
    font-size: 13px;
    margin-bottom: 6px;
  }

  :deep(.n-form-item-feedback) {
    font-size: 12px;
  }

  :deep(.n-input),
  :deep(.n-select),
  :deep(.n-date-picker),
  :deep(.n-input-number) {
    font-size: 14px;
    border-radius: 8px;
  }

  :deep(.n-checkbox) {
    font-size: 14px;
  }
}

@media (min-width: 1024px) {
  .project-form {
    padding: 0 16px;
  }

  :deep(.n-card) {
    --n-padding-top: 20px;
    --n-padding-bottom: 20px;
    --n-padding-left: 20px;
    --n-padding-right: 20px;
  }

  :deep(.n-form-item) {
    margin-bottom: 20px;
  }

  :deep(.n-card-header-title) {
    font-size: 15px;
  }

  :deep(.n-input-textarea) {
    min-height: 100px;
  }
}
</style>
