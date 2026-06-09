<template>
  <div class="tab-content">
    <div class="space-y-4">
      <!-- 有暂存的暂停任务：显示恢复入口 -->
      <div v-if="!largeFile && pendingUpload" class="border-2 border-dashed border-blue-300 bg-blue-50 dark:bg-blue-900/20 rounded-lg p-6 text-center">
        <Folder class="w-10 h-10 text-blue-500 mx-auto mb-3" />
        <p class="font-medium text-blue-700 dark:text-blue-300 mb-1">有暂停的上传任务</p>
        <p class="text-sm text-blue-600 dark:text-blue-400 mb-3">{{ pendingUpload.fileName }} · {{ formatSize(pendingUpload.fileSize) }} · 已上传 {{ pendingUpload.uploadedChunks }}/{{ pendingUpload.totalChunks }} 分片</p>
        <div class="flex items-center justify-center gap-3">
          <n-button type="primary" size="small" @click="triggerLargeInput">选择相同文件以继续</n-button>
          <n-button size="small" @click="discardPending">放弃</n-button>
        </div>
        <input ref="largeInput" type="file" class="hidden" @change="onLargeFileSelect" />
      </div>

      <!-- 无文件选中：普通选择 -->
      <div v-else-if="!largeFile" class="border-2 border-dashed border-gray-200 rounded-lg p-8 text-center cursor-pointer hover:border-blue-400 transition-colors" @click="triggerLargeInput">
        <Upload class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-lg mb-1">选择大文件</p>
        <p class="text-sm text-gray-400">支持分片上传与断点续传，最大 10GB</p>
        <input ref="largeInput" type="file" class="hidden" @change="onLargeFileSelect" />
      </div>
      <template v-else>
        <div class="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <Folder class="w-10 h-10 text-blue-500 flex-shrink-0" />
          <div class="flex-1 min-w-0">
            <p class="font-medium truncate">{{ largeFile.name }}</p>
            <p class="text-sm text-gray-500">{{ formatSize(largeFile.size) }} · {{ totalChunks }} 分片</p>
          </div>
          <n-button size="small" @click="clearLarge" :disabled="uploading">更换</n-button>
        </div>
        <!-- 上传进度 -->
        <div v-if="uploading" class="space-y-2">
          <div class="flex justify-between text-sm">
            <span>{{ isHash ? '初始化...' : uploadPhase === 'merge' ? '合并中...' : '上传中' }}</span>
            <span v-if="!isHash">{{ progress }}%</span>
          </div>
          <n-progress v-if="!isHash" type="line" :percentage="progress" :height="8" :show-indicator="false" :status="uploadStatus" />
          <n-progress v-else type="line" :height="8" :show-indicator="false" processing />
          <div v-if="!isHash" class="flex justify-between text-xs text-gray-400"><span>{{ uploadedC }} / {{ totalChunks }} 分片</span><span v-if="speed">{{ formatSpeed(speed) }}</span></div>
        </div>
        <div v-if="error" class="p-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded text-sm">{{ error }}</div>
        <div v-if="largeFileTooSmall" class="p-3 bg-yellow-50 dark:bg-yellow-900/20 text-yellow-700 dark:text-yellow-400 rounded text-sm">
          文件较小（{{ formatSize(largeFile!.size) }}），建议切换到「普通上传」以获得更好体验
        </div>
        <div class="grid grid-cols-2 gap-3">
          <n-form-item label="显示名称"><n-input v-model:value="form.title" placeholder="可选" /></n-form-item>
          <n-form-item label="文件类型"><n-select v-model:value="form.fileType" :options="fileTypeOptions" /></n-form-item>
        </div>
        <n-form-item label="分类"><n-select v-model:value="form.categoryId" placeholder="选择分类" clearable :options="categoryOptions" /></n-form-item>
        <n-form-item label="描述"><n-input v-model:value="form.description" type="textarea" placeholder="可选" :rows="2" /></n-form-item>
        <n-form-item><n-switch v-model:value="form.isPublic" /><span class="ml-2">公开访问</span></n-form-item>
        <div v-if="error" class="p-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded text-sm">{{ error }}</div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Upload, Folder } from '@/icons'
