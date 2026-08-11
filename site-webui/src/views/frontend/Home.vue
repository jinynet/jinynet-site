<template>
  <div class="min-h-screen bg-page">
    <Header />

    <main>
      <!-- Hero Section -->
      <section
        class="relative overflow-hidden pt-28 pb-16 sm:pt-32 sm:pb-20"
        :class="isDark
          ? 'bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900'
          : 'bg-gradient-to-br from-gray-50 via-white to-slate-50'"
      >
        <!-- 装饰光晕：使用主题色，始终柔和不抢眼 -->
        <div
          class="absolute -top-24 -right-24 w-72 h-72 sm:w-96 sm:h-96 rounded-full opacity-20 blur-3xl pointer-events-none"
          :style="{ backgroundColor: primaryColor }"
        />
        <div
          class="absolute -bottom-20 -left-20 w-56 h-56 sm:w-72 sm:h-72 rounded-full opacity-10 blur-3xl pointer-events-none"
          :style="{ backgroundColor: primaryColor }"
        />

        <div class="max-w-6xl mx-auto px-4 sm:px-6 relative z-10">
          <div
            class="flex flex-col md:flex-row items-center gap-8 lg:gap-12
                   md:items-start md:justify-start"
          >
            <!-- 头像 -->
            <div class="flex-shrink-0 relative w-24 h-24 sm:w-28 sm:h-28 md:w-32 md:h-32 lg:w-36 lg:h-36">
              <!-- 头像加载骨架 -->
              <div
                v-if="!userInfo"
                class="w-full h-full r-pill bg-subtle animate-pulse"
              />
              <template v-else>
                <!-- 头像外围主色光晕 -->
                <div
                  class="absolute inset-0 r-pill blur-xl opacity-30 pointer-events-none"
                  :style="{ backgroundColor: primaryColor }"
                />
                <div
                  class="relative w-full h-full r-pill flex items-center justify-center
                         shadow-elevated overflow-hidden
                         ring-4"
                  :class="isDark ? 'ring-white/10' : 'ring-white'"
                >
                  <img
                    v-if="userInfo.avatar"
                    :src="userInfo.avatar"
                    alt="avatar"
                    class="w-full h-full object-cover"
                    loading="eager"
                    decoding="async"
                  >
                  <div
                    v-else
                    class="w-full h-full flex items-center justify-center"
                    :style="{ backgroundColor: primaryColor }"
                  >
                    <Person class="w-12 h-12 sm:w-16 sm:h-16 md:w-20 md:h-20 text-white/90" />
                  </div>
                </div>
              </template>
            </div>

            <!-- 用户信息文本 -->
            <div class="text-center md:text-left flex-1 min-w-0 px-2">
              <!-- 骨架屏：用户信息 -->
              <div v-if="!userInfo" class="space-y-3 max-w-lg mx-auto md:mx-0">
                <div class="h-7 sm:h-9 w-48 sm:w-64 bg-subtle r-sm animate-pulse mx-auto md:mx-0" />
                <div class="h-5 sm:h-6 w-40 sm:w-56 bg-subtle r-sm animate-pulse mx-auto md:mx-0" />
                <div class="space-y-2 pt-2">
                  <div class="h-4 w-full bg-subtle r-sm animate-pulse" />
                  <div class="h-4 w-11/12 bg-subtle r-sm animate-pulse mx-auto md:mx-0" />
                  <div class="h-4 w-9/12 bg-subtle r-sm animate-pulse mx-auto md:mx-0" />
                </div>
                <div class="h-10 w-56 bg-subtle r-md animate-pulse mx-auto md:mx-0 mt-6" />
              </div>

              <template v-else>
                <!-- 名称 + 在线状态 -->
                <div class="flex flex-wrap items-center justify-center md:justify-start gap-2 sm:gap-3 mb-2 sm:mb-3">
                  <h1
                    class="text-2xl sm:text-3xl md:text-4xl font-bold tracking-tight text-heading"
                  >
                    {{ userInfo.nickname || userInfo.name || 'John Doe' }}
                  </h1>
                  <NTag
                    v-if="userInfo.online !== undefined ? userInfo.online : true"
                    type="success"
                    round
                    size="small"
                    :bordered="false"
                  >
                    <span class="w-1.5 h-1.5 sm:w-2 sm:h-2 r-pill bg-green-500 mr-1 inline-block align-middle" />
                    <span class="text-xs sm:text-sm">在线</span>
                  </NTag>
                </div>

                <!-- 头衔 -->
                <p class="text-base sm:text-lg md:text-xl mb-2 sm:mb-4 text-body">
                  {{ userInfo.title || 'Full Stack Developer' }}
                </p>

                <!-- 简介 -->
                <p class="text-sm sm:text-base leading-relaxed max-w-none sm:max-w-lg text-muted">
                  {{ userInfo.summary || '热爱技术，专注于 Web 开发和系统架构设计，乐于分享技术经验。' }}
                </p>

                <!-- 联系方式图标 -->
                <NSpace
                  class="flex items-center justify-center md:justify-start gap-2 sm:gap-3 mt-5 sm:mt-7"
                >
                  <a
                    v-for="contact in contacts"
                    :key="contact.id"
                    :href="contactHref(contact)"
                    :target="contactTarget(contact)"
                    rel="noopener noreferrer"
                    class="w-9 h-9 sm:w-10 sm:h-10 flex items-center justify-center
                           r-md transition-all duration-200 ease-out
                           hover:-translate-y-0.5"
                    :class="isDark
                      ? 'bg-white/10 text-gray-200 hover:bg-white/20 hover:text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200 hover:text-gray-900'"
                    :title="contact.displayName || contact.contactType"
                  >
                    <component
                      v-if="contact.icon"
                      :is="iconComp('material', contact.icon)"
                      class="w-4 h-4 sm:w-5 sm:h-5"
                    />
                  </a>
                </NSpace>
              </template>
            </div>
          </div>
        </div>
      </section>

      <!-- Latest Articles Section -->
      <section class="py-14 sm:py-16">
        <div class="max-w-6xl mx-auto px-4 sm:px-6">
          <div class="flex items-end justify-between mb-8 gap-4">
            <div>
              <h2 class="text-xl sm:text-2xl font-bold tracking-tight text-heading mb-1">
                最新发布
              </h2>
              <p class="text-sm text-muted">持续记录与分享</p>
            </div>
            <NButton
              text
              tag="a"
              href="/articles"
              size="medium"
              :style="{ color: primaryColor }"
              class="shrink-0"
            >
              查看全部
            </NButton>
          </div>

          <!-- 骨架屏 -->
          <div
            v-if="latestArticles.length === 0"
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            <div
              v-for="i in 3"
              :key="i"
              class="r-lg bg-card border border-base p-6 animate-pulse space-y-4"
            >
              <div class="flex justify-between items-center">
                <div class="h-5 w-20 bg-subtle r-pill" />
                <div class="h-3 w-20 bg-subtle r-sm" />
              </div>
              <div class="h-6 w-full bg-subtle r-sm" />
              <div class="h-4 w-11/12 bg-subtle r-sm" />
              <div class="h-4 w-9/12 bg-subtle r-sm" />
              <div class="h-4 w-6/12 bg-subtle r-sm" />
            </div>
          </div>

          <div
            v-else
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 sm:gap-6"
          >
            <ArticleCard
              v-for="article in latestArticles"
              :key="article.id"
              :article="article"
            />
          </div>
        </div>
      </section>

      <!-- Hot Articles Section -->
      <section class="py-14 sm:py-16 bg-subtle/40">
        <div class="max-w-6xl mx-auto px-4 sm:px-6">
          <div class="mb-8">
            <h2 class="text-xl sm:text-2xl font-bold tracking-tight text-heading mb-1">
              热门文章
            </h2>
            <p class="text-sm text-muted">阅读量最高的精选内容</p>
          </div>

          <!-- 骨架屏 -->
          <div v-if="hotArticles.length === 0" class="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
            <div
              v-for="i in 4"
              :key="i"
              class="r-md bg-card border border-base p-4 animate-pulse flex items-center gap-3"
            >
              <div class="w-7 h-7 r-pill bg-subtle" />
              <div class="h-5 flex-1 bg-subtle r-sm" />
            </div>
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
            <NCard
              v-for="(article, index) in hotArticles"
              :key="article.id"
              size="small"
              :bordered="false"
              class="!p-4 !r-md transition-all duration-200 ease-out
                     hover:shadow-card-hover cursor-pointer card-elevated !rounded-md"
              @click="router.push(`/articles/id/${article.id}`)"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-7 h-7 r-pill flex items-center justify-center
                         text-white font-bold text-xs flex-shrink-0"
                  :class="getRankClass(index)"
                >
                  {{ index + 1 }}
                </div>
                <h3
                  class="font-medium truncate text-sm sm:text-base text-heading"
                >
                  {{ article.title }}
                </h3>
              </div>
              <div class="flex items-center gap-4 mt-2 text-xs sm:text-sm text-muted">
                <span>{{ article.category?.name || '文章' }}</span>
                <span class="flex items-center gap-1">
                  <Eye class="w-3.5 h-3.5" />
                  <span class="tabular-nums">{{ article.viewCount }}</span>
                </span>
              </div>
            </NCard>
          </div>
        </div>
      </section>

      <!-- Projects Section -->
      <section class="py-14 sm:py-16">
        <div class="max-w-6xl mx-auto px-4 sm:px-6">
          <div class="mb-8">
            <h2 class="text-xl sm:text-2xl font-bold tracking-tight text-heading mb-1">
              项目案例
            </h2>
            <p class="text-sm text-muted">参与过的部分项目与开源作品</p>
          </div>

          <!-- 骨架屏 -->
          <div
            v-if="projects.length === 0"
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          >
            <div
              v-for="i in 3"
              :key="i"
              class="r-lg bg-card border border-base p-6 animate-pulse space-y-4 min-h-[240px]"
            >
              <div class="flex justify-between items-center">
                <div class="h-5 w-20 bg-subtle r-pill" />
                <div class="h-3 w-24 bg-subtle r-sm" />
              </div>
              <div class="h-6 w-10/12 bg-subtle r-sm" />
              <div class="space-y-2">
                <div class="h-4 w-full bg-subtle r-sm" />
                <div class="h-4 w-11/12 bg-subtle r-sm" />
              </div>
              <div class="h-5 w-full flex gap-1.5">
                <div class="w-12 h-full bg-subtle r-sm" />
                <div class="w-12 h-full bg-subtle r-sm" />
                <div class="w-12 h-full bg-subtle r-sm" />
              </div>
            </div>
          </div>

          <div
            v-else
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 sm:gap-6"
          >
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
import { NTag, NSpace, NButton, NCard } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
// 懒加载组件，减少首屏体积
const ArticleCard = defineAsyncComponent(() => import('@/components/frontend/ArticleCard.vue'))
const ProjectCard = defineAsyncComponent(() => import('@/components/frontend/ProjectCard.vue'))
import { getLatestArticles, getHotArticles, getPostedProjects } from '@/api/public'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'
import { loadMaterialIcon } from '@/utils/iconLoader'
import type { ArticleCardItem, PostedProjectListItem, UserContactPublic } from '@/types'

