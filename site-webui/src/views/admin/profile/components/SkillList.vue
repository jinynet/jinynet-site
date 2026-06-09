<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
        <n-button type="primary" @click="openSkillModal">
          <Plus class="w-4 h-4 mr-2" />
          添加技能
        </n-button>
        <n-button @click="fetchSkills" circle size="small" title="刷新">
          <Refresh class="w-4 h-4" />
        </n-button>
      </div>
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
        :columns="skillColumns"
        :data="skills"
        :bordered="true"
        :loading="skillsLoading"
        :scroll-x="700"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无技能数据</p>
            <n-button type="primary" size="small" @click="openSkillModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个技能
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>

    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="skillsLoading">
        <div v-if="skills.length === 0" class="flex flex-col items-center justify-center py-12">
          <p class="text-gray-400">暂无技能数据</p>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card v-for="skill in skills" :key="skill.id" size="small" class="hover:shadow-md transition-shadow">
            <h4 class="font-semibold text-sm mb-1">{{ skill.name }}</h4>
            <p class="text-xs text-gray-400 mb-1">{{ getCategoryLabel(skill.category) }}</p>
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs text-gray-400">熟练度:</span>
              <n-progress type="line" :percentage="skill.level * 20" :height="6" :show-indicator="false" />
              <span class="text-xs text-gray-400">{{ getLevelLabel(skill.level) }}</span>
            </div>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editSkill(skill)">
                <Edit class="w-4 h-4" />
              </n-button>
              <n-button text size="small" type="error" @click.stop="deleteSkillItem(skill)">
                <Trash2 class="w-4 h-4" />
              </n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>

    <n-modal
      v-model:show="showSkillModal"
      preset="card"
      :title="editingSkill ? '编辑技能' : '添加技能'"
      :style="{ width: '500px' }"
    >
      <n-form :model="skillForm" label-placement="top">
        <n-form-item label="技能名称" path="name">
          <n-input v-model:value="skillForm.name" placeholder="请输入技能名称" />
        </n-form-item>
        <n-form-item label="类别" path="category">
          <n-select v-model:value="skillForm.category" :options="categoryOptions" placeholder="请选择类别" />
        </n-form-item>
        <n-form-item label="熟练度" path="level">
          <n-select v-model:value="skillForm.level" :options="levelOptions" placeholder="请选择熟练度" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="skillForm.description" type="textarea" placeholder="请输入描述（可选）" :rows="2" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="skillForm.sortOrder" :min="0" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showSkillModal = false">取消</n-button>
          <n-button type="primary" @click="saveSkill">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteSkillModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除技能「{{ deletingSkillName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteSkillModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteSkill">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NSelect, NInputNumber, NProgress, NSpin, useMessage } from 'naive-ui'
import { getSkills, createSkill, updateSkill, deleteSkill, type UserSkills } from '@/api/profile'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-profile-skills-view')

const getCategoryLabel = (cat: string) => {
  const found = categoryOptions.find(c => c.value === cat)
  return found?.label || cat
}
const getLevelLabel = (level: number) => {
  const found = levelOptions.find(l => l.value === level)
  return found?.label || String(level)
}

const skills = ref<UserSkills[]>([])
const skillsLoading = ref(false)
const showSkillModal = ref(false)
const editingSkill = ref<UserSkills | null>(null)
const deleteSkillModal = ref(false)
const deletingSkillId = ref<number | null>(null)
const deletingSkillName = ref('')

const categoryOptions = [
  { label: '前端', value: 'frontend' },
  { label: '后端', value: 'backend' },
  { label: '数据库', value: 'database' },
  { label: '工具', value: 'tools' },
  { label: '其他', value: 'other' }
]

const skillForm = ref({
  name: '',
  category: 'frontend' as 'frontend' | 'backend' | 'database' | 'tools' | 'other',
  level: 3,
  description: '',
  sortOrder: 0
})

const levelOptions = [
  { label: '入门', value: 1 },
  { label: '熟悉', value: 2 },
  { label: '掌握', value: 3 },
  { label: '熟练', value: 4 },
  { label: '精通', value: 5 }
]

const skillColumns = [
  { title: '技能名称', key: 'name', minWidth: 120 },
  {
    title: '类别',
    key: 'category',
    width: 100,
    render: (row: UserSkills) => {
      const cat = categoryOptions.find(c => c.value === row.category)
      return cat?.label || row.category
    }
  },
  {
    title: '熟练度',
    key: 'level',
    width: 100,
    render: (row: UserSkills) => {
      const lvl = levelOptions.find(l => l.value === row.level)
      return lvl?.label || row.level
    }
  },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 200 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: UserSkills) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editSkill(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteSkillItem(row) }, () => '删除')
      ])
    }
  }
]

const openSkillModal = () => {
  editingSkill.value = null
  skillForm.value = { name: '', category: 'frontend', level: 3, description: '', sortOrder: 0 }
  showSkillModal.value = true
}

const editSkill = (skill: UserSkills) => {
  editingSkill.value = skill
  skillForm.value = {
    name: skill.name,
    category: skill.category,
    level: skill.level,
    description: skill.description || '',
    sortOrder: skill.sortOrder
  }
  showSkillModal.value = true
}

const deleteSkillItem = (skill: UserSkills) => {
  deletingSkillId.value = skill.id
  deletingSkillName.value = skill.name
  deleteSkillModal.value = true
}

const confirmDeleteSkill = async () => {
  if (deletingSkillId.value) {
    await deleteSkill(deletingSkillId.value)
    skills.value = skills.value.filter(s => s.id !== deletingSkillId.value)
    message.success('删除成功')
  }
  deleteSkillModal.value = false
}

const saveSkill = async () => {
  if (!skillForm.value.name.trim()) {
    message.error('请输入技能名称')
    return
  }

  try {
    if (editingSkill.value) {
      await updateSkill({ id: editingSkill.value.id, ...skillForm.value })
      const index = skills.value.findIndex(s => s.id === editingSkill.value!.id)
      if (index !== -1) {
        skills.value[index] = { ...skills.value[index], ...skillForm.value }
      }
      message.success('更新成功')
    } else {
      const response = await createSkill(skillForm.value)
      skills.value.push(response.data)
      message.success('创建成功')
    }
    showSkillModal.value = false
  } catch (error) {
    console.error('保存技能失败:', error)
    message.error('保存失败')
  }
}

const fetchSkills = async () => {
  skillsLoading.value = true
  try {
    const response = await getSkills()
    if (response.data) {
      skills.value = response.data
    }
  } catch (error) {
    console.error('获取技能失败:', error)
  } finally {
    skillsLoading.value = false
  }
}

onMounted(() => {
  fetchSkills()
})
</script>