import { NButton, NFormItem, NInput, NSelect, NSwitch, NProgress, useMessage } from 'naive-ui'
import { LargeFileUploader, pauseUpload, resumeUpload, cancelUpload, deleteTask } from '@/api/files'

interface PendingUpload {
  uploadId: string; fileName: string; fileSize: number
  uploadedChunks: number; totalChunks: number
}

const props = defineProps<{
  show: boolean
  form: { title: string; fileType?: string; categoryId?: number; description: string; isPublic: boolean }
  fileTypeOptions: { label: string; value: any }[]
  categoryOptions: { label: string; value: any }[]
  clearFormStorage: () => void
}>()

const emit = defineEmits<{
  'update:uploading': [value: boolean]
  'update:uploadPhase': [value: 'init' | 'upload' | 'merge']
  'update:paused': [value: boolean]
  success: []
  'update:show': [value: boolean]
}>()

const CHUNK = 5 * 1024 * 1024
const LARGE_MIN_SIZE = 50 * 1024 * 1024
const PENDING_KEY = 'ptech_upload_pending'

const message = useMessage()

const largeFile = ref<File | null>(null)
const largeInput = ref<HTMLInputElement | null>(null)
const progress = ref(0)
const uploadedC = ref(0)
const uploadStatus = ref<'default' | 'success' | 'error'>('default')
const error = ref('')
const speed = ref<number | null>(null)
const paused = ref(false)
const isHash = ref(false)
const uploading = ref(false)
const uploadPhase = ref<'init' | 'upload' | 'merge'>('init')
const pendingUpload = ref<PendingUpload | null>(null)
let uploader: LargeFileUploader | null = null
let lastBytes = 0
let lastTime = 0

// 模块级：同窗口 + 跨标签页暂停/取消监听
const runningUploaders = new Map<string, LargeFileUploader>()
if (typeof window !== 'undefined' && !(window as any).__ptech_upload_listener2) {
  ;(window as any).__ptech_upload_listener2 = true
  const pauseUploader = (uid: string) => {
    if (uid && runningUploaders.has(uid)) {
      runningUploaders.get(uid)!.pause()
      runningUploaders.delete(uid)
    }
  }
  // 同窗口事件
  window.addEventListener('ptech:pause-upload', ((e: CustomEvent) => pauseUploader(e.detail?.uploadId)) as EventListener)
  // 跨标签页通信
  try {
    const bc = new BroadcastChannel('ptech-upload-ctrl')
    bc.onmessage = (e) => { if (e.data?.type === 'pause') pauseUploader(e.data.uploadId) }
  } catch { /* 浏览器不支持 BroadcastChannel，仅支持同窗口 */ }
  // 网络恢复后自动续传暂停的上传任务
  window.addEventListener('online', () => {
    for (const [uid, u] of runningUploaders) {
      if (u.isUploadPaused()) {
        u.resumeUpload()
        pauseUploader(uid) // 从 Map 移除（内部 resume 会重新注册）
      }
    }
  })
}

const loadPending = (): PendingUpload | null => {
  try {
    const saved = localStorage.getItem(PENDING_KEY)
    return saved ? JSON.parse(saved) : null
  } catch { return null }
}

const savePending = (uploadId: string, uploaded: number, total: number) => {
  if (!largeFile.value) return
  try {
    localStorage.setItem(PENDING_KEY, JSON.stringify({
      uploadId, fileName: largeFile.value.name, fileSize: largeFile.value.size,
      uploadedChunks: uploaded, totalChunks: total
    }))
    pendingUpload.value = loadPending()
  } catch {}
}

const clearPending = () => {
  try { localStorage.removeItem(PENDING_KEY) } catch {}
  pendingUpload.value = null
}

const discardPending = async () => {
  const uid = pendingUpload.value?.uploadId
  if (uid) try { await deleteTask(uid) } catch {}
  clearPending()
}

