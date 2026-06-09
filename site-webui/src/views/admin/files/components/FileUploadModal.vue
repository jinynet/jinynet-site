<template>
  <n-modal :show="show" preset="card" title="上传文件" :style="{ width: '700px' }" @update:show="onClose">
    <!-- 上传模式切换 -->
    <n-button-group class="mb-5">
      <n-button :type="uploadMode === 'simple' ? 'primary' : 'default'" @click="switchMode('simple')">普通上传</n-button>
      <n-button :type="uploadMode === 'large' ? 'primary' : 'default'" @click="switchMode('large')">大文件上传</n-button>
    </n-button-group>

    <!-- 普通上传 -->
    <SimpleUploadSection
      v-if="uploadMode === 'simple'"
      ref="simpleRef"
      :form="form"
      :file-type-options="fileTypeOptions"
      :category-options="categoryOptions"
      :uploading="uploading"
      :clear-form-storage="clearFormStorage"
      @update:uploading="uploading = $event"
      @success="onSuccess"
      @update:show="val => emit('update:show', val)"
    />

    <!-- 大文件上传 -->
    <LargeFileUploadSection
      v-else
      ref="largeRef"
      :show="show"
      :form="form"
      :file-type-options="fileTypeOptions"
      :category-options="categoryOptions"
      :clear-form-storage="clearFormStorage"
      @update:uploading="uploading = $event"
      @update:upload-phase="uploadPhase = $event"
      @update:paused="paused = $event"
      @success="onSuccess"
      @update:show="val => emit('update:show', val)"
    />

    <template #footer>
      <n-space justify="end">
        <n-button @click="onCancel" :disabled="uploadPhase === 'merge'">取消</n-button>
        <template v-if="uploadMode === 'simple'">
          <n-button type="primary" @click="simpleRef?.handleSimpleUpload()" :loading="uploading" :disabled="!hasSimpleFile">上传</n-button>
        </template>
        <template v-else>
          <n-button v-if="paused" type="primary" @click="largeRef?.handleResume()">继续上传</n-button>
          <n-button v-else-if="uploadPhase === 'init' && uploading" type="error" @click="largeRef?.handleCancelInit()">取消上传</n-button>
          <n-button v-else-if="uploadPhase === 'upload' && uploading" type="warning" @click="largeRef?.handlePause()">暂停</n-button>
          <n-button v-else-if="uploadPhase === 'merge'" type="primary" loading>处理中...</n-button>
          <n-button v-else type="primary" @click="largeRef?.handleLargeUpload()" :disabled="!largeRef?.largeFile || largeRef?.largeFileTooSmall">开始上传</n-button>
        </template>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { NButton, NButtonGroup, NModal, NSpace, useMessage } from 'naive-ui'
import type { FileCategory } from '@/api/files'
import SimpleUploadSection from './SimpleUploadSection.vue'
import LargeFileUploadSection from './LargeFileUploadSection.vue'

const props = defineProps<{ show: boolean; categories: FileCategory[] }>()
const emit = defineEmits<{ 'update:show': [value: boolean]; 'success': [] }>()
const message = useMessage()
const FORM_KEY = 'ptech_upload_form'

const loadForm = (): { title: string; fileType?: string; categoryId?: number; description: string; isPublic: boolean } => {
  try {
    const saved = localStorage.getItem(FORM_KEY)
    return saved ? JSON.parse(saved) : { title: '', fileType: undefined, categoryId: undefined, description: '', isPublic: false }
  } catch { return { title: '', fileType: undefined, categoryId: undefined, description: '', isPublic: false } }
}
const saveForm = () => { try { localStorage.setItem(FORM_KEY, JSON.stringify(form.value)) } catch {} }
const clearFormStorage = () => { try { localStorage.removeItem(FORM_KEY) } catch {} }

const form = ref(loadForm())
watch(form, saveForm, { deep: true })

const fileTypeOptions = [
  { label: '自动检测', value: undefined },
  { label: '文档', value: 'document' }, { label: '图片', value: 'image' },
  { label: '视频', value: 'video' }, { label: '音频', value: 'audio' },
  { label: '压缩包', value: 'archive' }, { label: '其他', value: 'other' }
]
const categoryOptions = computed(() => [
  { label: '未分类', value: undefined as any },
  ...props.categories.map(c => ({ label: c.name, value: c.id as unknown as number }))
])

type UploadMode = 'simple' | 'large'
const uploadMode = ref<UploadMode>('simple')
const uploading = ref(false)
const uploadPhase = ref<'init' | 'upload' | 'merge'>('init')
const paused = ref(false)

const simpleRef = ref<InstanceType<typeof SimpleUploadSection> | null>(null)
const largeRef = ref<InstanceType<typeof LargeFileUploadSection> | null>(null)

// 检测是否有待恢复任务，自动切换大文件模式
watch(() => props.show, (val) => {
  if (val) {
    const autoopen = localStorage.getItem('ptech_upload_autoopen')
    if (autoopen === '1') {
      uploadMode.value = 'large'
    }
  }
})

const hasSimpleFile = computed(() => {
  const list = simpleRef.value?.simpleFileList
  return list && list.length > 0
})

function switchMode(mode: UploadMode) {
  if (uploading.value) return
  uploadMode.value = mode
}

function onSuccess() {
  emit('success')
}

function onClose(val: boolean) {
  if (!val) {
    if (uploadPhase.value === 'init' && uploading.value) {
      largeRef.value?.handleCancelInit()
      return
    }
    if (uploadPhase.value === 'upload' && uploading.value) {
      message.warning('上传将在后台继续，可前往任务管理器暂停')
    }
    emit('update:show', false)
  }
}

function onCancel() {
  emit('update:show', false)
}
</script>
