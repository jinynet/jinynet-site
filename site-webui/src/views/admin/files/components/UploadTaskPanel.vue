<template>
  <div class="task-list">
    <div v-if="tasks.length === 0" class="empty-state">
      <Cloud class="w-10 h-10 mb-3 opacity-60" />
      <p class="text-sm">暂无上传任务</p>
    </div>

    <template v-for="task in tasks" :key="task.uploadId">
      <div class="task-item" :class="{ 'task-terminal': isTerminal(task.status) }">
        <div class="flex-1 min-w-0">
          <p class="text-xs font-medium truncate">{{ task.fileName }}</p>
          <div class="flex items-center gap-1 mt-1">
            <n-tag :type="statusType(task.status)" size="tiny" :bordered="false">{{ statusText(task.status) }}</n-tag>
            <span v-if="!isTerminal(task.status)" class="text-xs text-gray-400">{{ task.uploadedChunks }}/{{ task.totalChunks }}</span>
          </div>
          <n-progress v-if="task.status === 'UPLOADING'" type="line"
            :percentage="task.totalChunks > 0 ? Math.round(task.uploadedChunks / task.totalChunks * 100) : 0"
            :height="4" :show-indicator="false" processing class="mt-1" />
        </div>
        <div class="flex items-center gap-1 ml-2">
          <n-button v-if="task.status === 'UPLOADING'" text size="tiny" type="warning" @click="doPause(task.uploadId)">暂停</n-button>
          <n-button v-if="task.status === 'PAUSED'" text size="tiny" type="success" @click="doResume(task)">继续</n-button>
          <n-button v-if="!isTerminal(task.status)" text size="tiny" type="error" @click="doCancel(task.uploadId)">取消</n-button>
          <n-button v-else text size="tiny" @click="doDismiss(task.uploadId)" title="移除"><Close class="w-3 h-3" /></n-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Cloud, Close } from '@/icons'
import { NTag, NProgress, NButton, useMessage } from 'naive-ui'
import { getUploadTasks, pauseUpload, resumeUpload, cancelUpload, deleteTask } from '@/api/files'

const emit = defineEmits<{ 'count-change': [count: number] }>()
const message = useMessage()
const router = useRouter()
const tasks = ref<any[]>([])
let timer: ReturnType<typeof setInterval> | null = null

const PENDING_KEY = 'ptech_upload_pending'

const isTerminal = (s: string) => s === 'CANCELLED' || s === 'FAILED'

const activeTasks = computed(() => tasks.value.filter(t => !isTerminal(t.status)))

const statusType = (s: string) => ({ UPLOADING: 'warning', PAUSED: 'default', MERGING: 'info', COMPLETED: 'success', CANCELLED: 'default', FAILED: 'error' } as any)[s] || 'default'
const statusText = (s: string) => ({ UPLOADING: '上传中', PAUSED: '已暂停', MERGING: '合并中', COMPLETED: '已完成', CANCELLED: '已取消', FAILED: '失败' } as any)[s] || s

async function fetchTasks() {
  try {
    const resp = await getUploadTasks()
    // 兼容多种返回格式：Result 包裹 / 直接数组
    let raw: any[] = []
    if (Array.isArray(resp)) {
      raw = resp
    } else if (resp && Array.isArray(resp.data)) {
      raw = resp.data
    } else if (resp && resp.data && Array.isArray(resp.data.rows)) {
      // Jimmer Page 包裹
      raw = resp.data.rows
    } else {
      console.warn('上传任务数据格式异常:', resp)
    }
    const all = raw.filter((t: any) => t.status !== 'COMPLETED')
    // 按更新时间倒序，活跃任务在前
    all.sort((a: any, b: any) => {
      const aTerm = isTerminal(a.status) ? 1 : 0
      const bTerm = isTerminal(b.status) ? 1 : 0
      if (aTerm !== bTerm) return aTerm - bTerm
      return new Date(b.updatedAt || 0).getTime() - new Date(a.updatedAt || 0).getTime()
    })
    tasks.value = all
    emit('count-change', activeTasks.value.length)
  } catch (e) {
    console.error('获取上传任务失败:', e)
  }
}
// 跨标签页广播辅助
let _bc: BroadcastChannel | null = null
try { _bc = new BroadcastChannel('ptech-upload-ctrl') } catch {}
const broadcastPause = (uploadId: string) => {
  window.dispatchEvent(new CustomEvent('ptech:pause-upload', { detail: { uploadId } }))
  if (_bc) _bc.postMessage({ type: 'pause', uploadId })
}

async function doPause(uploadId: string) {
  try {
    await pauseUpload(uploadId)
    broadcastPause(uploadId)
    message.success('已暂停'); fetchTasks()
  } catch { message.error('暂停失败') }
}
async function doResume(task: any) {
  try {
    await resumeUpload(task.uploadId)
    // 保存为待恢复任务
    localStorage.setItem(PENDING_KEY, JSON.stringify({
      uploadId: task.uploadId,
      fileName: task.fileName,
      fileSize: task.fileSize,
      uploadedChunks: task.uploadedChunks,
      totalChunks: task.totalChunks
    }))
    // 当前已在文件管理页则通过标记触发自动打开；否则跳转
    if (router.currentRoute.value.path === '/admin/files') {
      localStorage.setItem('ptech_upload_autoopen', '1')
      message.success('已准备恢复，请选择相同文件继续上传')
      // 如果当前显示的是文件列表页，需要触发弹窗
      window.dispatchEvent(new CustomEvent('ptech:open-upload'))
    } else {
      router.push({ path: '/admin/files', query: { resume: '1' } })
    }
  } catch { message.error('恢复失败') }
}
async function doCancel(uploadId: string) {
  try {
    await cancelUpload(uploadId)
    broadcastPause(uploadId)
    message.success('已取消'); fetchTasks()
  } catch { message.error('取消失败') }
}
async function doDismiss(uploadId: string) {
  try {
    await deleteTask(uploadId)
    message.success('已删除')
  } catch {
    // 即使 API 失败也移除本地显示
  }
  tasks.value = tasks.value.filter(t => t.uploadId !== uploadId)
  emit('count-change', activeTasks.value.length)
}

onMounted(() => { fetchTasks(); timer = setInterval(fetchTasks, 10000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 100%;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  color: #9ca3af;
}
:global(.dark) .empty-state {
  color: #6b7280;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}
.task-item:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}
:global(.dark) .task-item {
  background: #1f2937;
  border-color: #374151;
}
:global(.dark) .task-item:hover {
  background: #374151;
  border-color: #4b5563;
}

.task-terminal {
  opacity: 0.6;
  background: #f9fafb;
}
.task-terminal:hover {
  opacity: 0.8;
  background: #f3f4f6;
}
:global(.dark) .task-terminal {
  background: #111827;
}
:global(.dark) .task-terminal:hover {
  background: #1f2937;
}
</style>