const totalChunks = computed(() => largeFile.value ? Math.ceil(largeFile.value.size / CHUNK) : 0)
const largeFileTooSmall = computed(() => largeFile.value ? largeFile.value.size < LARGE_MIN_SIZE : false)

function formatSize(b: number) {
  if (b === 0) return '0 B'
  const k = 1024; const s = ['B','KB','MB','GB','TB']
  const i = Math.floor(Math.log(b) / Math.log(k))
  return parseFloat((b / Math.pow(k, i)).toFixed(i > 0 ? 2 : 0)) + ' ' + s[i]
}
const formatSpeed = (bps: number) => formatSize(bps) + '/s'

function detectFileType(fileName: string): string | undefined {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  const map: Record<string, string> = {
    doc:'document',docx:'document',pdf:'document',txt:'document',md:'document',
    xls:'document',xlsx:'document',ppt:'document',pptx:'document',
    csv:'document',json:'document',xml:'document',yaml:'document',yml:'document',
    html:'document',htm:'document',css:'document',js:'document',ts:'document',
    jsx:'document',tsx:'document',vue:'document',
    jpg:'image',jpeg:'image',png:'image',gif:'image',bmp:'image',
    svg:'image',webp:'image',ico:'image',tiff:'image',tif:'image',
    mp4:'video',avi:'video',mov:'video',mkv:'video',wmv:'video',
    flv:'video',webm:'video',m4v:'video',mpg:'video',mpeg:'video',
    mp3:'audio',wav:'audio',flac:'audio',aac:'audio',ogg:'audio',
    wma:'audio',m4a:'audio',ape:'audio',
    zip:'archive',rar:'archive','7z':'archive',tar:'archive',
    gz:'archive',bz2:'archive',xz:'archive',iso:'archive',
  }
  return map[ext] || 'other'
}

function stripExt(fileName: string): string {
  const i = fileName.lastIndexOf('.')
  return i > 0 ? fileName.substring(0, i) : fileName
}

function triggerLargeInput() { largeInput.value?.click() }

function onLargeFileSelect(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (f) { largeFile.value = f; resetProgress(); props.form.fileType = detectFileType(f.name); props.form.title = stripExt(f.name) }
}

function clearLarge() { largeFile.value = null; uploader = null; resetProgress(); if (largeInput.value) largeInput.value.value = '' }

function resetProgress() {
  progress.value = 0; uploadedC.value = 0; error.value = ''; uploadStatus.value = 'default'
  isHash.value = false; paused.value = false; uploadPhase.value = 'init'
  emit('update:paused', false); emit('update:uploadPhase', 'init')
}

// 弹窗打开时重置换肤
watch(() => props.show, (val) => {
  if (val) {
    uploading.value = false
    speed.value = null
    lastBytes = 0; lastTime = 0
    emit('update:uploading', false)
    const autoopen = localStorage.getItem('ptech_upload_autoopen')
    if (autoopen === '1') {
      localStorage.removeItem('ptech_upload_autoopen')
      pendingUpload.value = loadPending()
    } else {
      pendingUpload.value = null
    }
    if (pendingUpload.value && largeFile.value) {
      progress.value = pendingUpload.value.totalChunks > 0
        ? Math.round(pendingUpload.value.uploadedChunks / pendingUpload.value.totalChunks * 100) : 0
      uploadedC.value = pendingUpload.value.uploadedChunks
      error.value = ''
      uploadStatus.value = 'default'
      isHash.value = false
    } else {
      resetProgress()
    }
  }
}, { immediate: true })

