<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4" v-if="article">
        <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
          <div class="lg:col-span-3">
            <n-card class="mb-6" :class="isDark ? 'bg-gray-800' : ''">
              <div class="flex items-start justify-between mb-4">
                <n-tag type="primary" v-if="article.category">{{ article.category.name }}</n-tag>
                <span class="text-sm" :class="isDark ? 'text-gray-500' : 'text-gray-400'">{{ formatDateLong(article.publishedAt) }}</span>
              </div>
              <h1 class="text-3xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-gray-900'">{{ article.title }}</h1>
              <div class="flex items-center gap-4 text-sm mb-6" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                <span class="flex items-center gap-1">
                  <Eye class="w-4 h-4" />
                  {{ article.viewCount }}
                </span>
                <span class="flex items-center gap-1">
                  <Heart class="w-4 h-4" />
                  {{ article.likeCount }}
                </span>
              </div>
              <div class="flex flex-wrap gap-2 mb-6" v-if="article.tags">
                <n-tag
                  v-for="tag in article.tags"
                  :key="tag.id"
                  size="small"
                  :class="isDark ? 'bg-gray-700 text-gray-300' : ''"
                >
                  {{ tag.name }}
                </n-tag>
              </div>
              <div class="prose prose-lg max-w-none" :class="isDark ? 'prose-invert' : ''" v-html="renderedContent"></div>
            </n-card>
          </div>

          <div class="hidden lg:block lg:col-span-1">
            <n-card title="文章目录" class="sticky top-24 max-h-[calc(100vh-180px)] overflow-hidden" v-if="tocItems.length > 0" :class="isDark ? 'bg-gray-800' : ''">
              <template #header-extra>
                <span class="text-xs" :class="isDark ? 'text-gray-500' : 'text-gray-400'">{{ tocItems.length }} 节</span>
              </template>
              <nav class="space-y-1 overflow-y-auto max-h-[calc(100vh-240px)] pr-1 custom-scrollbar">
                  <a
                    v-for="item in tocItems"
                    :key="item.id"
                    :href="'#' + item.id"
                    class="block py-1 text-sm transition-colors"
                    :class="[
                      activeHeading === item.id
                        ? 'font-semibold bg-gray-100 rounded px-2 -mx-2'
                        : isDark ? 'text-gray-400 hover:text-white hover:bg-gray-700 rounded px-2 -mx-2' : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded px-2 -mx-2'
                    ]"
                    :style="[
                      { paddingLeft: (item.level - 1) * 12 + 8 + 'px' },
                      activeHeading === item.id ? { color: primaryColor } : {}
                    ]"
                    @click.prevent="scrollToHeading(item.id)"
                  >
                    {{ item.text }}
                  </a>
                </nav>
            </n-card>
          </div>
        </div>
      </div>

      <!-- 文章不存在或未发布时显示 -->
      <div class="max-w-6xl mx-auto px-4" v-else>
        <n-card class="text-center py-16">
          <div class="text-6xl mb-4">404</div>
          <h2 class="text-2xl font-bold text-gray-800 mb-2">文章不存在</h2>
          <p class="text-gray-500 mb-6">该文章可能尚未发布或已被删除</p>
          <n-button type="primary" @click="router.push('/')">返回首页</n-button>
        </n-card>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Eye, Heart } from '@/icons'
import { NCard, NTag, NButton } from 'naive-ui'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import { getPostedArticleById, getPostedArticleBySlug } from '@/api/public'
import { getArticleById } from '@/api/articles'
import { useTheme } from '@/composables/useTheme'
import type { ArticleDetail } from '@/types'
import { formatDateLong } from '@/utils/formatDate'

const router = useRouter()
const route = useRoute()
const { themeConfig, isDark } = useTheme()

const primaryColor = computed(() => themeConfig.value.primaryColor)

type Article = ArticleDetail

interface TocItem {
  id: string
  text: string
  level: number
}

const article = ref<Article | null>(null)
const renderedContent = ref('')
const tocItems = ref<TocItem[]>([])
const activeHeading = ref('')
const isAdminPreview = computed(() => route.meta.adminPreview === true)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' + hljs.highlight(str, { language: lang }).value + '</code></pre>'
      } catch (__) {}
    }
    // 如果没有语言或者语言不支持，返回转义后的代码
    return '<pre class="hljs"><code>' + escapeHtml(str) + '</code></pre>'
  }
})
md.use(markdownItAnchor)

