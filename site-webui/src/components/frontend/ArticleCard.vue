<template>
  <div 
    class="rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 cursor-pointer group" 
    :class="isDark ? 'bg-gray-800' : 'bg-white'"
    @click="handleClick"
  >
    <div class="flex items-start justify-between mb-4">
      <span 
        v-if="article.category" 
        class="px-3 py-1 text-sm rounded-full" 
        :style="{ backgroundColor: `${themeConfig.primaryColor}10`, color: themeConfig.primaryColor }"
      >
        {{ article.category.name }}
      </span>
      <span class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">{{ formatDate(article.publishedAt) }}</span>
    </div>
    <h3 
      class="text-xl font-bold mb-2 group-hover:transition-colors" 
      :class="isDark ? 'text-white' : 'text-gray-800'"
    >
      {{ article.title }}
    </h3>
    <p class="text-sm mb-4 line-clamp-2" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ article.excerpt }}</p>
    <div class="flex items-center justify-between">
      <div class="flex flex-wrap gap-2" v-if="article.tags">
        <span
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          class="px-2 py-1 text-xs rounded"
          :class="isDark ? 'bg-gray-700 text-gray-400' : 'bg-gray-100 text-gray-500'"
        >
          {{ tag.name }}
        </span>
      </div>
      <div class="flex items-center gap-1 text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-500'">
        <Eye class="w-4 h-4" />
        <span>{{ article.viewCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Eye } from '@/icons'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'

const router = useRouter()
const { themeConfig, isDark } = useTheme()

interface Article {
  id: number | string
  title: string
  excerpt: string | null
  publishedAt: string | null
  viewCount: number
  category?: { name: string } | null
  tags?: Array<{ id?: number | string; name: string }>
}

const props = defineProps<{
  article: Article
}>()

const handleClick = () => {
  router.push(`/articles/${props.article.id}`)
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>
