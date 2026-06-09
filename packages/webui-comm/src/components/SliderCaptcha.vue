<template>
  <div
    class="jn-captcha"
    :class="{ 
      'jn-captcha--verified': state === 'success',
      'jn-captcha--failed': state === 'failed'
    }"
  >
    <div v-if="showHeader" class="jn-captcha__header">
      <span class="jn-captcha__title">安全验证</span>
      <n-button text size="tiny" :disabled="state === 'verifying'" @click="refresh" aria-label="刷新验证码">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="jn-captcha__refresh-icon">
            <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </template>
      </n-button>
    </div>
    <div v-if="captcha" class="jn-captcha__body">
      <div 
        ref="bgRef" 
        class="jn-captcha__bg" 
        :class="{ 
          'jn-captcha__bg--dragging': dragging,
          'jn-captcha__bg--verifying': state === 'verifying',
          'jn-captcha__bg--success': state === 'success',
          'jn-captcha__bg--failed': state === 'failed'
        }"
        :style="{ backgroundImage: `url(${bgUrl})` }"
      >
        <div 
          class="jn-captcha__slot" 
          :class="{ 
            'jn-captcha__slot--dragging': dragging,
            'jn-captcha__slot--verifying': state === 'verifying',
            'jn-captcha__slot--success': state === 'success'
          }"
          :style="slotStyle" 
        />
        <div
          class="jn-captcha__track"
          :class="{ 
            'jn-captcha__track--dragging': dragging,
            'jn-captcha__track--verifying': state === 'verifying',
            'jn-captcha__track--success': state === 'success'
          }"
          :style="trackStyle"
          @mousedown.prevent="onStart($event, 'mouse')"
          @touchstart.prevent="onStart($event, 'touch')"
        >
          <div v-if="captcha?.slider" class="jn-captcha__block" :style="sliderImageStyle" />
          <div v-else class="jn-captcha__block">
            <svg v-if="state !== 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" /></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12l5 5L19 7" /></svg>
          </div>
        </div>
        <div 
          v-if="dragging" 
          class="jn-captcha__progress"
          :style="{ width: progressPercent + '%' }"
        />
      </div>
    </div>
    <div 
      class="jn-captcha__tip" 
      :class="`jn-captcha__tip--${state}`"
    >
      <span v-if="state === 'loading'" class="jn-captcha__tip-content">
        <span class="jn-captcha__spinner"></span>
        加载中...
      </span>
      <span v-else-if="state === 'ready'" class="jn-captcha__tip-content">
        <svg class="jn-captcha__tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
        拖动滑块完成拼图
      </span>
      <span v-else-if="state === 'verifying'" class="jn-captcha__tip-content">
        <span class="jn-captcha__spinner"></span>
        验证中...
      </span>
      <span v-else-if="state === 'success'" class="jn-captcha__tip-content">
        <svg class="jn-captcha__tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 13l4 4L19 7"/></svg>
        验证通过
      </span>
      <span v-else class="jn-captcha__tip-content">
        <svg class="jn-captcha__tip-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
        {{ errorMsg || '验证失败，请重试' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { NButton, useThemeVars } from 'naive-ui'

export interface CaptchaVO { id: string; background: string; slider?: string; x: number; y?: number; sliderWidth?: number; sliderHeight?: number }
export interface CaptchaAPI<T extends CaptchaVO = CaptchaVO> {
  getCaptcha: () => Promise<{ data: T }>
  verifyCaptcha: (id: string, position: number, extra?: Record<string, unknown>) => Promise<{ data: string }>
}

const themeVars = useThemeVars()

const props = withDefaults(defineProps<{
  api: CaptchaAPI
  blockSize?: number
  tolerance?: number
  /** 是否显示内置标题栏，在 CaptchaModal 中应设为 false */
  showHeader?: boolean
}>(), {
  blockSize: 0,
  tolerance: 6,
  showHeader: true,
})

const emit = defineEmits<{
  (e: 'verified', token: string): void
  (e: 'failed'): void
}>()

type State = 'loading' | 'ready' | 'dragging' | 'verifying' | 'success' | 'failed'

const state = ref<State>('loading')
const captcha = ref<CaptchaVO | null>(null)
const position = ref(0)
const dragging = ref(false)
const containerWidth = ref(330)
const errorMsg = ref('')
const startX = ref(0)
let trail: number[] = []

const bgRef = ref<HTMLElement | null>(null)
const bgNaturalSize = ref({ w: 512, h: 288 })
let observer: ResizeObserver | null = null

const sliderSize = () => props.blockSize > 0 ? props.blockSize : (captcha.value?.sliderWidth ?? 60) - 20
const maxPosition = () => containerWidth.value - sliderSize()
const scaleX = computed(() => containerWidth.value / bgNaturalSize.value.w)
/** 容器高度（aspect-ratio: 16/9） */
const containerHeight = computed(() => containerWidth.value * 9 / 16)

/** 拖动进度百分比 */
const progressPercent = computed(() => {
  return Math.round((position.value / maxPosition()) * 100)
})
// const scaleY = scaleX.value
const scaleY = computed(() => containerHeight.value / bgNaturalSize.value.h)
/** 将容器坐标转换为原始图片坐标，用于后端验证 */
const scaledPosition = computed(() => {
  if (!bgNaturalSize.value.w || !containerWidth.value) return position.value
  return Math.round(position.value * bgNaturalSize.value.w / containerWidth.value)
})

/** 背景图 URL：自动补全 base64 前缀 */
const bgUrl = computed(() => {
  const bg = captcha.value?.background
  if (!bg) return ''
  if (bg.startsWith('data:') || bg.startsWith('http')) return bg
  return `data:image/png;base64,${bg}`
})

const slotStyle = computed(() => ({
  left: (captcha.value?.x ?? 0) * scaleX.value + 'px',
  top: (captcha.value?.y ?? 0) * scaleY.value + 'px',
  width: sliderSize() + 'px',
  height: sliderSize() + 'px',
}))

const trackStyle = computed(() => ({
  left: position.value + 'px',
  top: (captcha.value?.y ?? 0) * scaleY.value + 'px',
  transition: state.value === 'ready' ? 'left 0.3s ease' : 'none',
  width: sliderSize() + 'px',
  height: sliderSize() + 'px',
}))

/** 滑块图片 URL：自动补全 base64 前缀 */
const sliderImageUrl = computed(() => {
  const slider = captcha.value?.slider
  if (!slider) return ''
  if (slider.startsWith('data:') || slider.startsWith('http')) return slider
  return `data:image/png;base64,${slider}`
})

const sliderImageStyle = computed(() => ({
  width: '100%',
  height: '100%',
  backgroundImage: `url(${sliderImageUrl.value})`,
  backgroundSize: '100% 100%',
  backgroundRepeat: 'no-repeat',
  backgroundPosition: 'center',
  borderRadius: '6px',
}))

async function refresh() {
  if (state.value === 'verifying') return
  state.value = 'loading'; errorMsg.value = ''; position.value = 0; trail = []
  try {
    const res = await props.api.getCaptcha()
    captcha.value = res.data as unknown as CaptchaVO
    // 解析图片实际尺寸用于缩放计算
    const img = new Image()
    img.onload = () => { 
      bgNaturalSize.value = { w: img.naturalWidth, h: img.naturalHeight }
      state.value = 'ready'
    }
    img.onerror = () => {
      // 如果图片加载失败，使用默认尺寸
      bgNaturalSize.value = { w: 320, h: 160 }
      state.value = 'ready'
    }
    // bgUrl 已处理 base64 前缀，直接使用
    img.src = bgUrl.value
  } catch {
    errorMsg.value = '获取验证码失败'
    state.value = 'failed'
  }
}

function onStart(e: MouseEvent | TouchEvent, type: 'mouse' | 'touch') {
  if (state.value === 'success' || state.value === 'verifying') return
  state.value = 'dragging'; dragging.value = true
  if (type === 'touch') {
    startX.value = (e as TouchEvent).touches[0].clientX
    trail = [(e as TouchEvent).touches[0].clientX]
  } else {
    startX.value = (e as MouseEvent).clientX
    trail = [(e as MouseEvent).clientX]
  }
}

function onMove(e: MouseEvent | TouchEvent) {
  if (!dragging.value || !captcha.value) return
  if ('touches' in e) {
    e.preventDefault()
  }
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  position.value = Math.max(0, Math.min(clientX - startX.value, maxPosition()))
  if ('touches' in e) {
    trail.push(e.touches[0].clientX)
  } else {
    trail.push(clientX)
  }
}

async function onEnd() {
  if (!dragging.value || !captcha.value) return
  dragging.value = false
  if (state.value === 'success') return
  state.value = 'verifying'
  try {
    const res = await props.api.verifyCaptcha(captcha.value!.id, scaledPosition.value, { trail, containerWidth: containerWidth.value, bgWidth: bgNaturalSize.value.w })
    if (res.data) {
      state.value = 'success'
      emit('verified', res.data as unknown as string)
    } else {
      state.value = 'failed'; errorMsg.value = '验证失败'
      emit('failed'); setTimeout(refresh, 1200)
    }
  } catch (e: unknown) {
    state.value = 'failed'; errorMsg.value = (e as Error)?.message || '验证失败'
    emit('failed'); setTimeout(refresh, 1200)
  }
}

onMounted(() => {
  refresh()
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onEnd)
  document.addEventListener('touchmove', onMove, { passive: false })
  document.addEventListener('touchend', onEnd)
  
  if (bgRef.value) {
    containerWidth.value = bgRef.value.offsetWidth
    observer = new ResizeObserver(([e]) => { if (e) containerWidth.value = e.contentRect.width })
    observer.observe(bgRef.value)
  }
  window.addEventListener('resize', () => {
    if (bgRef.value) containerWidth.value = bgRef.value.offsetWidth
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMove)
  document.removeEventListener('mouseup', onEnd)
  document.removeEventListener('touchmove', onMove)
  document.removeEventListener('touchend', onEnd)
  observer?.disconnect()
})

defineExpose({ refresh, state })
</script>

<style scoped>
.jn-captcha {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  border: 1px solid v-bind("themeVars.borderColor");
  border-radius: v-bind("themeVars.borderRadius");
  overflow: hidden;
  background: v-bind("themeVars.cardColor");
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.jn-captcha__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid v-bind("themeVars.dividerColor");
  background: linear-gradient(180deg, rgba(255,255,255,0.5) 0%, transparent 100%);
}
.jn-captcha__title {
  font-size: 14px;
  font-weight: 600;
  color: v-bind("themeVars.textColor1");
}
.jn-captcha__refresh-icon { 
  width: 18px; 
  height: 18px; 
  transition: transform 0.3s ease;
}
.jn-captcha__header :deep(.n-button):hover .jn-captcha__refresh-icon {
  transform: rotate(180deg);
}
.jn-captcha__body { padding: 16px; }
.jn-captcha__bg {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-size: 100% 100%;
  border-radius: 8px;
  position: relative;
  user-select: none;
  overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.15);
  touch-action: none;
}
.jn-captcha__slot {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(2px);
  border-radius: 6px;
  border: 2px dashed rgba(255, 255, 255, 0.6);
  box-sizing: border-box;
  pointer-events: none;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.15);
}
.jn-captcha__track { 
  position: absolute; 
  cursor: grab; 
  z-index: 10;
  transition: transform 0.1s ease-out;
  touch-action: none;
  background: linear-gradient(145deg, #ffffff 0%, #f5f5f5 100%);
  border-radius: 6px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 1px 2px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(200, 200, 200, 0.5);
  box-sizing: border-box;
}
.jn-captcha__track--dragging { 
  cursor: grabbing; 
  transform: scale(1.02);
}
.jn-captcha__block {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: v-bind("themeVars.textColor2");
  border-radius: 4px;
}
.jn-captcha__block svg { 
  width: 55%; 
  height: 55%; 
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.1));
}
.jn-captcha__track--dragging { 
  cursor: grabbing; 
  transform: scale(1.02);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.25),
    0 2px 6px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  border-color: v-bind("themeVars.primaryColor");
}
.jn-captcha--verified .jn-captcha__track {
  background: linear-gradient(145deg, v-bind("themeVars.successColor") 0%, v-bind("themeVars.successColorSuppl") 100%);
  border-color: v-bind("themeVars.successColor");
  box-shadow: 
    0 4px 12px rgba(v-bind("themeVars.successColor"), 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  animation: successPulse 0.4s ease-out;
}
.jn-captcha--verified .jn-captcha__block {
  color: #fff;
}
.jn-captcha--verified .jn-captcha__block svg {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}
.jn-captcha__tip {
  padding: 12px 16px;
  font-size: 13px;
  text-align: center;
  border-top: 1px solid v-bind("themeVars.dividerColor");
  color: v-bind("themeVars.textColor3");
  transition: all 0.3s ease;
  background: linear-gradient(180deg, transparent 0%, rgba(0,0,0,0.02) 100%);
}
.jn-captcha__tip--verifying { 
  color: v-bind("themeVars.infoColor"); 
}
.jn-captcha__tip--success { 
  color: v-bind("themeVars.successColor"); 
  font-weight: 500;
}
.jn-captcha__tip--failed { 
  color: v-bind("themeVars.errorColor"); 
}
.jn-captcha--verified .jn-captcha__tip {
  background: linear-gradient(180deg, rgba(v-bind("themeVars.successColor"), 0.05) 0%, transparent 100%);
}
.jn-captcha--verified {
  border-color: rgba(v-bind("themeVars.successColor"), 0.3);
}
.jn-captcha--failed {
  border-color: rgba(v-bind("themeVars.errorColor"), 0.3);
}
.jn-captcha--failed .jn-captcha__tip {
  background: linear-gradient(180deg, rgba(v-bind("themeVars.errorColor"), 0.05) 0%, transparent 100%);
}
.jn-captcha__bg--dragging {
  filter: brightness(0.95);
}
.jn-captcha__bg--success {
  animation: bgSuccess 0.5s ease-out;
}
.jn-captcha__bg--failed {
  animation: bgShake 0.4s ease-out;
}
.jn-captcha__slot--dragging {
  border-style: solid;
  border-color: rgba(v-bind("themeVars.primaryColor"), 0.6);
  background: rgba(v-bind("themeVars.primaryColor"), 0.1);
}
.jn-captcha__slot--success {
  border-style: solid;
  border-color: rgba(v-bind("themeVars.successColor"), 0.8);
  background: rgba(v-bind("themeVars.successColor"), 0.2);
  animation: slotSuccess 0.4s ease-out;
}
.jn-captcha__track--success {
  animation: trackSnap 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}
.jn-captcha__progress {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  background: linear-gradient(90deg, v-bind("themeVars.primaryColor") 0%, v-bind("themeVars.primaryColorSuppl") 100%);
  border-radius: 0 2px 0 0;
  transition: width 0.05s linear;
  box-shadow: 0 0 8px rgba(v-bind("themeVars.primaryColor"), 0.5);
}
.jn-captcha__tip-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.jn-captcha__tip-icon {
  width: 14px;
  height: 14px;
}
.jn-captcha__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-top-color: v-bind("themeVars.primaryColor");
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes successPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
@keyframes bgSuccess {
  0% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
  100% { filter: brightness(1); }
}
@keyframes bgShake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
  20%, 40%, 60%, 80% { transform: translateX(4px); }
}
@keyframes slotSuccess {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}
@keyframes trackSnap {
  0% { transform: translateX(0); }
  50% { transform: translateX(3px); }
  100% { transform: translateX(0); }
}
</style>
