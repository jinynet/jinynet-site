<template>
  <div 
    class="min-h-screen flex items-center justify-center"
    :class="isDark ? 'bg-gray-900' : 'bg-gradient-to-br from-gray-50 to-gray-100'"
  >
    <div class="text-center px-4">
      <!-- 404 图标 -->
      <div class="relative w-48 h-48 mx-auto mb-8">
        <div class="absolute inset-0 rounded-full animate-pulse" :style="{ backgroundColor: `${themeConfig.primaryColor}10` }"></div>
        <div class="absolute inset-4 rounded-full" :style="{ backgroundColor: `${themeConfig.primaryColor}20` }"></div>
        <div class="absolute inset-0 flex items-center justify-center">
          <span class="text-8xl font-bold" :style="{ color: themeConfig.primaryColor }">404</span>
        </div>
      </div>

      <!-- 标题和描述 -->
      <h1 class="text-4xl font-bold mb-4" :class="isDark ? 'text-white' : 'text-gray-800'">页面未找到</h1>
      <p class="text-lg mb-8 max-w-md mx-auto" :class="isDark ? 'text-gray-400' : 'text-gray-600'">
        抱歉，您访问的页面不存在或已被删除。<br>
        请检查URL是否正确，或返回首页浏览其他内容。
      </p>

      <!-- 操作按钮 -->
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <n-button
          type="primary"
          size="large"
          @click="goHome"
          class="px-8"
        >
          <template #icon>
            <Home class="w-5 h-5" />
          </template>
          返回首页
        </n-button>
        <n-button
          type="default"
          size="large"
          @click="goBack"
          class="px-8"
        >
          <template #icon>
            <ArrowLeft class="w-5 h-5" />
          </template>
          返回上一页
        </n-button>
      </div>

      <!-- 快捷链接 -->
      <div class="mt-12 pt-8 border-t" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
        <p class="text-sm mb-4" :class="isDark ? 'text-gray-500' : 'text-gray-500'">快速导航</p>
        <div class="flex flex-wrap justify-center gap-4">
          <n-button
            v-for="link in quickLinks"
            :key="link.path"
            text
            @click="navigateTo(link.path)"
            :style="{ color: themeConfig.primaryColor }"
          >
            {{ link.label }}
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Home, ArrowLeft } from '@vicons/tabler'
import { NButton } from 'naive-ui'
import { useTheme } from '@/composables/useTheme'
import { onMounted } from 'vue'

const router = useRouter()
const { themeConfig, isDark } = useTheme()

const quickLinks = [
  { path: '/', label: '首页' },
  { path: '/articles', label: '文章' },
  { path: '/about', label: '关于' }
]

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    router.push('/')
  }
}

const navigateTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
})
</script>
