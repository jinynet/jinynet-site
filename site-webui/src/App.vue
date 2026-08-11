<template>
  <n-config-provider :theme="currentTheme" :theme-overrides="themeOverrides" :locale="zhCN">
    <n-message-provider>
      <n-dialog-provider>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive :include="['Search']">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </n-dialog-provider>
    </n-message-provider>
    <transition name="fade">
      <button
        v-if="showBackToTop"
        @click="scrollToTop"
        class="fixed bottom-6 right-6 w-12 h-12 bg-gray-900 text-white rounded-full shadow-lg flex items-center justify-center hover:bg-gray-700 transition-colors z-50 dark:bg-gray-700 dark:hover:bg-gray-600"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      </button>
    </transition>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider } from 'naive-ui'
import zhCN from 'naive-ui/es/locales/common/zhCN'
import { useAppStore } from '@/stores/app'
import { getPublicSettings } from '@/api/settings'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const showBackToTop = ref(false)
const { currentTheme, themeOverrides, initTheme, loadThemeConfig } = useTheme()
let scrollTimer: number | null = null
let cleanupThemeListener: (() => void) | null = null

const siteTitle = computed(() => appStore.siteConfig.title || '个人技术平台')

// 通过路由 meta.title 设置页面标题（已无 routeTitles 字典）
const updatePageTitle = () => {
  const title = (route.meta.title as string | undefined) || siteTitle.value
  document.title = `${title} - ${siteTitle.value}`
}

watch(() => route.path, updatePageTitle, { immediate: true })
watch(siteTitle, updatePageTitle)

// 页面刷新时的滚动位置恢复（SPA 内部跳转由 router.scrollBehavior 处理）
const SCROLL_KEY_PREFIX = 'scroll_pos_'

const getScrollKey = (path: string) => `${SCROLL_KEY_PREFIX}${path}`

const saveScrollPosition = (path: string) => {
  const key = getScrollKey(path)
  sessionStorage.setItem(key, String(window.scrollY))
  sessionStorage.setItem(`${SCROLL_KEY_PREFIX}last_path`, path)
}

const restoreScrollPosition = () => {
  const lastPath = sessionStorage.getItem(`${SCROLL_KEY_PREFIX}last_path`)
  if (lastPath && lastPath === route.fullPath) {
    const saved = sessionStorage.getItem(getScrollKey(lastPath))
    if (saved) {
      const scrollY = parseInt(saved, 10)
      if (scrollY > 0) {
        window.scrollTo({ top: scrollY, behavior: 'instant' })
      }
    }
  }
}

const handleScroll = () => {
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = window.setTimeout(() => {
    showBackToTop.value = window.scrollY > 300
    saveScrollPosition(route.fullPath)
  }, 200)
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const loadPublicSettings = async () => {
  try {
    const response = await getPublicSettings()
    const data = response.data
    if (data) {
      appStore.loadSiteConfig(data)
      await loadThemeConfig(response)
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
  }
}

// 前端页面共享的用户信息加载（store 内部有 hasLoaded 缓存，不会重复请求）
const loadUserInfo = async () => {
  // 仅在前端页面（非 /admin）加载
  if (route.path.startsWith('/admin')) return
  await Promise.all([
    userStore.fetchUserInfo(),
    userStore.fetchUserContacts()
  ])
}

onMounted(() => {
  cleanupThemeListener = initTheme()
  loadPublicSettings()
  loadUserInfo()
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('beforeunload', () => {
    saveScrollPosition(route.fullPath)
  })
  setTimeout(restoreScrollPosition, 100)
})

onUnmounted(() => {
  if (cleanupThemeListener) {
    cleanupThemeListener()
  }
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>