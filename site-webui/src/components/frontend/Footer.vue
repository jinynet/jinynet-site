<template>
  <footer 
    class="py-12"
    :class="themeMode === 'dark' ? 'bg-gray-800 text-white' : 'bg-gray-900 text-white'"
  >
    <div class="max-w-6xl mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- 站点信息 -->
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor }">
              <Person class="w-6 h-6 text-white" />
            </div>
            <span class="text-xl font-bold">{{ siteConfig.title || 'Jinynet个人站点' }}</span>
          </div>
          <p class="text-gray-400 text-sm">{{ siteConfig.description || '分享技术文章和项目经验' }}</p>
        </div>
        <!-- 快速链接 -->
        <div>
          <h3 class="text-lg font-semibold mb-4">快速链接</h3>
          <ul class="space-y-2">
            <li><a href="/" class="text-gray-400 hover:text-white transition-colors">首页</a></li>
            <li><a href="/articles" class="text-gray-400 hover:text-white transition-colors">文章</a></li>
            <li><a href="/videos" class="text-gray-400 hover:text-white transition-colors">视频</a></li>
            <li><a href="/about" class="text-gray-400 hover:text-white transition-colors">关于</a></li>
          </ul>
        </div>
        <!-- 联系方式 -->
        <div>
          <h3 class="text-lg font-semibold mb-4">联系方式</h3>
          <ul class="space-y-2">
            <li v-if="contactInfo && contactInfo.email" class="flex items-center gap-2 text-gray-400">
              <Mail class="w-4 h-4" />
              <span>{{ contactInfo.email }}</span>
            </li>
            <li v-if="contactInfo && contactInfo.phone" class="flex items-center gap-2 text-gray-400">
              <Phone class="w-4 h-4" />
              <span>{{ contactInfo.phone }}</span>
            </li>
            <li v-if="contactInfo && contactInfo.location" class="flex items-center gap-2 text-gray-400">
              <MapPin class="w-4 h-4" />
              <span>{{ contactInfo.location }}</span>
            </li>
            <li v-if="!contactInfo" class="text-gray-500 text-sm">
              加载中...
            </li>
          </ul>
        </div>
      </div>
      <div class="border-t border-gray-700 mt-8 pt-8">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
          <p class="text-gray-500 text-sm text-center md:text-left">
            <template v-if="siteConfig.copyright">
              {{ siteConfig.copyright }}
            </template>
            <template v-else>
              &copy; {{ currentYear }} {{ siteConfig.title || 'Jinynet' }}. All rights reserved.
            </template>
          </p>
          <div class="flex flex-wrap items-center justify-center gap-4 text-gray-500 text-sm">
            <span v-if="siteConfig.icp">
              <a 
                href="https://beian.miit.gov.cn/" 
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:text-white transition-colors"
              >
                ICP备案号: {{ siteConfig.icp }}
              </a>
            </span>
            <span v-if="siteConfig.securityRecord" class="ml-2">
              公安备案号: {{ siteConfig.securityRecord }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { Person, Mail, Phone, MapPin } from '@/icons'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'

const appStore = useAppStore()
const userStore = useUserStore()
const { themeConfig } = useTheme()
const themeMode = ref('light')

const siteConfig = computed(() => appStore.siteConfig)
const contactInfo = computed(() => userStore.getContactInfo())

const currentYear = new Date().getFullYear()

onMounted(() => {
  themeMode.value = themeConfig.value.themeMode
})
</script>
