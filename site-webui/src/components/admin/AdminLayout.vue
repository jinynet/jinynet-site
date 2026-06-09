<template>
  <div class="flex h-screen lg:static">
    <div class="hidden lg:flex w-64 flex-shrink-0">
      <aside class="w-full flex flex-col h-screen sticky top-0 transition-colors duration-300" :class="isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'" >
        <div class="p-4 border-b transition-colors duration-300" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor }">
              <Settings class="w-6 h-6 text-white" />
            </div>
            <span class="text-lg font-bold transition-colors duration-300" :class="isDark ? 'text-gray-100' : 'text-gray-800'">管理后台</span>
          </div>
        </div>

        <nav class="flex-1 p-4">
          <n-menu
            :value="currentPath"
            mode="vertical"
            :options="menuOptions"
            class="w-full"
            @update:value="(key) => router.push(key as string)"
          />
        </nav>

        <div class="p-4 border-t transition-colors duration-300" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
          <n-button block @click="handleLogout" type="primary">
            <LogOut class="w-4 h-4 mr-2" />
            退出登录
          </n-button>
        </div>
      </aside>
    </div>

    <div
      class="fixed inset-0 z-50 lg:hidden"
      :class="sidebarOpen ? 'block' : 'hidden'"
    >
      <div class="fixed inset-0 bg-black/50" @click="sidebarOpen = false"></div>
      <aside class="fixed left-0 top-0 bottom-0 w-64 flex flex-col z-50 transition-colors duration-300" :class="isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'">
        <div class="p-4 border-b flex items-center justify-between transition-colors duration-300" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
          <div class="flex items-center gap-2">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor }">
              <Settings class="w-6 h-6 text-white" />
            </div>
            <span class="text-lg font-bold transition-colors duration-300" :class="isDark ? 'text-gray-100' : 'text-gray-800'">管理后台</span>
          </div>
          <n-button text size="small" @click="sidebarOpen = false">
            <span class="transition-colors duration-300" :class="isDark ? 'text-gray-400' : 'text-gray-500'">✕</span>
          </n-button>
        </div>

        <nav class="flex-1 p-4">
          <n-menu
            :value="currentPath"
            mode="vertical"
            :options="menuOptions"
            class="w-full"
            @update:value="(key) => { router.push(key as string); sidebarOpen = false }"
          />
        </nav>

        <div class="p-4 border-t transition-colors duration-300" :class="isDark ? 'border-gray-700' : 'border-gray-200'">
          <n-button block @click="handleLogout" type="primary">
            <LogOut class="w-4 h-4 mr-2" />
            退出登录
          </n-button>
        </div>
      </aside>
    </div>

    <main class="flex-1 overflow-auto flex flex-col transition-colors duration-300" :class="isDark ? 'bg-gray-900' : 'bg-gray-50'">
      <header class="sticky top-0 z-30 transition-colors duration-300" :class="isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'">
        <div class="flex items-center justify-between px-4 py-3">
          <div class="flex items-center gap-4">
            <n-button text size="small" class="lg:hidden" @click="sidebarOpen = true">
              <template #icon>
                <Menu class="w-5 h-5" />
              </template>
            </n-button>
            <span class="font-bold transition-colors duration-300" :class="isDark ? 'text-gray-100' : 'text-gray-800'" >{{ pageTitle }}</span>
          </div>
          <div class="flex items-center gap-4">
            <n-button text size="medium" @click="goToHome" title="返回首页">
              <template #icon>
                <Home class="w-6 h-6" />
              </template>
            </n-button>
            <n-button text size="medium" @click="showNotifications = true">
              <template #icon>
                <Bell class="w-6 h-6" />
              </template>
            </n-button>
            <n-popover trigger="click" placement="bottom-end" :width="320" :show-arrow="false" class="upload-task-popover">
              <template #trigger>
                <n-badge :value="activeTaskCount" :max="99" :show="activeTaskCount > 0" processing>
                  <n-button text size="medium" title="上传任务">
                    <template #icon>
                      <Upload class="w-6 h-6" />
                    </template>
                  </n-button>
                </n-badge>
              </template>
              <UploadTaskPanel @count-change="c => activeTaskCount = c" />
            </n-popover>
            <n-button text size="medium" @click="toggleTheme" :title="isDark ? '切换到浅色模式' : '切换到深色模式'">
              <template #icon>
                <component :is="isDark ? Sun : Moon" class="w-6 h-6" />
              </template>
            </n-button>
          </div>
        </div>
      </header>
      <div class="lg:p-6 p-4 pt-0 lg:pt-0 flex-1">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Grid, Document, Folder, Person, Settings, LogOut, Menu, Bell, Sun, Moon, Home, Video, Upload } from '@/icons'
import { NMenu, NButton, NPopover, NBadge, type MenuOption, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { useTheme } from '@/composables/useTheme'
import { updateSettings } from '@/api/settings'
import UploadTaskPanel from '@/views/admin/files/components/UploadTaskPanel.vue'

const sidebarOpen = ref(false)
const showNotifications = ref(false)
const activeTaskCount = ref(0)
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { themeConfig, isDark, applyTheme } = useTheme()
const message = useMessage()

const currentPath = computed(() => route.path)

const menuOptions: MenuOption[] = [
  {
    label: '仪表盘',
    key: '/admin',
    icon: () => h(Grid)
  },
  {
    label: '文章管理',
    key: '/admin/articles',
    icon: () => h(Document)
  },
  {
    label: '项目管理',
    key: '/admin/projects',
    icon: () => h(Folder)
  },
  {
    label: '文件管理',
    key: '/admin/files',
    icon: () => h(Video)
  },
  {
    label: '个人信息',
    key: '/admin/profile',
    icon: () => h(Person)
  },
  {
    label: '系统设置',
    key: '/admin/settings',
    icon: () => h(Settings)
  }
]
const pageTitle = computed(() => {
  let best: any = null
  for (const m of menuOptions) {
    const key = (m as any).key as string
    if (key && route.path.startsWith(key)) {
      if (!best || key.length > (best.key as string).length) best = m
    }
  }
  return best ? (best as any).label : '管理后台'
})


const goToHome = () => {
  router.push('/')
}

const toggleTheme = async () => {
  const currentMode = themeConfig.value.themeMode
  const newMode = currentMode === 'dark' ? 'light' : 'dark'
  
  try {
    await updateSettings({
      theme_mode: { value: newMode, category: 'theme' }
    } as unknown as any)
    
    applyTheme({ themeMode: newMode })
    message.success(newMode === 'dark' ? '已切换到深色模式' : '已切换到浅色模式')
  } catch (error) {
    console.error('保存主题模式失败:', error)
    message.error('保存主题模式失败')
  }
}

const handleLogout = async () => {
  await authStore.handleLogout()
  router.push('/admin/login')
}
</script>
