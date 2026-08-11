<template>
  <header class="fixed top-0 left-0 right-0 z-50 r-none border-b border-base">
    <!-- 毛玻璃背景层：用 bg-card 语义类（亮色=浅/暗色=深） + backdrop-blur -->
    <div class="absolute inset-0 bg-card/90 backdrop-blur-md supports-[backdrop-filter]:bg-card/70" />

    <div class="relative max-w-6xl mx-auto px-4 sm:px-6">
      <!-- 统一的垂直节奏：nav 高度固定，移动端/桌面端一致 -->
      <div class="flex items-center justify-between h-16 sm:h-[68px]">
        <!-- Logo 区 -->
        <a href="/" class="flex items-center gap-2.5 group shrink-0">
          <div
            class="w-9 h-9 r-md flex items-center justify-center transition-transform duration-200 group-hover:scale-[1.03]"
            :style="{ backgroundColor: themeConfig.primaryColor }"
          >
            <Person class="w-5 h-5 text-white" />
          </div>
          <span class="text-base sm:text-lg font-bold tracking-tight text-heading">
            {{ siteConfig.title }}
          </span>
        </a>

        <!-- 桌面端导航 -->
        <nav class="hidden md:flex items-center gap-1">
          <a
            v-for="item in navItems"
            :key="item.name"
            :href="item.path"
            class="relative px-3.5 py-2 text-sm font-medium transition-colors duration-200 rounded-md
                   text-body hover:text-heading hover:bg-card-hover"
            :class="{ 'text-heading': isNavActive(item.path) }"
          >
            {{ item.name }}
            <!-- 激活下划线：使用主题主色 -->
            <span
              v-if="isNavActive(item.path)"
              class="absolute left-3.5 right-3.5 -bottom-[1px] h-0.5 rounded-full"
              :style="{ backgroundColor: themeConfig.primaryColor }"
            />
          </a>
        </nav>

        <!-- 右侧操作区：主题切换 + 移动端菜单 -->
        <div class="flex items-center gap-1">
          <n-button
            text
            class="w-10 h-10"
            :title="themeModeLabel"
            @click="toggleTheme"
          >
            <template #icon>
              <Sun v-if="isDark" class="w-[18px] h-[18px]" />
              <Moon v-else class="w-[18px] h-[18px]" />
            </template>
          </n-button>
          <n-button
            text
            class="md:hidden w-10 h-10"
            aria-label="菜单"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <template #icon>
              <Menu v-if="!mobileMenuOpen" class="w-[20px] h-[20px]" />
              <X v-else class="w-[20px] h-[20px]" />
            </template>
          </n-button>
        </div>
      </div>

      <!-- 移动端菜单 -->
      <div
        v-if="mobileMenuOpen"
        class="md:hidden pb-4 overflow-hidden"
      >
        <nav class="flex flex-col gap-0.5 pt-2 border-t border-base">
          <a
            v-for="item in navItems"
            :key="item.name"
            :href="item.path"
            class="px-3 py-2.5 text-sm font-medium rounded-md transition-colors
                   text-body hover:bg-card-hover hover:text-heading"
            :class="isNavActive(item.path) ? 'text-heading bg-card-hover' : ''"
            @click="mobileMenuOpen = false"
          >
            {{ item.name }}
          </a>
        </nav>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Person, Menu, Sun, Moon, Close as X } from '@/icons'
import { NButton } from 'naive-ui'
import { useAppStore } from '@/stores/app'
import { useTheme } from '@/composables/useTheme'

const route = useRoute()
const appStore = useAppStore()
const siteConfig = computed(() => appStore.siteConfig)
const { themeConfig, isDark, toggleTheme } = useTheme()

const mobileMenuOpen = ref(false)
const currentPath = computed(() => route.path)

const navItems = [
  { name: '首页', path: '/' },
  { name: '文章', path: '/articles' },
  { name: '视频', path: '/videos' },
  { name: '搜索', path: '/search' },
  { name: '关于', path: '/about' }
] as const

const themeModeLabel = computed(() => {
  const mode = themeConfig.value.themeMode
  return mode === 'dark' ? '切换到浅色模式' : '切换到深色模式'
})

const isNavActive = (path: string) => {
  if (path === '/') {
    return currentPath.value === '/'
  }
  return currentPath.value === path || currentPath.value.startsWith(path + '/')
}
</script>
