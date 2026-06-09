<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
    <Header />
    
    <main>
      <!-- Hero Section -->
      <section 
        class="pt-20 pb-12 relative overflow-hidden min-h-[320px]"
        :class="isDark ? 'bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900' : 'bg-gradient-to-br from-slate-100 via-gray-50 to-blue-50'"
      >
        <div class="absolute top-0 right-0 w-64 h-64 sm:w-96 sm:h-96 rounded-full opacity-20 blur-3xl" :style="{ backgroundColor: primaryColor }"></div>
        <div class="absolute bottom-0 left-0 w-48 h-48 sm:w-64 sm:h-64 rounded-full opacity-10 blur-2xl" :style="{ backgroundColor: 'var(--accent-color)' }"></div>
        
        <div class="max-w-6xl mx-auto px-4 relative z-10">
          <div class="flex flex-col md:flex-row items-center gap-6 lg:gap-12 min-h-[260px]">
            <!-- 头像骨架屏 -->
            <div class="flex-shrink-0 relative w-24 h-24 sm:w-28 sm:h-28 md:w-32 md:h-32 lg:w-36 lg:h-36">
              <NSpin v-if="!userInfo" size="large" />
              <template v-else>
                <div class="absolute inset-0 rounded-full blur-xl opacity-30" :style="{ backgroundColor: primaryColor }"></div>
                <div class="relative w-full h-full rounded-full flex items-center justify-center shadow-2xl overflow-hidden ring-4 ring-white/10">
                  <img :src="userInfo.avatar" alt="avatar" class="w-full h-full object-cover" v-if="userInfo.avatar" loading="lazy">
                  <div v-else class="w-full h-full flex items-center justify-center" :style="{ backgroundColor: primaryColor }">
                    <Person class="w-12 h-12 sm:w-16 sm:h-16 md:w-20 md:h-20 text-white/90" />
                  </div>
                </div>
              </template>
            </div>
            <!-- 用户信息骨架屏 -->
            <div class="text-center md:text-left flex-1 min-w-0 px-2">
              <template v-if="!userInfo">
                <NSpin size="large" />
              </template>
              <template v-else>
                <div class="flex items-center justify-center md:justify-start gap-2 sm:gap-3 mb-2 sm:mb-3">
                  <h1 class="text-2xl sm:text-3xl md:text-4xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">{{ userInfo.nickname || userInfo.name || 'John Doe' }}</h1>
                  <NTag 
                    v-if="userInfo.online !== undefined ? userInfo.online : true" 
                    type="success" 
                    round 
                    size="small"
                  >
                    <span class="w-1.5 h-1.5 sm:w-2 sm:h-2 rounded-full bg-green-500 mr-1"></span>
                    <span class="text-xs sm:text-sm">在线</span>
                  </NTag>
                </div>
                <p class="text-base sm:text-lg md:text-xl mb-2 sm:mb-4" :class="isDark ? 'text-gray-200' : 'text-gray-600'">{{ userInfo.title || 'Full Stack Developer' }}</p>
                <p class="text-sm sm:text-base leading-relaxed max-w-none sm:max-w-lg" :class="isDark ? 'text-gray-300' : 'text-gray-500'">{{ userInfo.summary || '热爱技术，专注于 Web 开发和系统架构设计，乐于分享技术经验。' }}</p>
                <NSpace class="flex items-center justify-center md:justify-start gap-2 sm:gap-3 mt-4 sm:mt-6">
                  <a
                    v-for="contact in contacts"
                    :key="contact.id"
                    :href="contactHref(contact)"
                    :target="contactTarget(contact)"
                    class="w-8 h-8 sm:w-10 sm:h-10 flex items-center justify-center rounded-lg sm:rounded-xl transition-all duration-300 hover:scale-110 hover:shadow-lg"
                    :class="isDark ? 'bg-white/10 hover:bg-white/20' : 'bg-gray-200/50 hover:bg-gray-300/50'"
                    :style="{ color: isDark ? '#fff' : primaryColor }"
                    :title="contact.displayName || contact.contactType"
                  >
                    <component v-if="contact.icon" :is="iconComp('material', contact.icon)" class="w-4 h-4 sm:w-5 sm:h-5" />
                  </a>
                </NSpace>
              </template>
            </div>
          </div>
        </div>
      </section>

      <!-- Latest Articles Section -->
      <section class="py-16">
        <div class="max-w-6xl mx-auto px-4">
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-2xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">最新发布</h2>
            <NButton text tag="a" href="/articles" :style="{ color: primaryColor }">查看全部</NButton>
          </div>
          <!-- 骨架屏 -->
          <NSpin v-if="latestArticles.length === 0" size="large" class="flex justify-center py-12">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <NCard v-for="i in 3" :key="i" class="h-48">
              </NCard>
            </div>
          </NSpin>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <ArticleCard
              v-for="article in latestArticles"
              :key="article.id"
              :article="article"
            />
          </div>
        </div>
      </section>

      <!-- Hot Articles Section -->
      <section class="py-16">
        <div class="max-w-6xl mx-auto px-4">
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-2xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">热门文章</h2>
          </div>
          <!-- 骨架屏 -->
          <NSpin v-if="hotArticles.length === 0" size="large" class="flex justify-center py-12">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <NCard v-for="i in 4" :key="i" class="h-16">
              </NCard>
            </div>
          </NSpin>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <NCard
              v-for="(article, index) in hotArticles"
              :key="article.id"
              class="p-4 hover:shadow-md transition-shadow cursor-pointer"
              :class="isDark ? 'bg-gray-700' : 'bg-white'"
              @click="router.push(`/articles/id/${article.id}`)"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-7 h-7 rounded-full flex items-center justify-center text-white font-bold text-xs flex-shrink-0"
                  :class="getRankClass(index)"
                >
                  {{ index + 1 }}
                </div>
                <h3 class="font-medium truncate" :class="isDark ? 'text-white' : 'text-gray-900'">{{ article.title }}</h3>
              </div>
              <div class="flex items-center gap-4 mt-2 text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">
                <span>{{ article.category?.name || '文章' }}</span>
                <span class="flex items-center gap-1">
                  <Eye class="w-4 h-4" />
                  {{ article.viewCount }}
                </span>
              </div>
            </NCard>
          </div>
        </div>
      </section>

      <!-- Projects Section -->
      <section class="py-16" :class="isDark ? 'bg-gray-800' : 'bg-gray-100'">
        <div class="max-w-6xl mx-auto px-4">
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-2xl font-bold" :class="isDark ? 'text-white' : 'text-gray-900'">项目案例</h2>
          </div>
          <!-- 骨架屏 -->
          <NSpin v-if="projects.length === 0" size="large" class="flex justify-center py-12">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <NCard v-for="i in 3" :key="i" class="h-48">
              </NCard>
            </div>
          </NSpin>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <ProjectCard
              v-for="project in projects"
              :key="project.id"
              :project="project"
            />
          </div>
        </div>
      </section>

    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { Person, Eye } from '@/icons'
