<template>
  <article class="card-elevated r-lg p-6 group min-h-[240px] flex flex-col">
    <!-- 顶部：状态 + 时间 -->
    <div class="flex items-start justify-between gap-4 mb-4">
      <span
        class="accent-pill shrink-0"
        :style="{
          backgroundColor: `${themeConfig.primaryColor}12`,
          color: themeConfig.primaryColor
        }"
      >
        {{ getStatusText(project.status) }}
      </span>
      <span class="text-xs sm:text-sm text-faint whitespace-nowrap pt-0.5">
        {{ formatDateRange(project.startDate, project.endDate) }}
      </span>
    </div>

    <!-- 项目名称 -->
    <h3
      class="text-lg sm:text-xl font-semibold tracking-tight text-heading mb-2
             group-hover:text-heading transition-colors"
    >
      {{ project.name }}
    </h3>

    <!-- 项目描述（flex-grow 占中间，保证底部对齐） -->
    <p class="text-sm text-body leading-relaxed line-clamp-2 mb-4 flex-grow">
      {{ project.description }}
    </p>

    <!-- 技术栈 -->
    <div v-if="project.stacks && project.stacks.length > 0" class="mb-4">
      <div class="flex flex-wrap gap-1.5">
        <span
          v-for="stack in project.stacks"
          :key="stack.id"
          class="px-2 py-0.5 text-[11px] sm:text-xs r-sm font-medium"
          :style="{
            backgroundColor: withAlpha(stack.color, 0.12),
            color: stack.color || '#6b7280'
          }"
        >
          {{ stack.name }}
        </span>
      </div>
    </div>

    <!-- 底部：角色 + 外链 -->
    <div class="flex items-center justify-between gap-3 pt-2 mt-auto">
      <div v-if="project.role" class="min-w-0 shrink">
        <span class="text-[11px] sm:text-xs px-2 py-0.5 r-sm text-muted bg-subtle truncate max-w-[180px] block">
          角色：{{ project.role }}
        </span>
      </div>
      <div class="flex items-center gap-3 ml-auto shrink-0">
        <a
          v-if="project.projectUrl"
          :href="project.projectUrl"
          target="_blank"
          rel="noopener noreferrer"
          @click.stop
          class="text-xs sm:text-sm font-medium transition-opacity hover:opacity-80"
          :style="{ color: themeConfig.primaryColor }"
        >
          在线预览
        </a>
        <a
          v-if="project.repoUrl"
          :href="project.repoUrl"
          target="_blank"
          rel="noopener noreferrer"
          @click.stop
          class="text-xs sm:text-sm font-medium text-muted hover:text-heading transition-colors"
        >
          代码仓库
        </a>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import type { PostedProjectListItem } from '@/types'
import { formatDateRange } from '@/utils/formatDate'

const { themeConfig } = useTheme()

defineProps<{
  project: PostedProjectListItem
}>()

const getStatusText = (status: string): string => {
  const map: Record<string, string> = {
    active: '进行中',
    completed: '已完成',
    paused: '暂停'
  }
  return map[status] || status
}

/** 将颜色（hex 或空）转为带 alpha 的 rgba；避免在模板里堆叠字符串 */
const withAlpha = (hexColor?: string | null, alpha = 0.12): string => {
  if (!hexColor) return 'rgb(107 114 128 / 0.12)'
  const color = hexColor.replace('#', '')
  if (color.length === 3) {
    const r = parseInt(color[0] + color[0], 16)
    const g = parseInt(color[1] + color[1], 16)
    const b = parseInt(color[2] + color[2], 16)
    return `rgb(${r} ${g} ${b} / ${alpha})`
  }
  if (color.length === 6) {
    const r = parseInt(color.slice(0, 2), 16)
    const g = parseInt(color.slice(2, 4), 16)
    const b = parseInt(color.slice(4, 6), 16)
    return `rgb(${r} ${g} ${b} / ${alpha})`
  }
  return `rgb(107 114 128 / ${alpha})`
}
</script>
