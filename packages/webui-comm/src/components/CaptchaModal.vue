<template>
  <n-modal
    :show="show"
    preset="card"
    title="安全验证"
    :style="{ width: Math.min(420, windowWidth - 32) + 'px' }"
    :mask-closable="false"
    @update:show="$emit('update:show', $event)"
  >
    <SliderCaptcha
      ref="captchaRef"
      :api="api as any"
      :show-header="false"
      @verified="onVerified"
      @failed="onFailed"
    />
  </n-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { NModal } from 'naive-ui'
import SliderCaptcha from './SliderCaptcha.vue'

defineProps<{
  show: boolean
  api: any  // CaptchaAPI — TS 与 Axios 泛型有类型断层，使用 any 桥接
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  verified: [token: string]
}>()

const captchaRef = ref<InstanceType<typeof SliderCaptcha> | null>(null)
const windowWidth = ref(window.innerWidth)

function onWindowResize() { windowWidth.value = window.innerWidth }

function onVerified(token: string) {
  emit('verified', token)
  emit('update:show', false)
}

function onFailed() {
  // 验证失败不清除弹窗，用户可重试或手动关闭
}

onMounted(() => window.addEventListener('resize', onWindowResize))
onUnmounted(() => window.removeEventListener('resize', onWindowResize))
</script>
