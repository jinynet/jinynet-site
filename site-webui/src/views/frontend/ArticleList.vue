<template>
  <div class="min-h-screen bg-page">
    <Header />

    <main class="pt-24 sm:pt-28 pb-14 sm:pb-16">
      <div class="max-w-6xl mx-auto px-4 sm:px-6">
        <div class="flex flex-col lg:flex-row gap-6 lg:gap-10">
          <!-- 主内容：搜索 + 列表 + 分页 -->
          <div class="flex-1 min-w-0">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-6 sm:mb-8 gap-4">
              <div>
                <h1 class="text-xl sm:text-2xl font-bold tracking-tight text-heading mb-1">
                  技术文章
                </h1>
                <p class="text-sm text-muted">共 <span class="tabular-nums text-body">{{ articles.length }}</span> 篇内容</p>
              </div>
              <div class="flex items-center gap-2">
                <n-input
                  v-model:value="keyword"
                  placeholder="搜索文章..."
                  class="w-full sm:w-64"
                  clearable
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <Search class="w-4 h-4" />
                  </template>
                </n-input>
                <n-button
                  type="primary"
                  :style="{ backgroundColor: primaryColor, borderColor: primaryColor }"
                  @click="handleSearch"
                >
                  <template #icon>
                    <Search class="w-[18px] h-[18px]" />
                  </template>
                  <span class="hidden sm:inline">搜索</span>
                </n-button>
              </div>
            </div>

            <!-- 文章列表（使用 card-elevated 规范） -->
            <div class="space-y-4 sm:space-y-5">
              <article
                v-for="article in articles"
                :key="article.id"
                class="card-elevated r-lg p-5 sm:p-6 cursor-pointer"
                @click="router.push(`/articles/id/${article.id}`)"
              >
                <div class="flex items-start justify-between gap-4 mb-3">
                  <span
                    v-if="getCategoryName(article)"
                    class="accent-pill shrink-0"
                    :style="{
                      backgroundColor: `${primaryColor}12`,
                      color: primaryColor
                    }"
                  >
                    {{ getCategoryName(article) }}
                  </span>
                  <span class="text-xs sm:text-sm text-faint whitespace-nowrap pt-0.5">
                    {{ formatDate(article.publishedAt || article.updatedAt) }}
                  </span>
                </div>
                <h2 class="text-lg sm:text-xl font-semibold tracking-tight text-heading mb-2 line-clamp-2">
                  {{ article.title }}
                </h2>
                <p class="text-sm sm:text-[15px] text-body leading-relaxed line-clamp-2 mb-4 min-h-[2.5rem]">
                  {{ article.excerpt || '（暂无摘要）点击查看完整内容。' }}
                </p>
                <div class="flex items-center justify-between gap-4">
                  <div class="flex flex-wrap gap-1.5 min-h-[1.5rem]" v-if="getArticleTags(article).length">
                    <span
                      v-for="tag in getArticleTags(article).slice(0, 5)"
                      :key="tag.id"
                      class="px-2 py-0.5 text-[11px] sm:text-xs r-sm text-muted bg-subtle"
                    >
                      #{{ tag.name }}
                    </span>
                  </div>
                  <div class="flex items-center gap-1 text-xs sm:text-sm text-faint shrink-0 ml-auto">
                    <Eye class="w-3.5 h-3.5" />
                    <span class="tabular-nums">{{ article.viewCount }}</span>
                  </div>
                </div>
              </article>

              <!-- 空状态 -->
              <div
                v-if="articles.length === 0 && !isLoading"
                class="r-lg bg-card border border-base border-dashed p-12 text-center"
              >
                <p class="text-muted mb-1">暂无匹配的文章</p>
                <p class="text-xs text-faint">试试其他关键词或筛选条件</p>
              </div>
            </div>

            <!-- 分页 -->
            <div class="flex items-center justify-center mt-8 sm:mt-10">
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

          <!-- 侧边栏：分类 + 标签 -->
          <aside class="lg:w-64 shrink-0 space-y-4 sm:space-y-5">
            <NCard title="文章分类" size="small" :bordered="false" class="!r-lg !shadow-card !border !border-base">
              <div class="space-y-1">
                <button
                  type="button"
                  class="w-full flex items-center justify-between px-3 py-2 r-sm cursor-pointer
                         transition-colors text-sm text-left
                         border-none outline-none appearance-none bg-transparent
                         hover:bg-card-hover"
                  :class="selectedCategoryId === null
                    ? 'font-medium bg-card-hover text-heading'
                    : 'text-body'"
                  @click="filterByCategory(null)"
                >
                  <span>全部</span>
                </button>
                <button
                  v-for="category in categories"
                  :key="category.id"
                  type="button"
                  class="w-full flex items-center justify-between px-3 py-2 r-sm cursor-pointer
                         transition-colors text-sm text-left
                         border-none outline-none appearance-none bg-transparent
                         hover:bg-card-hover"
                  :class="selectedCategoryId === category.id
                    ? 'font-medium bg-card-hover text-heading'
                    : 'text-body'"
                  @click="filterByCategory(category.id)"
                >
                  <span>{{ category.name }}</span>
                </button>
              </div>
            </NCard>

            <NCard
              v-if="tags.length > 0"
              title="热门标签"
              size="small"
              :bordered="false"
              class="!r-lg !shadow-card !border !border-base"
            >
              <div class="flex flex-wrap gap-1.5">
                <button
                  v-for="tag in tags"
                  :key="tag.id"
                  type="button"
                  class="px-2.5 py-1 text-xs r-pill transition-colors
                         border-none outline-none appearance-none
                         hover:bg-card-hover"
                  :class="selectedTagId === tag.id
                    ? 'text-on-primary'
                    : 'text-body bg-subtle'"
                  :style="selectedTagId === tag.id
                    ? { backgroundColor: primaryColor }
                    : undefined"
                  @click="filterByTag(tag.id)"
                >
                  {{ tag.name }}
                </button>
              </div>
            </NCard>
          </aside>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Eye } from '@/icons'
import { NInput, NButton, NPagination, NCard } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import { getPostedArticles, getPostedArticleCategories, getPostedArticleTags } from '@/api/public'
import { useTheme } from '@/composables/useTheme'
import type { ArticleCardItem } from '@/types'
import { formatDate } from '@/utils/formatDate'

const router = useRouter()
const { themeConfig } = useTheme()

const primaryColor = computed(() => themeConfig.value.primaryColor)
const isLoading = computed(() => false)

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

const articles = ref<ArticleCardItem[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

/** 将文章 tags（可能是 string[] 或 ArticleTag[]）统一为 { id, name } 格式 */
const getCategoryName = (article: ArticleCardItem): string | undefined =>
  article.category?.name ?? article.categoryName

const getArticleTags = (article: ArticleCardItem) => {
  if (!article.tags) return []
  return article.tags.map(tag =>
    typeof tag === 'string' ? { id: tag, name: tag } : tag
  )
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
