<template>
  <header 
    class="fixed top-0 left-0 right-0 z-50 backdrop-blur-sm shadow-sm transition-all duration-300"
    :class="isDark ? 'bg-gray-800/95' : 'bg-white/95'"
  >
    <div class="max-w-6xl mx-auto px-4 py-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <a href="/" class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor }">
              <Person class="w-6 h-6 text-white" />
            </div>
            <span class="text-xl font-bold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ siteConfig.title }}</span>
          </a>
        </div>
        <nav class="hidden md:flex items-center gap-6">
          <a 
            v-for="item in navItems" 
            :key="item.name"
            :href="item.path"
            class="transition-colors duration-200"
            :class="[
              isDark ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-gray-900',
              isNavActive(item.path) ? 'font-medium' : ''
            ]"
            :style="isNavActive(item.path) ? { color: themeConfig.primaryColor } : {}"
          >
            {{ item.name }}
          </a>
        </nav>
        <div class="flex items-center gap-3">
          <NButton
            text
            class="p-2"
            :class="isDark ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-gray-900'"
            @click="toggleTheme"
            :title="themeModeLabel"
          >
            <Sun v-if="isDark" class="w-5 h-5" />
            <Moon v-else class="w-5 h-5" />
          </NButton>
          <n-button 
            text 
            class="md:hidden p-2"
            :class="isDark ? 'text-gray-300' : 'text-gray-600'"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <Menu class="w-6 h-6" />
          </n-button>
        </div>
      </div>
      <div 
        v-if="mobileMenuOpen"
        class="md:hidden mt-4 pb-2 border-t pt-4"
        :class="isDark ? 'border-gray-700' : 'border-gray-200'"
      >
        <nav class="flex flex-col gap-3">
          <a 
            v-for="item in navItems" 
            :key="item.name"
            :href="item.path"
            class="transition-colors duration-200"
            :class="[
              isDark ? 'text-gray-300' : 'text-gray-600',
              isNavActive(item.path) ? 'font-medium' : ''
            ]"
            :style="isNavActive(item.path) ? { color: themeConfig.primaryColor } : {}"
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
import { Person, Menu, Sun, Moon } from '@/icons'
import { NButton } from 'naive-ui'
import { useAppStore } from '@/stores/app'
import { useTheme } from '@/composables/useTheme'

const appStore = useAppStore()
const siteConfig = computed(() => appStore.siteConfig)
const { themeConfig, isDark, toggleTheme } = useTheme()

const mobileMenuOpen = ref(false)
const currentPath = computed(() => window.location.pathname)

const navItems = [
  { name: '首页', path: '/' },
  { name: '文章', path: '/articles' },
  { name: '视频', path: '/videos' },
  { name: '搜索', path: '/search' },
  { name: '关于', path: '/about' }
]

const themeModeLabel = computed(() => {
  const mode = themeConfig.value.themeMode
  const labels = {
    light: '切换到深色模式',
    dark: '切换到浅色模式',
    system: '切换到浅色模式'
  }
  return labels[mode]
})

const isNavActive = (path: string) => {
  if (path === '/') {
    return currentPath.value === '/'
  }
  return currentPath.value === path || currentPath.value.startsWith(path + '/')
}
</script>
