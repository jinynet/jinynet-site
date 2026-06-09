<template>
  <div 
    class="rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer group overflow-hidden min-h-[240px] flex flex-col"
    :class="isDark ? 'bg-gray-800' : 'bg-white'"
  >
    <div class="flex items-start justify-between mb-4">
      <span 
        class="px-3 py-1 text-sm rounded-full" 
        :style="{ backgroundColor: `${themeConfig.primaryColor}10`, color: themeConfig.primaryColor }"
      >
        {{ getStatusText(project.status) }}
      </span>
      <span class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">{{ formatDateRange(project.startDate, project.endDate) }}</span>
    </div>
    <h3 
      class="text-xl font-bold mb-2 group-hover:transition-colors"
      :class="isDark ? 'text-white' : 'text-gray-800'"
    >
      {{ project.name }}
    </h3>
    <p class="text-sm mb-3 line-clamp-2 flex-grow" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ project.description }}</p>
    
    <!-- 技术栈展示 -->
    <div v-if="project.stacks && project.stacks.length > 0" class="mb-4">
      <div class="flex flex-wrap gap-2">
        <span
          v-for="stack in project.stacks"
          :key="stack.id"
          class="text-xs px-2 py-1 rounded"
          :style="{ backgroundColor: stack.color || '#f3f4f6', color: getContrastColor(stack.color) || '#374151' }"
        >
          {{ stack.name }}
        </span>
      </div>
    </div>
    
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2" v-if="project.role">
        <span class="text-xs px-2 py-1 rounded" :class="isDark ? 'text-gray-400 bg-gray-700' : 'text-gray-500 bg-gray-100'">角色: {{ project.role }}</span>
      </div>
      <div class="flex items-center gap-3">
        <a
          v-if="project.projectUrl"
          :href="project.projectUrl"
          target="_blank"
          @click.stop
          class="hover:opacity-80 transition-colors"
          :style="{ color: themeConfig.primaryColor }"
        >
          在线预览
        </a>
        <a
          v-if="project.repoUrl"
          :href="project.repoUrl"
          target="_blank"
          @click.stop
          class="transition-colors"
          :class="isDark ? 'text-gray-400 hover:text-white' : 'text-gray-500 hover:text-gray-800'"
        >
          代码仓库
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { themeConfig, isDark } = useTheme()

interface ProjectStack {
  id: number
  name: string
  color?: string
}

interface Project {
  id: number
  name: string
  description: string | null
  status: string
  startDate: string | null
  endDate: string | null
  role: string | null
  projectUrl: string | null
  repoUrl: string | null
  stacks: ProjectStack[] | null
}

defineProps<{
  project: Project
}>()

const getStatusText = (status: string): string => {
  const map: Record<string, string> = {
    active: '进行中',
    completed: '已完成',
    paused: '暂停'
  }
  return map[status] || status
}

const formatDateRange = (start: string | null, end: string | null): string => {
  if (!start) return ''
  const startDate = start.split('T')[0]
  const endDate = end ? end.split('T')[0] : '至今'
  return `${startDate} - ${endDate}`
}

const getContrastColor = (hexColor?: string): string | null => {
  if (!hexColor) return null
  const color = hexColor.replace('#', '')
  if (color.length === 3) {
    const r = parseInt(color[0] + color[0], 16)
    const g = parseInt(color[1] + color[1], 16)
    const b = parseInt(color[2] + color[2], 16)
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    return brightness > 128 ? '#1f2937' : '#ffffff'
  }
  if (color.length === 6) {
    const r = parseInt(color.slice(0, 2), 16)
    const g = parseInt(color.slice(2, 4), 16)
    const b = parseInt(color.slice(4, 6), 16)
    const brightness = (r * 299 + g * 587 + b * 114) / 1000
    return brightness > 128 ? '#1f2937' : '#ffffff'
  }
  return null
}
</script>
