<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-6">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="openStackModal">
          <Plus class="w-4 h-4 mr-2" />
          添加技术栈
        </n-button>
        <n-button @click="fetchStacks" circle size="small" title="刷新">
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
        :columns="computedStackColumns"
        :data="stacks"
        :bordered="true"
        :loading="stackLoading"
        :scroll-x="700"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无技术栈数据</p>
            <n-button type="primary" size="small" @click="openStackModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个技术栈
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="stackLoading">
        <div v-if="stacks.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无技术栈数据</p>
          <n-button type="primary" size="small" @click="openStackModal" class="mt-4">
            <Plus class="w-4 h-4 mr-2" />
            添加一个技术栈
          </n-button>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card
            v-for="stack in stacks"
            :key="stack.id"
            size="small"
            class="hover:shadow-md transition-shadow"
          >
            <div class="flex items-center gap-2 mb-2">
              <span 
                class="w-4 h-4 rounded-full border flex-shrink-0" 
                :style="{ backgroundColor: stack.color || '#ccc' }"
              ></span>
              <h4 class="font-semibold text-sm">{{ stack.name }}</h4>
            </div>
            <p class="text-xs text-gray-400 mb-1">类别: {{ getStackCategoryText(stack.category) }}</p>
            <p class="text-xs text-gray-400 line-clamp-2 mb-2">{{ stack.description || '暂无描述' }}</p>
            <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
              <span>排序: {{ stack.sortOrder }}</span>
            </div>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editStack(stack)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="deleteStackItem(stack)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="showStackModal"
      preset="card"
      :title="editingStack ? '编辑技术栈' : '添加技术栈'"
      :style="{ width: '500px', backgroundColor: '#ffffff' }"
    >
      <n-form :model="stackForm" label-placement="top" class="space-y-4">
        <n-form-item label="技术栈名称" path="name">
          <n-input v-model:value="stackForm.name" placeholder="请输入技术栈名称" />
        </n-form-item>
        <n-form-item label="类别" path="category">
          <n-select
            v-model:value="stackForm.category"
            :options="stackCategoryOptions"
            placeholder="请选择类别"
          />
        </n-form-item>
        <n-form-item label="图标URL" path="icon">
          <n-input v-model:value="stackForm.icon" placeholder="请输入图标URL" />
        </n-form-item>
        <n-form-item label="颜色" path="color">
          <div class="flex items-center gap-2">
            <input type="color" v-model="stackForm.color" class="w-12 h-10 rounded cursor-pointer border" />
            <n-input v-model:value="stackForm.color" placeholder="#666666" class="w-32" />
          </div>
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="stackForm.description" type="textarea" placeholder="请输入描述" :rows="2" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="stackForm.sortOrder" placeholder="排序数字" :min="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showStackModal = false">取消</n-button>
          <n-button type="primary" @click="saveStack">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteStackModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px', backgroundColor: '#ffffff' }"
    >
      <p>确定要删除技术栈「{{ deletingStackName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteStackModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteStack">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NSelect, NInputNumber, NSpin, useMessage } from 'naive-ui'
import { getProjectStacks, createProjectStack, updateProjectStack, deleteProjectStack, type ProjectStack } from '@/api/projects'
import { stackColumns, stackActions, stackCategoryOptions, defaultStackForm } from '../config'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-project-stacks-view')

const getStackCategoryText = (category: string): string => {
  const option = stackCategoryOptions.find(o => o.value === category)
  return option?.label || category
}

const stacks = ref<ProjectStack[]>([])
const stackLoading = ref(false)
const showStackModal = ref(false)
const editingStack = ref<ProjectStack | null>(null)
const deleteStackModal = ref(false)
const deletingStackId = ref<number | null>(null)
const deletingStackName = ref('')

const computedStackColumns = computed(() => [
  ...stackColumns,
  stackActions({
    onEdit: editStack,
    onDelete: deleteStackItem
  })
])

const stackForm = ref(defaultStackForm())

const openStackModal = () => {
  editingStack.value = null
  stackForm.value = defaultStackForm()
  showStackModal.value = true
}

const editStack = (stack: ProjectStack) => {
  editingStack.value = stack
  stackForm.value = {
    name: stack.name,
    category: stack.category,
    icon: stack.icon || '',
    color: stack.color || '',
    description: stack.description || '',
    sortOrder: stack.sortOrder || 0
  }
  showStackModal.value = true
}

const deleteStackItem = (stack: ProjectStack) => {
  deletingStackId.value = stack.id
  deletingStackName.value = stack.name
  deleteStackModal.value = true
}

const confirmDeleteStack = async () => {
  if (deletingStackId.value) {
    await deleteProjectStack(deletingStackId.value)
    stacks.value = stacks.value.filter(s => s.id !== deletingStackId.value)
    message.success('删除成功')
  }
  deleteStackModal.value = false
}

const saveStack = async () => {
  if (!stackForm.value.name.trim()) {
    message.error('请输入技术栈名称')
    return
  }

  try {
    if (editingStack.value) {
      await updateProjectStack(editingStack.value.id, stackForm.value)
      const index = stacks.value.findIndex(s => s.id === editingStack.value!.id)
      if (index !== -1) {
        stacks.value[index] = { ...stacks.value[index], ...stackForm.value }
      }
      message.success('更新成功')
    } else {
      const response = await createProjectStack(stackForm.value)
      stacks.value.push(response.data)
      message.success('创建成功')
    }
    showStackModal.value = false
    stackForm.value = defaultStackForm()
  } catch (error) {
    console.error('保存技术栈失败:', error)
    message.error('保存失败')
  }
}

const fetchStacks = async () => {
  stackLoading.value = true
  try {
    const response = await getProjectStacks()
    if (response.data) {
      stacks.value = response.data
    }
  } catch (error) {
    console.error('获取技术栈失败:', error)
  } finally {
    stackLoading.value = false
  }
}

onMounted(() => {
  fetchStacks()
})
</script>
