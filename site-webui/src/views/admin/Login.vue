<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center">
    <n-card class="w-full max-w-md">
      <template #header>
        <div class="flex items-center justify-center">
          <div class="w-12 h-12 bg-gray-900 rounded-lg flex items-center justify-center">
            <Person class="w-6 h-6 text-white" />
          </div>
          <h1 class="text-xl font-bold text-dark ml-3">管理员登录</h1>
        </div>
      </template>
      
      <n-form :model="form" class="mt-6">
        <n-form-item label="用户名" path="username">
          <n-input 
            v-model:value="form.username" 
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
            v-model:value="form.password" 
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
            <n-checkbox v-model="form.remember">记住密码</n-checkbox>
            <div class="ml-auto">
              <a href="#" class="text-gray-900 hover:underline text-sm">忘记密码？</a>
            </div>
          </div>
        </n-form-item>
        
        <n-form-item>
          <n-button 
            type="primary"
            block 
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>

    <CaptchaModal
      v-model:show="showCaptchaModal"
      :api="{ getCaptcha, verifyCaptcha } as any"
      @verified="handleCaptchaVerified"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Person, Lock, Eye } from '@/icons'
import { NCard, NForm, NFormItem, NInput, NButton, NCheckbox, useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getCaptchaConfig } from '@/api/auth'
import { CaptchaModal } from '@jinynet/webui-comm'
import { getCaptcha, verifyCaptcha } from '@/api/captcha'
import { Sm2Utils } from '@/utils/sm2'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const redirectPath = computed(() => {
  return route.query.redirect as string || '/admin'
})

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const showPassword = ref(false)
const loading = ref(false)
const showCaptchaModal = ref(false)
const verifyToken = ref('')
const captchaEnabled = ref(false)

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

const handleCaptchaVerified = (token: string) => {
  verifyToken.value = token
  message.success('验证成功')
  showCaptchaModal.value = false
  executeLogin()
}

const handleLogin = () => {
  if (!form.username || !form.password) {
    message.warning('请输入用户名和密码')
    return
  }
  
  if (captchaEnabled.value) {
    showCaptchaModal.value = true
  } else {
    executeLoginWithoutCaptcha()
  }
}

const executeLoginWithoutCaptcha = async () => {
  loading.value = true
  try {
    await authStore.handleLogin(form.username, form.password)
    if (form.remember) {
      localStorage.setItem('remember', 'true')
    }
    router.push(redirectPath.value)
  } catch (error: any) {
    console.error('Login failed:', error)
    message.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const executeLogin = async () => {
  if (!verifyToken.value) return
  
  loading.value = true
  try {
    // 使用verifyToken（SM2公钥）加密密码
    const encryptedPassword = Sm2Utils.encrypt(verifyToken.value, form.password)
    await authStore.handleLogin(form.username, encryptedPassword, verifyToken.value)
    if (form.remember) {
      localStorage.setItem('remember', 'true')
    }
    router.push(redirectPath.value)
  } catch (error: any) {
    console.error('Login failed:', error)
    message.error(error?.message || '登录失败')
    verifyToken.value = ''
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCaptchaConfig()
})
</script>