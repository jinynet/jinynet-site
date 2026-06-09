<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NSpin, NCard, NSpace } from 'naive-ui'
import VideoPlayer from '@/components/frontend/VideoPlayer.vue'
import VideoCard from '@/components/frontend/VideoCard.vue'
import type { VideoDetail, VideoList } from '@/api/videos'
import { getPublicVideoById, getHotVideos } from '@/api/videos'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const { isDark } = useTheme()

const video = ref<VideoDetail | null>(null)
const hotVideos = ref<VideoList[]>([])
const loading = ref(true)
const likes = ref(0)
const isLiked = ref(false)

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatViewCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toString()
}

const toggleLike = () => {
  isLiked.value = !isLiked.value
  if (isLiked.value) {
    likes.value++
  } else {
    likes.value--
  }
}

onMounted(async () => {
  loading.value = true
  const id = route.params.id as string
  try {
    const [videoResponse, hotResponse] = await Promise.all([
      getPublicVideoById(id),
      getHotVideos(6)
    ])
    video.value = videoResponse.data
    if (video.value) {
      likes.value = video.value.likeCount ?? 0
    }
    hotVideos.value = hotResponse.data
  } catch (error) {
    console.error('Failed to fetch video:', error)
  } finally {
    loading.value = false
  }
  userStore.fetchUserInfo()
  userStore.fetchUserContacts()
})
</script>

<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4">
        <NSpin v-if="loading" size="large" class="flex justify-center py-12" />

        <div v-else-if="video" class="flex flex-col lg:flex-row gap-8">
          <!-- 视频播放区域 -->
          <div class="flex-1">
            <VideoPlayer :video="video" />
            
            <!-- 视频信息 -->
            <div class="mt-6">
              <h1 class="text-2xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">{{ video.title }}</h1>
              
              <div class="flex items-center gap-6 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                <span>{{ formatViewCount(video.viewCount ?? 0) }} 播放</span>
                <span>{{ formatDate(video.publishedAt ?? '') }}</span>
                <span v-if="video.category">{{ video.category.name }}</span>
              </div>

              <!-- 操作栏 -->
              <NSpace class="mt-4">
                <NButton
                  @click="toggleLike"
                  :type="isLiked ? 'error' : 'default'"
                  :bordered="!isLiked"
                >
                  <template #icon>
                    <svg class="w-5 h-5" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                    </svg>
                  </template>
                  <span>{{ formatViewCount(likes) }}</span>
                </NButton>
                
                <NButton type="default">
                  <template #icon>
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                    </svg>
                  </template>
                  <span>收藏</span>
                </NButton>
                
                <NButton type="default">
                  <template #icon>
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h5v2h-5v-2zm-2.5-10.5a3.5 3.5 0 110 7h-2a1 1 0 01-1-1v-1a3 3 0 00-3-3H6a1 1 0 01-1-1V7a5 5 0 0110 0v1a1 1 0 01-1 1h-2z"/>
                    </svg>
                  </template>
                  <span>分享</span>
                </NButton>
              </NSpace>

              <!-- 视频描述 -->
              <NCard v-if="video.description" class="mt-6" :class="isDark ? 'bg-gray-800' : ''">
                <p :class="isDark ? 'text-gray-300' : 'text-gray-600'">{{ video.description }}</p>
              </NCard>
            </div>
          </div>

          <!-- 侧边栏 -->
          <div class="w-full lg:w-80">
            <h3 class="font-semibold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">热门视频</h3>
            <div class="space-y-4">
              <VideoCard
                v-for="hotVideo in hotVideos"
                :key="hotVideo.id"
                :video="hotVideo"
              />
            </div>
          </div>
        </div>

        <!-- 视频未找到 -->
        <div v-else class="text-center py-12">
          <NSpin size="large" show-text="视频未找到" />
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>
