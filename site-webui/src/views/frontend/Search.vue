<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4">
        <div class="mb-8">
          <h1 class="text-3xl font-bold mb-6" :class="isDark ? 'text-white' : 'text-gray-900'">搜索</h1>
          
          <div class="flex gap-4 mb-6">
            <n-input
              v-model:value="keyword"
              placeholder="输入关键词搜索..."
              size="large"
              class="flex-1"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <Search class="w-5 h-5" />
              </template>
            </n-input>
            <n-button
              type="primary"
              size="large"
              :style="{ backgroundColor: themeConfig.primaryColor, borderColor: themeConfig.primaryColor }"
              @click="handleSearch"
            >
              <Search class="w-5 h-5 mr-2" />
              搜索
            </n-button>
          </div>

          <n-radio-group v-model:value="searchType" @update:value="handleSearch">
            <n-radio-button value="all">全部</n-radio-button>
            <n-radio-button value="article">文章</n-radio-button>
            <n-radio-button value="project">项目</n-radio-button>
          </n-radio-group>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-12">
          <n-spin size="large" />
        </div>

        <div v-else-if="searchResults.length > 0" class="space-y-6">
          <div
            v-for="result in searchResults"
            :key="`${result.type}-${result.id}`"
            class="p-6 rounded-xl shadow-md hover:shadow-lg transition-shadow cursor-pointer"
            :class="isDark ? 'bg-gray-800' : 'bg-white'"
            @click="handleResultClick(result)"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center gap-2">
                <n-tag :type="result.type === 'article' ? 'info' : 'success'" size="small">
                  {{ result.type === 'article' ? '文章' : '项目' }}
                </n-tag>
                <n-tag v-if="result.categoryName" size="small" :class="isDark ? 'bg-gray-700 text-gray-300' : ''">
                  {{ result.categoryName }}
                </n-tag>
              </div>
              <div class="flex items-center gap-1 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                <span>相关度: {{ Math.min(result.score * 100, 100).toFixed(0) }}%</span>
              </div>
            </div>
            
            <h2 class="text-xl font-bold mb-2 transition-colors" :class="isDark ? 'text-white hover:text-gray-300' : 'text-gray-900'" v-html="result.title"></h2>
            
            <p
              v-if="result.excerpt"
              class="mb-3 line-clamp-2"
              :class="isDark ? 'text-gray-400' : 'text-gray-600'"
              v-html="result.excerpt"
            ></p>
            
            <p
              v-else-if="result.description"
              class="mb-3 line-clamp-2"
              :class="isDark ? 'text-gray-400' : 'text-gray-600'"
              v-html="result.description"
            ></p>
            
            <p
              v-if="result.content"
              class="mb-3 line-clamp-3"
              :class="isDark ? 'text-gray-400' : 'text-gray-600'"
              v-html="result.content"
            ></p>
            
            <div class="flex items-center justify-between">
              <div class="flex flex-wrap gap-2">
                <n-tag
                  v-if="result.tags"
                  size="small"
                  :class="isDark ? 'bg-gray-700 text-gray-300' : ''"
                >
                  {{ result.tags }}
                </n-tag>
                <n-tag
                  v-if="result.stacks"
                  size="small"
                  :class="isDark ? 'bg-gray-700 text-gray-300' : ''"
                >
                  {{ result.stacks }}
                </n-tag>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-center mt-8">
            <n-button
              v-if="hasMore"
              type="primary"
              ghost
              @click="loadMore"
              :loading="loadingMore"
            >
              加载更多
            </n-button>
          </div>
        </div>

        <div v-else-if="hasSearched" class="text-center py-12">
          <div class="text-6xl mb-4">🔍</div>
          <p class="text-xl mb-2" :class="isDark ? 'text-gray-400' : 'text-gray-600'">未找到相关内容</p>
          <p class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">请尝试其他关键词</p>
        </div>

        <div v-else class="text-center py-12">
          <div class="text-6xl mb-4">📚</div>
          <p class="text-xl mb-2" :class="isDark ? 'text-gray-400' : 'text-gray-600'">搜索文章和项目</p>
          <p class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">输入关键词开始搜索</p>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'Search'
})

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@/icons'
import { NInput, NButton, NTag, NSpin, NRadioGroup, NRadioButton } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import { searchAll, searchArticles, searchProjects, type SearchResult } from '@/api/search'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const { isDark, themeConfig } = useTheme()
const userStore = useUserStore()

const keyword = ref('')
const searchType = ref<'all' | 'article' | 'project'>('all')
const searchResults = ref<SearchResult[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasSearched = ref(false)
const hasMore = ref(false)
const currentLimit = ref(20)

// 缓存状态
const previousKeyword = ref('')
const previousSearchType = ref<'all' | 'article' | 'project'>('all')

const handleResultClick = (result: SearchResult) => {
  // 保存当前搜索状态
  previousKeyword.value = keyword.value
  previousSearchType.value = searchType.value
  
  if (result.type === 'article') {
    router.push(`/articles/id/${result.id}`)
  } else if (result.type === 'project') {
    router.push(`/projects/${result.id}`)
  }
}

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    searchResults.value = []
    hasSearched.value = false
    return
  }
  loading.value = true
  hasSearched.value = true
  currentLimit.value = 20

  try {
    let response: any = null
    
    if (searchType.value === 'all') {
      response = await searchAll(keyword.value, currentLimit.value)
    } else if (searchType.value === 'article') {
      response = await searchArticles(keyword.value, currentLimit.value)
    } else if (searchType.value === 'project') {
      response = await searchProjects(keyword.value, currentLimit.value)
    }
    
    // API直接返回数组，而不是 { data: [...] } 格式
    const data = response?.data || []
    searchResults.value = Array.isArray(data) ? data : (data?.data || [])
    hasMore.value = searchResults.value.length >= currentLimit.value
  } catch (error) {
    console.error('Search failed:', error)
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  loadingMore.value = true
  currentLimit.value += 20
  try {
    let response: any = null
    
    if (searchType.value === 'all') {
      response = await searchAll(keyword.value, currentLimit.value)
    } else if (searchType.value === 'article') {
      response = await searchArticles(keyword.value, currentLimit.value)
    } else if (searchType.value === 'project') {
      response = await searchProjects(keyword.value, currentLimit.value)
    }
    
    // API直接返回数组，而不是 { data: [...] } 格式
    const data = response?.data || []
    searchResults.value = Array.isArray(data) ? data : (data?.data || [])
    hasMore.value = searchResults.value.length >= currentLimit.value
  } catch (error) {
    console.error('Load more failed:', error)
  } finally {
    loadingMore.value = false
  }
}

onMounted(() => {
  // 加载用户信息用于 Footer 显示
  userStore.fetchUserInfo()
  userStore.fetchUserContacts()
})

</script>
