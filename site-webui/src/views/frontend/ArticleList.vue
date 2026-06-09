<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4">
        <div class="flex flex-col lg:flex-row gap-8">
          <div class="flex-1">
            <div class="flex items-center justify-between mb-6">
              <h1 class="text-2xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">技术文章</h1>
              <div class="flex items-center gap-2">
                <n-input
                  v-model:value="keyword"
                  placeholder="搜索文章..."
                  class="w-64"
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <Search class="w-4 h-4" />
                  </template>
                </n-input>
                <n-button
                  type="primary"
                  :style="{ backgroundColor: themeConfig.primaryColor, borderColor: themeConfig.primaryColor }"
                  @click="handleSearch"
                >
                  <Search class="w-5 h-5" />
                </n-button>
              </div>
            </div>
            
            <div class="space-y-6">
              <div
                v-for="article in articles"
                :key="article.id"
                class="p-6 rounded-xl shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                :class="isDark ? 'bg-gray-800' : 'bg-white'"
                @click="router.push(`/articles/id/${article.id}`)"
              >
                <div class="flex items-start justify-between mb-3">
                  <n-tag type="primary" v-if="article.category">{{ article.category.name }}</n-tag>
                  <span class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">{{ formatDate(article.publishedAt || article.updatedAt) }}</span>
                </div>
                <h2 class="text-xl font-bold mb-2 transition-colors" :class="isDark ? 'text-white hover:text-gray-300' : 'text-gray-900'" :style="{ '--hover-color': primaryColor }">{{ article.title }}</h2>
                <p class="mb-4 line-clamp-2" :class="isDark ? 'text-gray-400' : 'text-gray-600'">{{ article.excerpt }}</p>
                <div class="flex items-center justify-between">
                  <div class="flex flex-wrap gap-2" v-if="article.tags">
                    <n-tag
                      v-for="tag in article.tags"
                      :key="tag.id"
                      size="small"
                      :class="isDark ? 'bg-gray-700 text-gray-300' : ''"
                    >
                      {{ tag.name }}
                    </n-tag>
                  </div>
                  <div class="flex items-center gap-1 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                    <Eye class="w-4 h-4" />
                    <span>{{ article.viewCount }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-center mt-8">
              <n-pagination
                v-model:page="currentPage"
                :page-size="pageSize"
                :page-count="totalPages"
                show-size-picker
                :page-sizes="[10, 20, 30, 40]"
                @update:page="handlePageChange"
                @update:page-size="handlePageSizeChange"
              />
            </div>
          </div>

          <div class="lg:w-64">
            <n-card title="文章分类" class="mb-6" :class="isDark ? 'bg-gray-800' : ''">
              <div class="space-y-2">
                <div
                  class="flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors"
                  :class="[
                    selectedCategoryId === null ? 'font-medium bg-gray-100' : '',
                    isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-gray-100'
                  ]"
                  :style="selectedCategoryId === null ? { color: primaryColor } : {}"
                  @click="filterByCategory(null)"
                >
                  <span>全部</span>
                </div>
                <div
                  v-for="category in categories"
                  :key="category.id"
                  class="flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors"
                  :class="[
                    selectedCategoryId === category.id ? 'font-medium bg-gray-100' : '',
                    isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-gray-100'
                  ]"
                  :style="selectedCategoryId === category.id ? { color: primaryColor } : {}"
                  @click="filterByCategory(category.id)"
                >
                  <span>{{ category.name }}</span>
                </div>
              </div>
            </n-card>

            <n-card title="热门标签" v-if="tags.length > 0" :class="isDark ? 'bg-gray-800' : ''">
              <div class="flex flex-wrap gap-2">
                <n-tag
                  v-for="tag in tags"
                  :key="tag.id"
                  :type="selectedTagId === tag.id ? 'primary' : 'default'"
                  :class="selectedTagId !== tag.id && isDark ? 'bg-gray-700 text-gray-300' : ''"
                  @click="filterByTag(tag.id)"
                >
                  {{ tag.name }}
                </n-tag>
              </div>
            </n-card>
          </div>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Eye } from '@/icons'
import { NInput, NButton, NTag, NPagination, NCard } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import { getPostedArticles, getPostedArticleCategories, getPostedArticleTags } from '@/api/public'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const { themeConfig, isDark } = useTheme()
const userStore = useUserStore()

const primaryColor = ref('#111827')

onMounted(() => {
  primaryColor.value = themeConfig.value.primaryColor
  // 加载用户信息用于 Footer 显示
  userStore.fetchUserInfo()
  userStore.fetchUserContacts()
})

interface Article {
  id: number | string
  title: string
  excerpt: string | null
  coverImage: string | null
  viewCount: number
  likeCount: number
  publishedAt: string | null
  updatedAt: string | null
  category?: { id: number | string; name: string } | null
  tags?: Array<{ id?: number | string; name: string }>
}

interface Category {
  id: number | string
  name: string
}

interface Tag {
  id: number | string
  name: string
}

const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const selectedCategoryId = ref<number | null>(null)
const selectedTagId = ref<number | null>(null)

const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const fetchArticles = async () => {
  try {
    const res = await getPostedArticles({
      pageIndex: currentPage.value,
      pageSize: pageSize.value,
      title: keyword.value || undefined,
      categoryId: selectedCategoryId.value || undefined,
      tagId: selectedTagId.value || undefined
    })
    const data = res.data
    articles.value = data?.rows || []
    if (data?.totalPageCount !== undefined) {
      totalPages.value = parseInt(data.totalPageCount.toString())
    } else if (data?.totalPages !== undefined) {
      totalPages.value = data.totalPages
    }
  } catch (error) {
    console.error('Failed to fetch articles:', error)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getPostedArticleCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getPostedArticleTags()
    tags.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch tags:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchArticles()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchArticles()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchArticles()
}

const filterByCategory = (id: number | string | null) => {
  selectedCategoryId.value = id as number | null
  currentPage.value = 1
  fetchArticles()
}

const filterByTag = (id: number | string | null) => {
  // 如果点击的是已选中的标签，则取消选中
  if (selectedTagId.value === id) {
    selectedTagId.value = null
  } else {
    selectedTagId.value = id as number | null
  }
  currentPage.value = 1
  fetchArticles()
}

onMounted(() => {
  fetchArticles()
  fetchCategories()
  fetchTags()
})
</script>