const escapeHtml = (str: string): string => {
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

const extractToc = (content: string) => {
  const headings: TocItem[] = []
  const lines = content.split('\n')
  let inCodeBlock = false
  const usedIds = new Map<string, number>()
  
  for (const line of lines) {
    // 检测代码块开始或结束（``` 标记）
    if (line.trim().startsWith('```')) {
      inCodeBlock = !inCodeBlock
      continue
    }
    
    // 如果在代码块内，跳过此行
    if (inCodeBlock) {
      continue
    }
    
    // 匹配标题（只在非代码块内进行匹配）
    const headingMatch = line.match(/^#{1,6}\s+(.+)$/)
    if (headingMatch) {
      const level = line.match(/^#{1,6}/)?.[0].length || 1
      const text = headingMatch[1].trim()
      const id = createUniqueHeadingId(text, usedIds)
      headings.push({ id, text, level })
    }
  }
  tocItems.value = headings
}

const createUniqueHeadingId = (text: string, usedIds: Map<string, number>): string => {
  const baseId = slugify(text) || 'heading'
  const usedCount = usedIds.get(baseId) || 0
  usedIds.set(baseId, usedCount + 1)
  return usedCount === 0 ? baseId : `${baseId}-${usedCount + 1}`
}

const slugify = (text: string): string => {
  return text
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

const renderMarkdown = () => {
  if (article.value?.content) {
    const result = md.render(article.value.content)
    renderedContent.value = result
    extractToc(article.value.content)
    setTimeout(() => {
      const headings = document.querySelectorAll('.prose h1, .prose h2, .prose h3, .prose h4, .prose h5, .prose h6')
      headings.forEach((heading, index) => {
        const tocItem = tocItems.value[index]
        if (!tocItem) return
        heading.id = tocItem.id
        ;(heading as HTMLElement).style.scrollMarginTop = '96px'
      })
      // 默认激活第一个目录项
      if (tocItems.value.length > 0) {
        activeHeading.value = tocItems.value[0].id
      }
    }, 100)
  }
}

const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
    activeHeading.value = id
  }
}

const handleScroll = () => {
  const headerHeight = 96
  const scrollPosition = window.scrollY + headerHeight
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  let currentHeading = ''
  const headings = document.querySelectorAll('.prose h1, .prose h2, .prose h3, .prose h4, .prose h5, .prose h6')
  
  if (headings.length === 0) {
    return
  }
  
  // 检查是否滚动到页面底部（最后150px）
  const isAtBottom = scrollPosition + windowHeight >= documentHeight - 150
  
  if (isAtBottom) {
    // 滚动到底部时激活最后一个标题
    currentHeading = headings[headings.length - 1].id
  } else {
    // 找到当前可视区域内最上方的标题
    let closestHeading = ''
    let minDistance = Infinity
    
    headings.forEach((heading) => {
      const rect = heading.getBoundingClientRect()
      const headingTop = rect.top
      
      // 标题顶部距离视口顶部的距离（考虑header高度）
      const distance = headingTop - headerHeight
      
      // 找到刚刚进入或即将进入视口的标题（距离在-100到500之间）
      if (distance >= -100 && distance <= 500 && distance < minDistance) {
        minDistance = distance
        closestHeading = heading.id
      }
    })
    
    // 如果没有找到合适的标题，使用备选逻辑
    if (!closestHeading) {
      headings.forEach((heading) => {
        const rect = heading.getBoundingClientRect()
        const offsetTop = window.scrollY + rect.top
        
        if (offsetTop <= scrollPosition + 100) {
          currentHeading = heading.id
        }
      })
    } else {
      currentHeading = closestHeading
    }
  }
  
  // 只有找到有效的标题才更新激活状态
  if (currentHeading) {
    activeHeading.value = currentHeading
  }
}

const fetchArticle = async () => {
  try {
    const id = route.params.id
    if (isAdminPreview.value && id && typeof id === 'string') {
      const res = await getArticleById(id)
      article.value = res.data || null
      renderMarkdown()
    } else if (id && typeof id === 'string') {
      const res = await getPostedArticleById(id)
      article.value = res.data || null
      renderMarkdown()
    } else if (route.params.slug && typeof route.params.slug === 'string') {
      const res = await getPostedArticleBySlug(route.params.slug)
      article.value = res.data || null
      renderMarkdown()
    }
  } catch (error) {
    console.error('Failed to fetch article:', error)
    article.value = null
  }
}

watch(() => [route.params.id, route.params.slug, route.meta.adminPreview], () => {
  fetchArticle()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 文章内容基础样式 */
.prose {
  color: #374151;
  line-height: 1.8;
  font-size: 16px;
}

.prose :deep(h1) {
  font-size: 2em;
  font-weight: 700;
  margin: 1.5em 0 0.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #e5e7eb;
  color: #111827;
}

.prose :deep(h2) {
  font-size: 1.5em;
  font-weight: 600;
  margin: 1.25em 0 0.5em;
  padding-bottom: 0.25em;
  border-bottom: 1px solid #f3f4f6;
  color: #1f2937;
}

.prose :deep(h3) {
  font-size: 1.25em;
  font-weight: 600;
  margin: 1em 0 0.5em;
  color: #374151;
}

.prose :deep(h4),
.prose :deep(h5),
.prose :deep(h6) {
  font-size: 1em;
  font-weight: 600;
  margin: 0.75em 0 0.5em;
  color: #4b5563;
}

/* 段落样式 */
.prose :deep(p) {
  margin: 1em 0;
}

/* 列表样式 */
.prose :deep(ul),
.prose :deep(ol) {
  margin: 1em 0;
  padding-left: 2em;
}

.prose :deep(ul) {
  list-style-type: disc;
}

.prose :deep(ol) {
  list-style-type: decimal;
}

.prose :deep(li) {
  margin: 0.5em 0;
  padding-left: 0.5em;
}

.prose :deep(ul ul),
.prose :deep(ol ul),
.prose :deep(ul ol),
.prose :deep(ol ol) {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.prose :deep(ul ul) {
  list-style-type: circle;
}

.prose :deep(ul ul ul) {
  list-style-type: square;
}

/* 代码块样式 - 清爽主题 */
.prose pre,
:deep(.hljs) {
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 1.5em 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.7;
  background-color: #1e1e1e !important;
  border: 1px solid #3d3d3d;
}

.prose pre code,
:deep(.hljs code) {
  background-color: transparent !important;
  padding: 0;
}

.prose code:not(pre code) {
  background-color: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-size: 0.875em;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  color: #d73a49;
}

/* 代码高亮配色 - 深色风格 */
:deep(.hljs) {
  color: #d4d4d4;
  background-color: #1e1e1e !important;
}

:deep(.hljs-keyword),
:deep(.hljs-selector-tag),
:deep(.hljs-built_in),
:deep(.hljs-name),
:deep(.hljs-tag) {
  color: #569cd6;
}

:deep(.hljs-string),
:deep(.hljs-title),
:deep(.hljs-section),
:deep(.hljs-attribute),
:deep(.hljs-literal),
:deep(.hljs-template-tag),
:deep(.hljs-template-variable),
:deep(.hljs-type),
:deep(.hljs-addition) {
  color: #ce9178;
}

:deep(.hljs-comment),
:deep(.hljs-quote),
:deep(.hljs-deletion),
:deep(.hljs-meta) {
  color: #6a9955;
}

:deep(.hljs-number),
:deep(.hljs-regexp),
:deep(.hljs-literal),
:deep(.hljs-bullet),
:deep(.hljs-link) {
  color: #b5cea8;
}

:deep(.hljs-function .hljs-title),
:deep(.hljs-class .hljs-title) {
  color: #dcdcaa;
}

:deep(.hljs-variable),
:deep(.hljs-params),
:deep(.hljs-class .hljs-title.function_) {
  color: #9cdcfe;
}

:deep(.hljs-attr) {
  color: #9cdcfe;
}

:deep(.hljs-symbol),
:deep(.hljs-meta-string) {
  color: #e6c07b;
}

/* 表格样式 */
.prose :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5em 0;
  font-size: 0.9em;
}

.prose :deep(th),
.prose :deep(td) {
  padding: 0.75em;
  border: 1px solid #e5e7eb;
  text-align: left;
}

.prose :deep(th) {
  background-color: #f9fafb;
  font-weight: 600;
}

.prose :deep(tr:nth-child(even)) {
  background-color: #f9fafb;
}

.prose :deep(tr:hover) {
  background-color: #f3f4f6;
}

/* 链接样式 */
.prose :deep(a) {
  color: #3b82f6;
  text-decoration: none;
}

.prose :deep(a:hover) {
  text-decoration: underline;
}

/* 引用样式 */
.prose :deep(blockquote) {
  margin: 1.5em 0;
  padding: 1em;
  border-left: 4px solid #3b82f6;
  background-color: #f8fafc;
  color: #64748b;
  font-style: italic;
}

/* 分隔线样式 */
.prose :deep(hr) {
  margin: 2em 0;
  border: none;
  border-top: 1px solid #e5e7eb;
}

/* 滚动条样式 */
.prose pre::-webkit-scrollbar,
:deep(.hljs)::-webkit-scrollbar {
  height: 8px;
  width: 8px;
}

.prose pre::-webkit-scrollbar-track,
:deep(.hljs)::-webkit-scrollbar-track {
  background: #2d2d2d;
  border-radius: 4px;
}

.prose pre::-webkit-scrollbar-thumb,
:deep(.hljs)::-webkit-scrollbar-thumb {
  background: #6b7280;
  border-radius: 4px;
}

.prose pre::-webkit-scrollbar-thumb:hover,
:deep(.hljs)::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* 深色模式适配 */
.dark .prose {
  color: #d1d5db;
}

.dark .prose :deep(h1) {
  color: #f9fafb;
  border-bottom-color: #374151;
}

.dark .prose :deep(h2) {
  color: #f3f4f6;
  border-bottom-color: #374151;
}

.dark .prose :deep(h3) {
  color: #e5e7eb;
}

.dark .prose :deep(h4),
.dark .prose :deep(h5),
.dark .prose :deep(h6) {
  color: #d1d5db;
}

.dark .prose code:not(pre code) {
  background-color: #374151;
  color: #f87171;
}

.dark .prose :deep(table) {
  border-color: #374151;
}

.dark .prose :deep(th) {
  background-color: #374151;
}

.dark .prose :deep(tr:nth-child(even)) {
  background-color: #374151;
}

.dark .prose :deep(tr:hover) {
  background-color: #4b5563;
}

.dark .prose :deep(a) {
  color: #60a5fa;
}

.dark .prose :deep(blockquote) {
  background-color: #374151;
  color: #9ca3af;
}

.dark .prose :deep(hr) {
  border-top-color: #374151;
}
</style>