import { NSpin, NTag, NSpace, NButton, NCard } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
// 懒加载组件，减少首屏体积
const ArticleCard = defineAsyncComponent(() => import('@/components/frontend/ArticleCard.vue'))
const ProjectCard = defineAsyncComponent(() => import('@/components/frontend/ProjectCard.vue'))
import { getLatestArticles, getHotArticles, getPostedProjects } from '@/api/public'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'
import { loadMaterialIcon } from '@/utils/iconLoader'

const router = useRouter()
const { themeConfig, isDark } = useTheme()
const userStore = useUserStore()

const primaryColor = computed(() => themeConfig.value.primaryColor)
// 获取图标组件（按需懒加载）
const iconComp = (_type: string, icon: string) => {
  return loadMaterialIcon(icon)
}
const contactHref = (contact: Contact) => {
  if (contact.contactType === 'email') {
    return `mailto:${contact.contactValue}`
  } else if (contact.contactType === 'phone') {
    return `tel:${contact.contactValue}`
  } else {
    return contact.contactValue
  }
}
const contactTarget = (contact: Contact) => {
  if (contact.contactType === 'email') {
    return '_self'
  } else if (contact.contactType === 'phone') {
    return '_self'
  } else {
    return '_blank'
  }
}

const latestArticles = ref<Article[]>([])
const hotArticles = ref<Article[]>([])
const projects = ref<Project[]>([])

// 加载状态
const loading = ref({
  articles: true,
  videos: true,
  projects: true
})

// 使用计算属性从store获取数据，减少手动同步
const userInfo = computed<UserInfo | null>(() => {
  const info = userStore.userInfo
  if (!info) return null
  return {
    name: info.name,
    nickname: info.nickname || null,
    avatar: info.avatar || null,
    title: info.title || null,
    summary: info.summary || null,
    online: info.online || false
  }
})

const contacts = computed<Contact[]>(() => {
  return userStore.userContacts as Contact[]
})

const getRankClass = (index: number) => {
  const classes = ['bg-yellow-500', 'bg-gray-400', 'bg-orange-500']
  return classes[index] || 'bg-gray-300'
}

const fetchData = async () => {
  try {
    // 先加载用户信息（首屏关键）
    await Promise.all([
      userStore.fetchUserInfo(),
      userStore.fetchUserContacts()
    ])
    
    // 并行加载列表数据，不阻塞UI渲染
    await Promise.all([
      getLatestArticles().then(res => { 
        latestArticles.value = res.data || [] 
        loading.value.articles = false
      }),
      getHotArticles().then(res => { hotArticles.value = res.data || [] }),
      
      getPostedProjects().then(res => { 
        projects.value = res.data || [] 
        loading.value.projects = false
      })
    ])
  } catch (error) {
    console.error('Failed to fetch data:', error)
    // 发生错误时也关闭加载状态
    loading.value.articles = false
    loading.value.videos = false
    loading.value.projects = false
  }
}

// 定义类型
interface Article {
  id: number
  title: string
  excerpt: string | null
  coverImage: string | null
  viewCount: number
  likeCount: number
  publishedAt: string | null
  category?: { name: string } | null
  tags?: Array<{ name: string }>
}

interface ProjectStack {
  id: number
  name: string
  color?: string
}

interface Project {
  id: number
  name: string
  description: string | null
  coverImage: string | null
  projectUrl: string | null
  repoUrl: string | null
  status: string
  startDate: string | null
  endDate: string | null
  role: string | null
  sortOrder: number
  stacks: ProjectStack[] | null
}

interface UserInfo {
  name: string
  nickname: string | null
  avatar: string | null
  title: string | null
  summary: string | null
  online: boolean
}

interface Contact {
  id: number
  contactType: string
  contactValue: string
  displayName?: string
  icon?: string
}

fetchData()
</script>
