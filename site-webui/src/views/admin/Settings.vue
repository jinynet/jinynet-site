<template>
  <div class="space-y-6">
    <n-tabs v-model:value="activeTab" type="line" animated>
      <n-tab-pane name="site" tab="网站配置">
        <n-card title="网站基本信息">
          <n-form :model="siteForm" label-placement="top">
            <n-form-item label="网站标题" path="title">
              <n-input v-model:value="siteForm.title" placeholder="请输入网站标题" />
            </n-form-item>
            <n-form-item label="网站描述" path="description">
              <n-input v-model:value="siteForm.description" placeholder="请输入网站描述" :rows="3" type="textarea" />
            </n-form-item>
            <n-form-item label="网站关键词">
              <n-dynamic-tags v-model:value="siteForm.keywords" />
            </n-form-item>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <n-form-item label="Logo URL" path="logo">
                <div class="flex items-center gap-4">
                  <div v-if="siteForm.logo" class="w-12 h-12 rounded border overflow-hidden">
                    <img :src="siteForm.logo" class="w-full h-full object-contain" />
                  </div>
                  <n-input v-model:value="siteForm.logo" placeholder="请输入Logo图片URL" />
                </div>
              </n-form-item>
              <n-form-item label="Favicon URL" path="favicon">
                <n-input v-model:value="siteForm.favicon" placeholder="请输入Favicon图片URL" />
              </n-form-item>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <n-form-item label="ICP备案号" path="icp">
                <n-input v-model:value="siteForm.icp" placeholder="请输入ICP备案号" />
              </n-form-item>
              <n-form-item label="公安备案号" path="securityRecord">
                <n-input v-model:value="siteForm.securityRecord" placeholder="请输入公安备案号" />
              </n-form-item>
            </div>
            <n-form-item label="版权信息" path="copyright">
              <n-input v-model:value="siteForm.copyright" placeholder="请输入版权信息" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="saveSiteSettings" :loading="siteSaving">保存网站配置</n-button>
            </n-form-item>
          </n-form>
        </n-card>
      </n-tab-pane>

      <n-tab-pane name="seo" tab="SEO设置">
        <n-card title="搜索引擎优化">
          <n-form :model="seoForm" label-placement="top">
            <n-form-item label="SEO标题" path="seoTitle">
              <n-input v-model:value="seoForm.seoTitle" placeholder="请输入SEO标题" />
            </n-form-item>
            <n-form-item label="SEO描述" path="seoDescription">
              <n-input v-model:value="seoForm.seoDescription" placeholder="请输入SEO描述" :rows="3" type="textarea" />
            </n-form-item>
            <n-form-item label="SEO关键词">
              <n-dynamic-tags v-model:value="seoForm.seoKeywords" />
            </n-form-item>
            <n-form-item label="Robots.txt内容" path="seoRobots">
              <n-input v-model:value="seoForm.seoRobots" placeholder="请输入Robots.txt内容" :rows="4" type="textarea" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="saveSeoSettings" :loading="seoSaving">保存SEO设置</n-button>
            </n-form-item>
          </n-form>
        </n-card>
      </n-tab-pane>

      <n-tab-pane name="theme" tab="主题配置">
        <n-card title="主题模式">
          <n-form :model="themeForm" label-placement="top">
            <n-form-item label="主题模式">
              <n-radio-group v-model:value="themeForm.themeMode" name="themeMode">
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="light" />
                    <span>浅色模式</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="dark" />
                    <span>深色模式</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="system" />
                    <span>跟随系统</span>
                  </label>
                </div>
              </n-radio-group>
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="预设主题" class="mt-6">
          <p class="text-gray-500 text-sm mb-4">快速应用预设主题风格</p>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'enterprise' }"
              @click="applyPreset('enterprise')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #0066CC"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #00B42A"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #FF7D00"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #F53F3F"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">企业经典蓝</p>
              <p class="text-center text-xs text-gray-500 mt-1">中后台首选</p>
            </div>
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'businessBlue' }"
              @click="applyPreset('businessBlue')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #2F5496"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #67C23A"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #E6A23C"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #F56C6C"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">商务高级蓝</p>
              <p class="text-center text-xs text-gray-500 mt-1">稳重专业</p>
            </div>
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'medical' }"
              @click="applyPreset('medical')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #10B981"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #34D399"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #FBBF24"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #F87171"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">医疗合规绿</p>
              <p class="text-center text-xs text-gray-500 mt-1">清新健康</p>
            </div>
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'deepPurple' }"
              @click="applyPreset('deepPurple')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #722ED1"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #00B42A"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #FF7D00"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #F53F3F"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">深邃紫色</p>
              <p class="text-center text-xs text-gray-500 mt-1">优雅高贵气质</p>
            </div>
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'naturalGreen' }"
              @click="applyPreset('naturalGreen')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #84CC16"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #A3E635"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #FCD34D"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #F87171"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">自然草绿</p>
              <p class="text-center text-xs text-gray-500 mt-1">清新自然风格</p>
            </div>
            <div
              class="p-5 rounded-xl border-2 cursor-pointer hover:shadow-lg transition-all bg-white/5 dark:bg-black/20"
              :class="{ 'border-primary': activePreset === 'minimal' }"
              @click="applyPreset('minimal')"
            >
              <div class="flex justify-center gap-4 mb-3">
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #1a1a1a"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #4CAF50"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #9E7B57"></div>
                <div class="w-10 h-10 rounded-full shadow-sm ring-2 ring-white/10" style="background-color: #C75050"></div>
              </div>
              <p class="text-center text-base font-semibold text-gray-700 dark:text-gray-200">简约黑白（默认）</p>
              <p class="text-center text-xs text-gray-500 mt-1">经典极简风格</p>
            </div>
          </div>
        </n-card>

        <n-card title="主题编辑器" class="mt-6">
          <p class="text-gray-500 text-sm mb-4">自定义主题颜色和样式</p>
          <n-form :model="themeForm" label-placement="top">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <n-form-item label="主色调">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.primaryColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.primaryColor" class="flex-1" />
                </div>
              </n-form-item>
              <n-form-item label="强调色">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.accentColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.accentColor" class="flex-1" />
                </div>
              </n-form-item>
              <n-form-item label="成功色">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.successColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.successColor" class="flex-1" />
                </div>
              </n-form-item>
              <n-form-item label="警告色">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.warningColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.warningColor" class="flex-1" />
                </div>
              </n-form-item>
              <n-form-item label="错误色">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.errorColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.errorColor" class="flex-1" />
                </div>
              </n-form-item>
              <n-form-item label="信息色">
                <div class="flex items-center gap-3">
                  <input
                    type="color"
                    v-model="themeForm.infoColor"
                    class="w-10 h-10 rounded cursor-pointer border-0"
                  />
                  <n-input v-model:value="themeForm.infoColor" class="flex-1" />
                </div>
              </n-form-item>
            </div>
            <div class="mt-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
              <h4 class="text-sm font-medium text-gray-700 dark:text-gray-200 mb-3">实时预览</h4>
              <div class="flex flex-wrap gap-3">
                <n-button type="primary">主要按钮</n-button>
                <n-button type="success">成功按钮</n-button>
                <n-button type="warning">警告按钮</n-button>
                <n-button type="error">错误按钮</n-button>
                <n-button type="info">信息按钮</n-button>
              </div>
              <div class="mt-4 flex flex-wrap gap-2">
                <n-tag type="primary">主色标签</n-tag>
                <n-tag type="success">成功标签</n-tag>
                <n-tag type="warning">警告标签</n-tag>
                <n-tag type="error">错误标签</n-tag>
                <n-tag type="info">信息标签</n-tag>
              </div>
              <div class="mt-4">
                <div class="flex gap-2 mb-2">
                  <n-switch v-model:value="previewSwitchValue" />
                  <span>开关示例</span>
                </div>
                <div class="flex gap-2">
                  <n-radio-group v-model:value="previewRadioValue" name="preview-radio">
                    <n-radio value="option1" />
                    <n-radio value="option2" />
                    <n-radio value="option3" />
                  </n-radio-group>
                </div>
              </div>
              <div class="mt-4">
                <n-input placeholder="输入框示例" class="w-64" />
              </div>
              <div class="mt-4">
                <n-select
                  v-model:value="previewSelectValue"
                  :options="selectOptions"
                  placeholder="选择框示例"
                  class="w-64"
                />
              </div>
              <div class="mt-4">
                <n-progress :percentage="75" />
              </div>
            </div>
          </n-form>
        </n-card>

        <n-card title="字体设置" class="mt-6">
          <n-form :model="themeForm" label-placement="top">
            <n-form-item label="字体家族">
              <n-select
                v-model:value="themeForm.fontFamily"
                :options="fontFamilyOptions"
                placeholder="请选择字体"
              />
            </n-form-item>
            <n-form-item label="字体大小">
              <n-radio-group v-model:value="themeForm.fontSize" name="fontSize">
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="sm" /> 
                    <span>小号</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="md" /> 
                    <span>中等</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="lg" /> 
                    <span>大号</span>
                  </label>
                </div>
              </n-radio-group>
            </n-form-item>
            <n-form-item label="圆角大小">
              <n-radio-group v-model:value="themeForm.borderRadius" name="borderRadius">
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="sm" /> 
                    <span>小圆角 (4px)</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="md" /> 
                    <span>中等圆角 (8px)</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="lg" /> 
                    <span>大圆角 (12px)</span>
                  </label>
                </div>
              </n-radio-group>
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="布局设置" class="mt-6">
          <n-form :model="themeForm" label-placement="top">
            <n-form-item label="布局模式">
              <n-radio-group v-model:value="themeForm.layoutMode" name="layoutMode">
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="full-width" />
                    <span>全宽布局</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <n-radio value="boxed" />
                    <span>盒装布局</span>
                  </label>
                </div>
              </n-radio-group>
            </n-form-item>
            <n-form-item label="启用动画效果">
              <n-switch v-model:value="themeForm.animationEnabled" />
              <span class="ml-2 text-gray-500 text-sm">{{ themeForm.animationEnabled ? '已启用' : '已禁用' }}</span>
            </n-form-item>
          </n-form>
        </n-card>

        <div class="mt-6">
          <n-button type="primary" @click="saveThemeSettings" :loading="themeSaving">保存主题配置</n-button>
          <n-button @click="resetThemeSettings" class="ml-4">重置为默认</n-button>
        </div>
      </n-tab-pane>

      <n-tab-pane name="security" tab="安全设置">
        <n-card title="账户安全">
          <n-form :model="securityForm" label-placement="top" autocomplete="off">
            <n-form-item label="管理员用户名" path="username">
              <n-input v-model:value="securityForm.username" placeholder="请输入管理员用户名" auto-complete="off" />
            </n-form-item>
            <n-form-item label="当前密码" path="oldPassword">
              <n-input v-model:value="securityForm.oldPassword" type="password" placeholder="请输入当前密码" show-password-on="click" auto-complete="off" />
            </n-form-item>
            <n-form-item label="新密码" path="newPassword">
              <n-input v-model:value="securityForm.newPassword" type="password" placeholder="请输入新密码" show-password-on="click" auto-complete="off" />
            </n-form-item>
            <n-form-item label="确认新密码" path="confirmPassword">
              <n-input v-model:value="securityForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password-on="click" auto-complete="off" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleChangePasswordClick" :loading="passwordChanging">修改密码</n-button>
            </n-form-item>
          </n-form>
        </n-card>

        <n-card title="会话设置" class="mt-6">
          <n-form :model="securityForm" label-placement="top">
            <n-form-item label="启用登录验证码" path="enableCaptcha">
              <n-switch v-model:value="securityForm.enableCaptcha" />
              <span class="ml-2 text-gray-500 text-sm">{{ securityForm.enableCaptcha ? '已启用' : '已禁用' }}</span>
            </n-form-item>
            <n-form-item label="会话超时时间（分钟）" path="sessionTimeout">
              <n-input-number v-model:value="securityForm.sessionTimeout" :min="5" :max="1440" style="width: 200px" />
            </n-form-item>
            <n-form-item label="登录失败最大尝试次数" path="loginAttempts">
              <n-input-number v-model:value="securityForm.loginAttempts" :min="3" :max="10" style="width: 200px" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="saveSecuritySettings" :loading="securitySaving">保存会话设置</n-button>
            </n-form-item>
          </n-form>
        </n-card>
      </n-tab-pane>
    </n-tabs>

    <CaptchaModal
      v-model:show="showPasswordCaptchaModal"
      :api="{ getCaptcha, verifyCaptcha } as any"
      @verified="handlePasswordCaptchaVerified"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, NInputNumber, NTabs, 
  NTabPane, NDynamicTags, NSwitch, NSelect, NRadio, NRadioGroup, NTag, useMessage, NProgress } from 'naive-ui'