type Article = ArticleCardItem
type Project = PostedProjectListItem
type Contact = UserContactPublic

const router = useRouter()
const { themeConfig, isDark } = useTheme()
const userStore = useUserStore()

const primaryColor = computed(() => themeConfig.value.primaryColor)
// 获取图标组件（按需懒加载）
const iconComp = (_type: string, icon: string) => loadMaterialIcon(icon)

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
  if (contact.contactType === 'email' || contact.contactType === 'phone') {
    return '_self'
  }
  return '_blank'
}

const latestArticles = ref<Article[]>([])
const hotArticles = ref<Article[]>([])
const projects = ref<Project[]>([])

// 使用计算属性从 store 获取数据，减少手动同步
const userInfo = computed(() => {
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
  return userStore.userContacts as unknown as Contact[]
})

const getRankClass = (index: number) => {
  if (index === 0) return 'bg-amber-500'
  if (index === 1) return 'bg-gray-400'
  if (index === 2) return 'bg-orange-500'
  return 'bg-gray-400/70'
}

// 并行加载所有数据
const fetchData = async () => {
  // 用户信息（首屏关键，优先）
  void Promise.all([
    userStore.fetchUserInfo(),
    userStore.fetchUserContacts()
  ])

  // 列表数据（并行，不阻塞首屏骨架）
  try {
    await Promise.all([
      getLatestArticles().then(res => { latestArticles.value = res.data || [] }),
      getHotArticles().then(res => { hotArticles.value = res.data || [] }),
      getPostedProjects().then(res => { projects.value = res.data || [] })
    ])
  } catch (error) {
    console.error('Failed to fetch data:', error)
  }
}

fetchData()
</script>
