<template>
  <footer class="bg-footer text-on-dark">
    <div class="max-w-6xl mx-auto px-4 py-12 sm:py-14">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-10 md:gap-8">
        <!-- 站点信息 -->
        <div>
          <div class="flex items-center gap-3 mb-4">
            <div
              class="w-10 h-10 r-md flex items-center justify-center"
              :style="{ backgroundColor: themeConfig.primaryColor }"
            >
              <Person class="w-6 h-6 text-white" />
            </div>
            <span class="text-lg font-semibold tracking-tight">
              {{ siteConfig.title || 'Jinynet 个人站点' }}
            </span>
          </div>
          <p class="text-on-dark-muted text-sm leading-relaxed">
            {{ siteConfig.description || '分享技术文章与项目实践' }}
          </p>
        </div>

        <!-- 快速链接 -->
        <div>
          <h3 class="text-sm font-semibold tracking-wide uppercase mb-4 text-on-dark/90">
            快速导航
          </h3>
          <ul class="space-y-3 text-sm">
            <li>
              <a href="/" class="text-on-dark-muted hover:text-on-dark transition-colors">
                首页
              </a>
            </li>
            <li>
              <a href="/articles" class="text-on-dark-muted hover:text-on-dark transition-colors">
                文章
              </a>
            </li>
            <li>
              <a href="/videos" class="text-on-dark-muted hover:text-on-dark transition-colors">
                视频
              </a>
            </li>
            <li>
              <a href="/about" class="text-on-dark-muted hover:text-on-dark transition-colors">
                关于我
              </a>
            </li>
          </ul>
        </div>

        <!-- 联系方式 -->
        <div>
          <h3 class="text-sm font-semibold tracking-wide uppercase mb-4 text-on-dark/90">
            联系方式
          </h3>
          <ul class="space-y-3 text-sm">
            <li v-if="contactInfo?.email" class="flex items-center gap-2 text-on-dark-muted">
              <Mail class="w-4 h-4 shrink-0" />
              <span>{{ contactInfo.email }}</span>
            </li>
            <li v-if="contactInfo?.phone" class="flex items-center gap-2 text-on-dark-muted">
              <Phone class="w-4 h-4 shrink-0" />
              <span>{{ contactInfo.phone }}</span>
            </li>
            <li v-if="contactInfo?.location" class="flex items-center gap-2 text-on-dark-muted">
              <MapPin class="w-4 h-4 shrink-0" />
              <span>{{ contactInfo.location }}</span>
            </li>
            <li v-if="!contactInfo" class="text-on-dark-muted/60 text-sm">
              加载中...
            </li>
          </ul>
        </div>
      </div>

      <div class="border-t border-on-dark mt-12 pt-8">
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
          <p class="text-on-dark-muted/80 text-sm text-center md:text-left">
            <template v-if="siteConfig.copyright">
              {{ siteConfig.copyright }}
            </template>
            <template v-else>
              &copy; {{ currentYear }} {{ siteConfig.title || 'Jinynet' }}. All rights reserved.
            </template>
          </p>
          <div class="flex flex-wrap items-center justify-center gap-4 text-on-dark-muted/80 text-sm">
            <a
              v-if="siteConfig.icp"
              href="https://beian.miit.gov.cn/"
              target="_blank"
              rel="noopener noreferrer"
              class="hover:text-on-dark transition-colors"
            >
              ICP备案号：{{ siteConfig.icp }}
            </a>
            <span v-if="siteConfig.securityRecord">
              公安备案号：{{ siteConfig.securityRecord }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Person, Mail, Phone, MapPin } from '@/icons'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'

const appStore = useAppStore()
const userStore = useUserStore()
const { themeConfig } = useTheme()

const siteConfig = computed(() => appStore.siteConfig)
const contactInfo = computed(() => userStore.getContactInfo())
const currentYear = new Date().getFullYear()
</script>