import { getSettings, updateSettings, type SiteSettings } from '@/api/settings'
import { useTheme } from '@/composables/useTheme'
import { changePassword, getCaptchaConfig } from '@/api/auth'
import { CaptchaModal } from '@jinynet/webui-comm'
import { getCaptcha, verifyCaptcha } from '@/api/captcha'
import { Sm2Utils } from '@/utils/sm2'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const { applyTheme, applyPresetTheme, defaultTheme } = useTheme()

const validTabs = ['site', 'seo', 'theme', 'security']
const defaultTab = 'site'

const activeTab = ref(
  validTabs.includes((route.query.tab as string) || '') 
    ? (route.query.tab as string) 
    : defaultTab
)

watch(activeTab, (newTab) => {
  if (newTab === defaultTab) {
    router.push({ query: {} })
  } else {
    router.push({ query: { tab: newTab } })
  }
})
const siteSaving = ref(false)
const seoSaving = ref(false)
const themeSaving = ref(false)
const securitySaving = ref(false)
const passwordChanging = ref(false)
const showPasswordCaptchaModal = ref(false)
const passwordCaptchaToken = ref('')
const passwordChangeForm = reactive({
  username: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pendingPasswordChange = ref(false)

const previewSwitchValue = ref(true)
const previewRadioValue = ref('option1')
const previewSelectValue = ref('')
const selectOptions = [
  { label: '选项一', value: '1' },
  { label: '选项二', value: '2' },
  { label: '选项三', value: '3' }
]

const siteForm = ref({
  title: '',
  description: '',
  keywords: [] as string[],
  logo: '',
  favicon: '',
  icp: '',
  securityRecord: '',
  copyright: ''
})

const seoForm = ref({
  seoTitle: '',
  seoDescription: '',
  seoKeywords: [] as string[],
  seoRobots: ''
})

const themeForm = ref({
  themeMode: 'light' as 'light' | 'dark' | 'system',
  primaryColor: '#27272A',
  primaryColorHover: '#3F3F46',
  primaryColorPressed: '#18181B',
  accentColor: '#52525B',
  successColor: '#059669',
  successColorHover: '#10B981',
  successColorPressed: '#047857',
  warningColor: '#B45309',
  warningColorHover: '#D97706',
  warningColorPressed: '#92400E',
  errorColor: '#B91C1C',
  errorColorHover: '#DC2626',
  errorColorPressed: '#991B1B',
  infoColor: '#3B82F6',
  infoColorHover: '#60A5FA',
  infoColorPressed: '#2563EB',
  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  fontSize: 'md' as 'sm' | 'md' | 'lg',
  layoutMode: 'full-width' as 'boxed' | 'full-width',
  animationEnabled: true,
  borderRadius: 'md' as 'sm' | 'md' | 'lg'
})

const activePreset = computed(() => {
  const presetMap: Record<string, { primary: string; accent: string }> = {
    enterprise: { primary: '#2563EB', accent: '#7C3AED' },
    businessBlue: { primary: '#1E40AF', accent: '#0891B2' },
    medical: { primary: '#059669', accent: '#0D9488' },
    deepPurple: { primary: '#7C3AED', accent: '#EC4899' },
    naturalGreen: { primary: '#4D7C0F', accent: '#B45309' },
    minimal: { primary: '#27272A', accent: '#52525B' }
  }
  for (const [key, value] of Object.entries(presetMap)) {
    if (themeForm.value.primaryColor === value.primary && themeForm.value.accentColor === value.accent) {
      return key
    }
  }
  return ''
})

watch(themeForm, (newForm) => {
  applyTheme(newForm)
}, { deep: true })

const fontFamilyOptions = [
  { label: 'System', value: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' },
  { label: 'PingFang SC', value: 'PingFang SC, Microsoft YaHei, sans-serif' },
  { label: 'Noto Sans SC', value: 'Noto Sans SC, sans-serif' },
  { label: 'Source Han Sans', value: 'Source Han Sans SC, sans-serif' }
]

const securityForm = ref({
  username: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  sessionTimeout: 30,
  loginAttempts: 5,
  enableCaptcha: true
})

const fetchSettings = async () => {
  try {
    const response = await getSettings()
    if (response.data) {
      const data = response.data
      if (data.site_title) siteForm.value.title = data.site_title
      if (data.site_description) siteForm.value.description = data.site_description
      if (data.site_keywords) {
        try {
          siteForm.value.keywords = JSON.parse(data.site_keywords)
        } catch {
          siteForm.value.keywords = []
        }
      }
      if (data.site_logo) siteForm.value.logo = data.site_logo
      if (data.site_favicon) siteForm.value.favicon = data.site_favicon
      if (data.site_icp) siteForm.value.icp = data.site_icp
      if (data.site_security_record) siteForm.value.securityRecord = data.site_security_record
      if (data.site_copyright) siteForm.value.copyright = data.site_copyright
      if (data.seo_title) seoForm.value.seoTitle = data.seo_title
      if (data.seo_description) seoForm.value.seoDescription = data.seo_description
      if (data.seo_keywords) {
        try {
          seoForm.value.seoKeywords = JSON.parse(data.seo_keywords)
        } catch {
          seoForm.value.seoKeywords = []
        }
      }
      if (data.seo_robots) seoForm.value.seoRobots = data.seo_robots
      if (data.session_timeout) securityForm.value.sessionTimeout = parseInt(data.session_timeout)
      if (data.login_attempts) securityForm.value.loginAttempts = parseInt(data.login_attempts)
      if (data.enable_captcha !== undefined) securityForm.value.enableCaptcha = data.enable_captcha === 'true'
      if (data.theme_mode) themeForm.value.themeMode = data.theme_mode as 'light' | 'dark' | 'system'
      if (data.primary_color) themeForm.value.primaryColor = data.primary_color
      if (data.primary_color_hover) themeForm.value.primaryColorHover = data.primary_color_hover
      if (data.primary_color_pressed) themeForm.value.primaryColorPressed = data.primary_color_pressed
      if (data.accent_color) themeForm.value.accentColor = data.accent_color
      if (data.success_color) themeForm.value.successColor = data.success_color
      if (data.success_color_hover) themeForm.value.successColorHover = data.success_color_hover
      if (data.success_color_pressed) themeForm.value.successColorPressed = data.success_color_pressed
      if (data.warning_color) themeForm.value.warningColor = data.warning_color
      if (data.warning_color_hover) themeForm.value.warningColorHover = data.warning_color_hover
      if (data.warning_color_pressed) themeForm.value.warningColorPressed = data.warning_color_pressed
      if (data.error_color) themeForm.value.errorColor = data.error_color
      if (data.error_color_hover) themeForm.value.errorColorHover = data.error_color_hover
      if (data.error_color_pressed) themeForm.value.errorColorPressed = data.error_color_pressed
      if (data.info_color) themeForm.value.infoColor = data.info_color
      if (data.info_color_hover) themeForm.value.infoColorHover = data.info_color_hover
      if (data.info_color_pressed) themeForm.value.infoColorPressed = data.info_color_pressed
      if (data.font_family) themeForm.value.fontFamily = data.font_family
      if (data.font_size) themeForm.value.fontSize = data.font_size as 'sm' | 'md' | 'lg'
      if (data.layout_mode) themeForm.value.layoutMode = data.layout_mode as 'boxed' | 'full-width'
      if (data.animation_enabled !== undefined) themeForm.value.animationEnabled = data.animation_enabled === 'true'
      if (data.border_radius) themeForm.value.borderRadius = data.border_radius as 'sm' | 'md' | 'lg'
    }
  } catch (error) {
    console.error('获取设置失败:', error)
  }
}

const saveThemeSettings = async () => {
  try {
    themeSaving.value = true
    const data: Record<string, any> = {
      theme_mode: { value: themeForm.value.themeMode, category: 'theme' },
      primary_color: { value: themeForm.value.primaryColor, category: 'theme' },
      primary_color_hover: { value: themeForm.value.primaryColorHover, category: 'theme' },
      primary_color_pressed: { value: themeForm.value.primaryColorPressed, category: 'theme' },
      accent_color: { value: themeForm.value.accentColor, category: 'theme' },
      success_color: { value: themeForm.value.successColor, category: 'theme' },
      success_color_hover: { value: themeForm.value.successColorHover, category: 'theme' },
      success_color_pressed: { value: themeForm.value.successColorPressed, category: 'theme' },
      warning_color: { value: themeForm.value.warningColor, category: 'theme' },
      warning_color_hover: { value: themeForm.value.warningColorHover, category: 'theme' },
      warning_color_pressed: { value: themeForm.value.warningColorPressed, category: 'theme' },
      error_color: { value: themeForm.value.errorColor, category: 'theme' },
      error_color_hover: { value: themeForm.value.errorColorHover, category: 'theme' },
      error_color_pressed: { value: themeForm.value.errorColorPressed, category: 'theme' },
      info_color: { value: themeForm.value.infoColor, category: 'theme' },
      info_color_hover: { value: themeForm.value.infoColorHover, category: 'theme' },
      info_color_pressed: { value: themeForm.value.infoColorPressed, category: 'theme' },
      font_family: { value: themeForm.value.fontFamily, category: 'theme' },
      font_size: { value: themeForm.value.fontSize, category: 'theme' },
      layout_mode: { value: themeForm.value.layoutMode, category: 'theme' },
      animation_enabled: { value: String(themeForm.value.animationEnabled), category: 'theme' },
      border_radius: { value: themeForm.value.borderRadius, category: 'theme' }
    }
    await updateSettings(data as unknown as SiteSettings)
    applyTheme(themeForm.value)
    message.success('主题配置保存成功')
  } catch (error) {
    console.error('保存主题配置失败:', error)
    message.error('保存失败')
  } finally {
    themeSaving.value = false
  }
}

const applyPreset = (preset: 'enterprise' | 'businessBlue' | 'medical' | 'deepPurple' | 'naturalGreen' | 'minimal') => {
  applyPresetTheme(preset)
  const presets: Record<string, typeof themeForm.value> = {
    enterprise: {
      ...themeForm.value,
      primaryColor: '#2563EB',
      primaryColorHover: '#3B82F6',
      primaryColorPressed: '#1D4ED8',
      accentColor: '#7C3AED',
      successColor: '#059669',
      successColorHover: '#10B981',
      successColorPressed: '#047857',
      warningColor: '#D97706',
      warningColorHover: '#F59E0B',
      warningColorPressed: '#B45309',
      errorColor: '#DC2626',
      errorColorHover: '#EF4444',
      errorColorPressed: '#B91C1C',
      infoColor: '#6366F1',
      infoColorHover: '#818CF8',
      infoColorPressed: '#4F46E5'
    },
    businessBlue: {
      ...themeForm.value,
      primaryColor: '#1E40AF',
      primaryColorHover: '#2563EB',
      primaryColorPressed: '#1E3A8A',
      accentColor: '#0891B2',
      successColor: '#059669',
      successColorHover: '#10B981',
      successColorPressed: '#047857',
      warningColor: '#D97706',
      warningColorHover: '#F59E0B',
      warningColorPressed: '#B45309',
      errorColor: '#DC2626',
      errorColorHover: '#EF4444',
      errorColorPressed: '#B91C1C',
      infoColor: '#3B82F6',
      infoColorHover: '#60A5FA',
      infoColorPressed: '#2563EB'
    },
    medical: {
      ...themeForm.value,
      primaryColor: '#059669',
      primaryColorHover: '#10B981',
      primaryColorPressed: '#047857',
      accentColor: '#0D9488',
      successColor: '#34D399',
      successColorHover: '#6EE7B7',
      successColorPressed: '#10B981',
      warningColor: '#F59E0B',
      warningColorHover: '#FBBF24',
      warningColorPressed: '#D97706',
      errorColor: '#EF4444',
      errorColorHover: '#F87171',
      errorColorPressed: '#DC2626',
      infoColor: '#06B6D4',
      infoColorHover: '#22D3EE',
      infoColorPressed: '#0891B2'
    },
    deepPurple: {
      ...themeForm.value,
      primaryColor: '#7C3AED',
      primaryColorHover: '#8B5CF6',
      primaryColorPressed: '#6D28D9',
      accentColor: '#EC4899',
      successColor: '#059669',
      successColorHover: '#10B981',
      successColorPressed: '#047857',
      warningColor: '#D97706',
      warningColorHover: '#F59E0B',
      warningColorPressed: '#B45309',
      errorColor: '#DC2626',
      errorColorHover: '#EF4444',
      errorColorPressed: '#B91C1C',
      infoColor: '#A78BFA',
      infoColorHover: '#C4B5FD',
      infoColorPressed: '#8B5CF6'
    },
    naturalGreen: {
      ...themeForm.value,
      primaryColor: '#4D7C0F',
      primaryColorHover: '#65A30D',
      primaryColorPressed: '#3F6212',
      accentColor: '#B45309',
      successColor: '#15803D',
      successColorHover: '#16A34A',
      successColorPressed: '#166534',
      warningColor: '#EA580C',
      warningColorHover: '#F97316',
      warningColorPressed: '#C2410C',
      errorColor: '#DC2626',
      errorColorHover: '#EF4444',
      errorColorPressed: '#B91C1C',
      infoColor: '#0E7490',
      infoColorHover: '#0891B2',
      infoColorPressed: '#155E75'
    },
    minimal: {
      ...themeForm.value,
      primaryColor: '#27272A',
      primaryColorHover: '#3F3F46',
      primaryColorPressed: '#18181B',
      accentColor: '#52525B',
      successColor: '#059669',
      successColorHover: '#10B981',
      successColorPressed: '#047857',
      warningColor: '#B45309',
      warningColorHover: '#D97706',
      warningColorPressed: '#92400E',
      errorColor: '#B91C1C',
      errorColorHover: '#DC2626',
      errorColorPressed: '#991B1B',
      infoColor: '#3B82F6',
      infoColorHover: '#60A5FA',
      infoColorPressed: '#2563EB'
    }
  }
  Object.assign(themeForm.value, presets[preset])
}

const resetThemeSettings = () => {
  themeForm.value.themeMode = defaultTheme.themeMode
  themeForm.value.primaryColor = defaultTheme.primaryColor
  themeForm.value.primaryColorHover = defaultTheme.primaryColorHover
  themeForm.value.primaryColorPressed = defaultTheme.primaryColorPressed
  themeForm.value.accentColor = defaultTheme.accentColor
  themeForm.value.successColor = defaultTheme.successColor
  themeForm.value.successColorHover = defaultTheme.successColorHover
  themeForm.value.successColorPressed = defaultTheme.successColorPressed
  themeForm.value.warningColor = defaultTheme.warningColor
  themeForm.value.warningColorHover = defaultTheme.warningColorHover
  themeForm.value.warningColorPressed = defaultTheme.warningColorPressed
  themeForm.value.errorColor = defaultTheme.errorColor
  themeForm.value.errorColorHover = defaultTheme.errorColorHover
  themeForm.value.errorColorPressed = defaultTheme.errorColorPressed
  themeForm.value.infoColor = defaultTheme.infoColor
  themeForm.value.infoColorHover = defaultTheme.infoColorHover
  themeForm.value.infoColorPressed = defaultTheme.infoColorPressed
  themeForm.value.fontFamily = defaultTheme.fontFamily
  themeForm.value.fontSize = defaultTheme.fontSize
  themeForm.value.layoutMode = defaultTheme.layoutMode
  themeForm.value.animationEnabled = defaultTheme.animationEnabled
  themeForm.value.borderRadius = defaultTheme.borderRadius
  message.success('主题配置已重置为默认')
}

const saveSiteSettings = async () => {
  try {
    siteSaving.value = true
    const data: Record<string, any> = {
      site_title: { value: siteForm.value.title, category: 'site' },
      site_description: { value: siteForm.value.description, category: 'site' },
      site_keywords: { value: JSON.stringify(siteForm.value.keywords), category: 'site' },
      site_logo: { value: siteForm.value.logo || null, category: 'site' },
      site_favicon: { value: siteForm.value.favicon || null, category: 'site' },
      site_icp: { value: siteForm.value.icp || null, category: 'site' },
      site_security_record: { value: siteForm.value.securityRecord || null, category: 'site' },
      site_copyright: { value: siteForm.value.copyright || null, category: 'site' }
    }
    await updateSettings(data as unknown as SiteSettings)
    message.success('网站配置保存成功')
  } catch (error) {
    console.error('保存网站配置失败:', error)
    message.error('保存失败')
  } finally {
    siteSaving.value = false
  }
}

const saveSeoSettings = async () => {
  try {
    seoSaving.value = true
    const data: Record<string, any> = {
      seo_title: { value: seoForm.value.seoTitle, category: 'seo' },
      seo_description: { value: seoForm.value.seoDescription, category: 'seo' },
      seo_keywords: { value: JSON.stringify(seoForm.value.seoKeywords), category: 'seo' },
      seo_robots: { value: seoForm.value.seoRobots, category: 'seo' }
    }
    await updateSettings(data as unknown as SiteSettings)
    message.success('SEO设置保存成功')
  } catch (error) {
    console.error('保存SEO设置失败:', error)
    message.error('保存失败')
  } finally {
    seoSaving.value = false
  }
}

const saveSecuritySettings = async () => {
  try {
    securitySaving.value = true
    const data: Record<string, any> = {
      enable_captcha: { value: String(securityForm.value.enableCaptcha), category: 'security' },
      session_timeout: { value: String(securityForm.value.sessionTimeout), category: 'security' },
      login_attempts: { value: String(securityForm.value.loginAttempts), category: 'security' }
    }
    await updateSettings(data as unknown as SiteSettings)
    message.success('会话设置保存成功')
  } catch (error) {
    console.error('保存会话设置失败:', error)
    message.error('保存失败')
  } finally {
    securitySaving.value = false
  }
}

const handleChangePasswordClick = async () => {
  if (!securityForm.value.username) {
    message.error('请输入管理员用户名')
    return
  }
  if (!securityForm.value.oldPassword) {
    message.error('请输入当前密码')
    return
  }
  if (!securityForm.value.newPassword) {
    message.error('请输入新密码')
    return
  }
  if (securityForm.value.newPassword !== securityForm.value.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  if (securityForm.value.newPassword.length < 6) {
    message.error('新密码长度不能少于6位')
    return
  }

  try {
    const response = await getCaptchaConfig()
    const captchaEnabled = response.data?.enabled
    if (captchaEnabled) {
      passwordChangeForm.username = securityForm.value.username
      passwordChangeForm.oldPassword = securityForm.value.oldPassword
      passwordChangeForm.newPassword = securityForm.value.newPassword
      passwordChangeForm.confirmPassword = securityForm.value.confirmPassword
      pendingPasswordChange.value = true
      showPasswordCaptchaModal.value = true
    } else {
      await executeChangePassword(securityForm.value.username, securityForm.value.oldPassword, securityForm.value.newPassword, '')
    }
  } catch (error) {
    console.error('获取验证码配置失败:', error)
    await executeChangePassword(securityForm.value.username, securityForm.value.oldPassword, securityForm.value.newPassword, '')
  }
}

const handlePasswordCaptchaVerified = async (token: string) => {
  passwordCaptchaToken.value = token
  showPasswordCaptchaModal.value = false
  
  if (pendingPasswordChange.value) {
    await executeChangePassword(passwordChangeForm.username, passwordChangeForm.oldPassword, passwordChangeForm.newPassword, passwordCaptchaToken.value)
    pendingPasswordChange.value = false
  }
}

const executeChangePassword = async (username: string, oldPassword: string, newPassword: string, captchaToken: string) => {
  try {
    passwordChanging.value = true

    let encryptedUsername = username
    let encryptedOldPassword = oldPassword
    let encryptedNewPassword = newPassword

    if (captchaToken) {
      encryptedUsername = Sm2Utils.encrypt(captchaToken, username)
      encryptedOldPassword = Sm2Utils.encrypt(captchaToken, oldPassword)
      encryptedNewPassword = Sm2Utils.encrypt(captchaToken, newPassword)
    }
    
    await changePassword(encryptedUsername, encryptedOldPassword, encryptedNewPassword, captchaToken)
    message.success('密码修改成功')
    securityForm.value.username = ''
    securityForm.value.oldPassword = ''
    securityForm.value.newPassword = ''
    securityForm.value.confirmPassword = ''
  } catch (error: any) {
    console.error('修改密码失败:', error)
    message.error(error?.message || '修改密码失败')
  } finally {
    passwordChanging.value = false
    passwordCaptchaToken.value = ''
    passwordChangeForm.oldPassword = ''
    passwordChangeForm.newPassword = ''
    passwordChangeForm.confirmPassword = ''
  }
}

onMounted(() => {
  fetchSettings()
})
</script>