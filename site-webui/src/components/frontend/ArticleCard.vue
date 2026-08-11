<template>
  <article class="card-elevated r-lg p-6 group" @click="handleClick">
    <!-- 顶部：分类 + 时间 -->
    <div class="flex items-start justify-between gap-4 mb-4">
      <span
        v-if="article.category"
        class="accent-pill shrink-0"
        :style="{
          backgroundColor: `${themeConfig.primaryColor}12`,
          color: themeConfig.primaryColor
        }"
      >
        {{ article.category.name }}
      </span>
      <span class="text-xs sm:text-sm text-faint whitespace-nowrap pt-0.5">
        {{ formatDate(article.publishedAt) }}
      </span>
    </div>

    <!-- 标题 -->
    <h3
      class="text-lg sm:text-xl font-semibold tracking-tight text-heading mb-2
             group-hover:text-heading transition-colors line-clamp-2"
    >
      {{ article.title }}
    </h3>

    <!-- 摘要 -->
    <p class="text-sm text-body leading-relaxed line-clamp-2 mb-5 min-h-[2.5rem]">
      {{ article.excerpt }}
    </p>

    <!-- 底部：标签 + 阅读量 -->
    <div class="flex items-end justify-between gap-4">
      <div class="flex flex-wrap gap-1.5 min-h-[1.5rem]" v-if="displayTags.length">
        <span
          v-for="tag in displayTags.slice(0, 3)"
          :key="tag.id"
          class="px-2 py-0.5 text-[11px] sm:text-xs r-sm text-muted bg-subtle"
        >
          #{{ tag.name }}
        </span>
      </div>
      <div class="flex items-center gap-1 text-xs sm:text-sm text-faint shrink-0">
        <Eye class="w-3.5 h-3.5" />
        <span class="tabular-nums">{{ article.viewCount }}</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Eye } from '@/icons'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import type { ArticleCardItem } from '@/types'
import { formatDate } from '@/utils/formatDate'

const router = useRouter()
const { themeConfig, isDark: _unused_isDark } = useTheme()

const props = defineProps<{
  article: ArticleCardItem
}>()

/** 将 tags（可能是 string[] 或 ArticleTag[]）统一为 { id, name } 格式 */
const displayTags = computed(() => {
  if (!props.article.tags) return []
  return props.article.tags.map(tag =>
    typeof tag === 'string' ? { id: tag, name: tag } : tag
  )
})

const handleClick = () => {
  router.push(`/articles/${props.article.id}`)
}
</script>
