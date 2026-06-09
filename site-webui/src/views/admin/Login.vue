<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
    <n-card class="w-full max-w-md">
      <template #header>
        <div class="flex items-center justify-center">
          <div class="w-12 h-12 bg-gray-900 rounded-lg flex items-center justify-center">
            <Person class="w-6 h-6 text-white" />
          </div>
          <h1 class="text-xl font-bold text-dark ml-3">管理员登录</h1>
        </div>
      </template>

      <!-- 登录方式切换 -->
      <n-tabs v-model:value="loginType" type="segment" class="mb-6">
        <n-tab name="password">密码登录</n-tab>
        <n-tab name="email">邮箱登录</n-tab>
      </n-tabs>

      <!-- 密码登录表单 -->
      <n-form v-if="loginType === 'password'" :model="pwdForm" class="mt-4">
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="pwdForm.username"
            placeholder="请输入用户名"
            autofocus
          >
            <template #prefix>
              <Person class="w-4 h-4" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="pwdForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
          >
            <template #prefix>
              <Lock class="w-4 h-4" />
            </template>
            <template #suffix>
              <n-button text @click="showPassword = !showPassword">
                <Eye class="w-4 h-4" />
              </n-button>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <div class="flex items-center justify-between w-full">
            <n-checkbox v-model="pwdForm.remember">记住密码</n-checkbox>
            <div class="ml-auto">
              <a href="#" class="text-gray-900 hover:underline text-sm">忘记密码？</a>
            </div>
          </div>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            block
            @click="handlePwdLogin"
            :loading="loading"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>

      <!-- 邮箱登录表单 -->
      <n-form v-else :model="emailForm" class="mt-4">
        <n-form-item label="邮箱" path="email">
          <n-input
            v-model:value="emailForm.email"
            placeholder="请输入邮箱地址"
            autofocus
          >
            <template #prefix>
              <Mail class="w-4 h-4" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="验证码" path="code">
          <div class="flex gap-2 w-full">
            <n-input
              v-model:value="emailForm.code"
              placeholder="请输入验证码"
              maxlength="6"
              class="flex-1"
            >
              <template #prefix>
                <Lock class="w-4 h-4" />
              </template>
            </n-input>
            <n-button
              @click="handleSendEmailCode"
              :loading="sendingCode"
              :disabled="codeCountdown > 0"
              class="min-w-28"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
            </n-button>
          </div>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            block
            @click="handleEmailLogin"
            :loading="loading"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>

    <!-- 密码登录滑块验证码弹窗 -->
    <CaptchaModal
      v-model:show="showPwdCaptchaModal"
      :api="{ getCaptcha, verifyCaptcha } as any"
      @verified="handlePwdCaptchaVerified"
    />

    <!-- 邮箱验证码滑块验证弹窗 -->
    <CaptchaModal
      v-model:show="showEmailCaptchaModal"
      :api="{ getCaptcha, verifyCaptcha } as any"
      @verified="handleEmailCaptchaVerified"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Person, Lock, Eye, Mail } from '@/icons'
import { NCard, NForm, NFormItem, NInput, NButton, NCheckbox, NTabs, NTab, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getCaptchaConfig } from '@/api/auth'
import { getCaptcha, verifyCaptcha, sendEmailCaptcha } from '@/api/captcha'
import { CaptchaModal } from '@jinynet/webui-comm'
import { Sm2Utils } from '@/utils/sm2'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const redirectPath = computed(() => {
  return route.query.redirect as string || '/admin'
})

// 登录方式
const loginType = ref<'password' | 'email'>('password')

// 密码登录表单
const pwdForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 邮箱登录表单
const emailForm = reactive({
  email: '',
  code: ''
})

const showPassword = ref(false)
const loading = ref(false)
const showPwdCaptchaModal = ref(false)
const showEmailCaptchaModal = ref(false)
const verifyToken = ref('')
const captchaEnabled = ref(false)

// 邮箱验证码状态
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null
const emailCaptchaId = ref('')

const fetchCaptchaConfig = async () => {
  try {
    const response = await getCaptchaConfig()
    if (response.data) {
      captchaEnabled.value = response.data.enabled
    }
  } catch (error) {
    console.error('获取验证码配置失败:', error)
  }
}

// ==================== 密码登录 ====================

const handlePwdLogin = () => {
  if (!pwdForm.username || !pwdForm.password) {
    message.warning('请输入用户名和密码')
    return
  }

  if (captchaEnabled.value) {
    showPwdCaptchaModal.value = true
  } else {
    executePwdLoginWithoutCaptcha()
  }
}

const executePwdLoginWithoutCaptcha = async () => {
  loading.value = true
  try {
    await authStore.handleLogin(pwdForm.username, pwdForm.password)
    if (pwdForm.remember) {
      localStorage.setItem('remember', 'true')
    }
    router.push(redirectPath.value)
  } catch (error: any) {
    message.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handlePwdCaptchaVerified = (token: string) => {
  verifyToken.value = token
  message.success('验证成功')
  showPwdCaptchaModal.value = false
  executePwdLogin()
}

const executePwdLogin = async () => {
  if (!verifyToken.value) return

  loading.value = true
  try {
    const encryptedPassword = Sm2Utils.encrypt(verifyToken.value, pwdForm.password)
    await authStore.handleLogin(pwdForm.username, encryptedPassword, verifyToken.value)
    if (pwdForm.remember) {
      localStorage.setItem('remember', 'true')
    }
    router.push(redirectPath.value)
  } catch (error: any) {
    message.error(error?.message || '登录失败')
    verifyToken.value = ''
  } finally {
    loading.value = false
  }
}

// ==================== 邮箱登录 ====================

const handleSendEmailCode = () => {
  if (!emailForm.email) {
    message.warning('请输入邮箱地址')
    return
  }

  // 邮箱格式校验
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.email)) {
    message.warning('请输入正确的邮箱格式')
    return
  }

  // 弹出滑块验证
  showEmailCaptchaModal.value = true
}

const handleEmailCaptchaVerified = async (verifyToken: string) => {
  try {
    sendingCode.value = true
    showEmailCaptchaModal.value = false

    // verifyToken 是滑块验证通过后的一次性 token
    // 直接使用它发送邮箱验证码
    const response = await sendEmailCaptcha(emailForm.email, verifyToken, '0')
    if (response.data) {
      emailCaptchaId.value = response.data
      message.success('验证码已发送')
      startCountdown()
    }
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    message.error(error?.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// 倒计时
const startCountdown = () => {
  codeCountdown.value = 60
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000)
}

// 邮箱登录
const handleEmailLogin = async () => {
  if (!emailForm.email) {
    message.warning('请输入邮箱地址')
    return
  }

  if (!emailForm.code) {
    message.warning('请输入验证码')
    return
  }

  if (!emailCaptchaId.value) {
    message.warning('请先发送验证码')
    return
  }

  loading.value = true
  try {
    await authStore.handleEmailLogin(emailForm.email, emailCaptchaId.value, emailForm.code)
    router.push(redirectPath.value)
  } catch (error: any) {
    message.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCaptchaConfig()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>
