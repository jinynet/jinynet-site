<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold mb-2" :class="isDark ? 'text-white' : 'text-gray-900'">视频频道</h1>
          <p class="text-gray-500">探索我的 vlog 和教程视频</p>
        </div>

        <!-- 搜索和筛选 -->
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <NInput
            v-model:value="searchQuery"
            placeholder="搜索视频..."
            class="flex-1"
            @keyup.enter="handleSearch"
          />
          <NButton type="primary" :style="{ backgroundColor: themeConfig.primaryColor, borderColor: themeConfig.primaryColor }" @click="handleSearch">
            搜索
          </NButton>
        </div>

        <!-- 分类筛选 -->
        <div class="flex flex-wrap gap-2 mb-6">
          <NTag
            checkable
            :checked="selectedCategory === null"
            @update:checked="handleCategoryChange(null)"
            :type="selectedCategory === null ? 'primary' : 'default'"
            round
            class="transition-opacity hover:opacity-80 cursor-pointer"
          >
            全部
          </NTag>
          <NTag
            v-for="category in categories"
            :key="category.id"
            checkable
            :checked="selectedCategory === category.id"
            @update:checked="handleCategoryChange(category.id)"
            :type="selectedCategory === category.id ? 'primary' : 'default'"
            round
            class="transition-opacity hover:opacity-80 cursor-pointer"
          >
            {{ category.name }}
          </NTag>
        </div>

        <!-- 视频列表 -->
        <div v-if="loading" class="flex justify-center items-center py-12">
          <NSpin size="large" />
        </div>

        <div v-else-if="videos.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <VideoCard
            v-for="video in videos"
            :key="video.id"
            :video="video"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-12">
          <div class="text-gray-400">
            <Video class="w-16 h-16 mx-auto mb-4 opacity-50" />
            <p class="text-lg">暂无视频内容</p>
            <p class="text-sm mt-2">还没有发布任何视频</p>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="!loading && totalPages > 1" class="flex justify-center mt-8">
          <NSpace>
            <NButton
              @click="handlePageChange(currentPage - 1)"
              :disabled="currentPage === 1"
              size="small"
            >
              上一页
            </NButton>
            <span class="text-sm text-gray-500 px-4">
              {{ currentPage }} / {{ totalPages }}
            </span>
            <NButton
              @click="handlePageChange(currentPage + 1)"
              :disabled="currentPage === totalPages"
              size="small"
            >
              下一页
            </NButton>
          </NSpace>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NInput, NButton, NSpin, NSpace, NTag } from 'naive-ui'
import { Video } from '@vicons/tabler'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import VideoCard from '@/components/frontend/VideoCard.vue'
import type { VideoList, VideoCategory, VideoQuery } from '@/api/videos'
import { getPublicVideos, getVideoCategories } from '@/api/videos'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'

const { isDark, themeConfig } = useTheme()
const userStore = useUserStore()

const videos = ref<VideoList[]>([])
const categories = ref<VideoCategory[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(1)
const selectedCategory = ref<number | null>(null)
const searchQuery = ref('')

const fetchVideos = async () => {
  loading.value = true
  try {
    const params: VideoQuery = {
      pageIndex: currentPage.value,
      pageSize: pageSize.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    if (searchQuery.value) {
      params.title = searchQuery.value
    }
    const response = await getPublicVideos(params)
    const data = response.data
    videos.value = data?.rows || []
    if (data?.totalPageCount !== undefined) {
      totalPages.value = parseInt(data.totalPageCount.toString())
    } else if (data?.totalPages !== undefined) {
      totalPages.value = data.totalPages
    }
  } catch (error) {
    console.error('Failed to fetch videos:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await getVideoCategories()
    categories.value = response.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const handleCategoryChange = (categoryId: number | null) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  fetchVideos()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchVideos()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchVideos()
}

onMounted(() => {
  fetchVideos()
  fetchCategories()
  // 加载用户信息用于 Footer 显示
  userStore.fetchUserInfo()
  userStore.fetchUserContacts()
})
</script>