async function handleLargeUpload() {
  if (!largeFile.value) return
  uploading.value = true; error.value = ''; uploadPhase.value = 'init'
  emit('update:uploading', true); emit('update:uploadPhase', 'init')
  lastBytes = 0; lastTime = Date.now()
  const currentUploader = new LargeFileUploader(largeFile.value, CHUNK,
    (p, c, stage) => {
      const loaded = c * CHUNK; const now = Date.now(); const diff = (now - lastTime) / 1000
      if (diff >= 1) { speed.value = (loaded - lastBytes) / diff; lastBytes = loaded; lastTime = now }
      if (stage === 'hash') { isHash.value = true; progress.value = Math.round(p) }
      else {
        if (uploadPhase.value === 'init') { uploadPhase.value = 'upload'; emit('update:uploadPhase', 'upload') }
        isHash.value = false; progress.value = Math.round(p); uploadedC.value = c
      }
    },
    () => { uploadStatus.value = 'success'; isHash.value = false; message.success('上传成功') },
    (err) => { error.value = err; uploadStatus.value = 'error'; isHash.value = false; message.error(err) }
  )
  uploader = currentUploader
  try {
    uploadPhase.value = 'init'; emit('update:uploadPhase', 'init')
    isHash.value = true
    await currentUploader.computeFileMd5()
    await currentUploader.init()
    // 恢复暂停任务时通知后端将状态置为 UPLOADING
    if (pendingUpload.value) {
      try { await resumeUpload(currentUploader.getUploadId()) } catch {}
    }
    runningUploaders.set(currentUploader.getUploadId(), currentUploader)
    savePending(currentUploader.getUploadId(), uploadedC.value, totalChunks.value)
    uploadPhase.value = 'upload'; emit('update:uploadPhase', 'upload')
    await currentUploader.uploadAll()
    uploadPhase.value = 'merge'; emit('update:uploadPhase', 'merge')
    await currentUploader.merge(props.form.categoryId, props.form.description, props.form.title || undefined, props.form.isPublic)
    props.clearFormStorage(); clearPending()
    emit('update:show', false); emit('success')
  } catch (e: any) {
    error.value = e.message || '上传失败'; uploadStatus.value = 'error'
    if (uploadPhase.value !== 'merge') {
      try { await currentUploader.cancel() } catch {}
    }
  } finally {
    runningUploaders.delete(currentUploader.getUploadId() || '')
    uploading.value = false; emit('update:uploading', false)
  }
}

async function handleCancelInit() {
  if (!uploader) return
  const uid = uploader.getUploadId()
  uploader.pause()
  if (uid) { try { await cancelUpload(uid) } catch {} }
  uploading.value = false; emit('update:uploading', false)
  error.value = '已取消上传'
  largeFile.value = null; uploader = null
  resetProgress(); clearPending()
}

async function handlePause() {
  uploader?.pause(); paused.value = true; emit('update:paused', true)
  const uid = uploader?.getUploadId()
  if (uid) try { await pauseUpload(uid) } catch {}
  savePending(uid || '', uploadedC.value, totalChunks.value)
  message.info('已暂停，可关闭弹窗后重新打开恢复')
}

async function handleResume() {
  if (!uploader) return
  const currentUploader = uploader
  uploading.value = true; emit('update:uploading', true)
  paused.value = false; emit('update:paused', false)
  currentUploader.resumeUpload()
  // 通知后端将任务状态置为 UPLOADING
  try { await resumeUpload(currentUploader.getUploadId()) } catch {}
  runningUploaders.set(currentUploader.getUploadId(), currentUploader)
  uploadPhase.value = 'upload'; emit('update:uploadPhase', 'upload')
  try {
    await currentUploader.uploadAll()
    uploadPhase.value = 'merge'; emit('update:uploadPhase', 'merge')
    await currentUploader.merge(props.form.categoryId, props.form.description, props.form.title || undefined, props.form.isPublic)
    props.clearFormStorage(); clearPending()
    emit('update:show', false); emit('success')
  } catch (e: any) {
    if (e.message !== '上传已取消') {
      error.value = e.message; uploadStatus.value = 'error'
      const isMergeError = e.message?.includes('合并') || e.message?.includes('timeout') || e.code === 'ECONNABORTED'
      if (!isMergeError) { try { await currentUploader.cancel() } catch {} }
    }
  } finally {
    runningUploaders.delete(currentUploader.getUploadId() || '')
    uploading.value = false; emit('update:uploading', false)
  }
}

defineExpose({
  handleLargeUpload, handleResume, handlePause, handleCancelInit,
  clearLarge, largeFile, paused, uploading, uploadPhase, error,
  largeFileTooSmall, pendingUpload, discardPending, clearPending
})
</script>
