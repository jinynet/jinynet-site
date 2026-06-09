<script setup lang="ts">
import type { VideoList } from '@/api/videos'
import { useRouter } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { ref, onMounted } from 'vue'

defineProps<{
  video: VideoList
}>()

const router = useRouter()
const { themeConfig } = useTheme()
const themeMode = ref('light')

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const goToVideo = (video: VideoList) => {
  router.push(`/videos/${video.id}`)
}

onMounted(() => {
  themeMode.value = themeConfig.value.themeMode
})
</script>

<template>
  <div
    class="video-card group rounded-lg overflow-hidden cursor-pointer transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
    :class="themeMode === 'dark' ? 'bg-gray-800' : 'bg-white'"
    @click="goToVideo(video)"
  >
    <div class="relative aspect-video overflow-hidden">
      <img
        :src="video.coverUrl || '/jinynet-rings.svg'"
        :alt="video.title"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
      <div class="absolute bottom-2 right-2 px-2 py-0.5 bg-black/70 rounded text-xs text-white font-medium">
        {{ formatDuration(video.duration ?? 0) }}
      </div>
      <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
        <div class="w-14 h-14 rounded-full bg-white/90 flex items-center justify-center shadow-lg">
          <svg class="w-8 h-8 ml-1" fill="currentColor" viewBox="0 0 24 24" :style="{ color: themeConfig.primaryColor }">
            <path d="M8 5v14l11-7z"/>
          </svg>
        </div>
      </div>
    </div>
    <div class="p-4">
      <h3 class="font-semibold line-clamp-2 mb-2" :class="themeMode === 'dark' ? 'text-white' : 'text-gray-900'">
        {{ video.title }}
      </h3>
      <p v-if="video.description" class="text-sm line-clamp-2 mb-3" :class="themeMode === 'dark' ? 'text-gray-400' : 'text-gray-600'">
        {{ video.description }}
      </p>
      <div class="flex items-center justify-between text-xs" :class="themeMode === 'dark' ? 'text-gray-400' : 'text-gray-500'">
        <span v-if="video.categoryName" class="px-2 py-0.5 rounded-full" :style="{ backgroundColor: `${themeConfig.primaryColor}10`, color: themeConfig.primaryColor }">
          {{ video.categoryName }}
        </span>
        <span v-else>&nbsp;</span>
        <span>{{ formatViewCount(video.viewCount ?? 0) }} 播放</span>
      </div>
      <div class="mt-2 text-xs" :class="themeMode === 'dark' ? 'text-gray-500' : 'text-gray-400'">
        {{ formatDate(video.publishedAt ?? '') }}
      </div>
    </div>
  </div>
</template>
