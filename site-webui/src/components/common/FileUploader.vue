<template>
  <div class="file-uploader">
    <div
      class="upload-area"
      :class="{ 'is-dragging': isDragging }"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInputRef"
        type="file"
        :accept="accept"
        :multiple="false"
        class="file-input"
        @change="handleFileSelect"
      />
      <div class="upload-icon">
        <component :is="icons.CloudUploadOutlined" class="icon" />
      </div>
      <div class="upload-text">
        <div class="title">{{ isDragging ? '释放文件以上传' : '点击或拖拽文件到此处' }}</div>
        <div class="subtitle">{{ accept || '支持所有文件格式' }}</div>
      </div>
    </div>

    <div v-if="uploadingFiles.length > 0" class="upload-list">
      <div
        v-for="item in uploadingFiles"
        :key="item.id"
        class="upload-item"
      >
        <div class="file-info">
          <component :is="getFileIcon(item.file.type)" class="file-icon" />
          <div class="file-details">
            <div class="file-name">{{ item.file.name }}</div>
            <div class="file-size">{{ formatFileSize(item.file.size) }}</div>
          </div>
        </div>

        <div class="progress-container">
          <div v-if="item.stage === 'hash'" class="progress-info">
            <span>计算MD5: {{ item.hashProgress }}%</span>
          </div>
          <div v-else class="progress-info">
            <span>上传中: {{ item.uploadProgress }}%</span>
            <span class="chunk-info">{{ item.uploadedChunks }}/{{ item.totalChunks }}</span>
          </div>
          <n-progress
            :percentage="item.stage === 'hash' ? item.hashProgress : item.uploadProgress"
            :show-indicator="false"
            :status="item.status === 'error' ? 'error' : 'success'"
            class="progress-bar"
          />
        </div>

        <div class="actions">
          <n-button
            v-if="item.status === 'uploading'"
            size="small"
            type="error"
            text
            @click="cancelUpload(item.id)"
          >
            <component :is="icons.CancelOutlined" class="action-icon" />
            取消
          </n-button>
          <n-button
            v-else-if="item.status === 'error'"
            size="small"
            type="primary"
            text
            @click="retryUpload(item.id)"
          >
            <component :is="icons.RefreshOutlined" class="action-icon" />
            重试
          </n-button>
          <n-tag v-else-if="item.status === 'success'" type="success">
            <component :is="icons.CheckCircleOutlined" class="success-icon" />
            完成
          </n-tag>
        </div>

        <div v-if="item.error" class="error-message">
          <component :is="icons.AddAlertOutlined" class="error-icon" />
          {{ item.error }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  CloudUploadOutlined,
  CancelOutlined,
  RefreshOutlined,
  CheckCircleOutlined,
  AddAlertOutlined,
  InsertDriveFileOutlined,
  ImageOutlined,
  VideoLibraryOutlined,
  AudioFileOutlined,
  DescriptionOutlined
} from '@vicons/material'
import { NProgress, NButton, NTag } from 'naive-ui'
import { LargeFileUploader, type MergeChunksResult, type UploadMode } from '@/api/files'
import { formatFileSize } from '@/utils/fileHash'

interface UploadingFile {
  id: string
  file: File
  uploader: any
  stage: 'hash' | 'upload' | 'merge' | 'done'
  hashProgress: number
  uploadProgress: number
  uploadedChunks: number
  totalChunks: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  error: string | null
}

const icons = {
  CloudUploadOutlined,
  CancelOutlined,
  RefreshOutlined,
  CheckCircleOutlined,
  AddAlertOutlined,
  InsertDriveFileOutlined,
  ImageOutlined,
  VideoLibraryOutlined,
  AudioFileOutlined,
  DescriptionOutlined
}

const props = withDefaults(defineProps<{
  accept?: string
  categoryId?: number
  description?: string
  chunkSize?: number
  uploadMode?: UploadMode
  maxConcurrency?: number
}>(), {
  accept: '',
  chunkSize: 5 * 1024 * 1024,
  uploadMode: 'serial',
  maxConcurrency: 3
})

const emit = defineEmits<{
  (e: 'success', result: MergeChunksResult): void
  (e: 'error', error: string): void
  (e: 'progress', progress: number): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const uploadingFiles = ref<UploadingFile[]>([])

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files && files.length > 0) {
    handleFiles(Array.from(files))
    target.value = ''
  }
}

const handleDragOver = () => {
  isDragging.value = true
}

const handleDragLeave = () => {
  isDragging.value = false
}

const handleDrop = (event: DragEvent) => {
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    handleFiles(Array.from(files))
  }
}

const handleFiles = async (files: File[]) => {
  for (const file of files) {
    const item: UploadingFile = {
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
      file,
      uploader: null,
      stage: 'hash',
      hashProgress: 0,
      uploadProgress: 0,
      uploadedChunks: 0,
      totalChunks: Math.ceil(file.size / props.chunkSize),
      status: 'uploading',
      error: null
    }

    uploadingFiles.value.push(item)
    await processFile(item)
  }
}

const processFile = async (item: UploadingFile) => {
  try {
    const uploader = new LargeFileUploader(
      item.file,
      props.chunkSize,
      (progress, uploadedChunks, stage) => {
        if (stage === 'upload') {
          item.uploadProgress = progress
          item.uploadedChunks = uploadedChunks
          emit('progress', progress)
        }
      },
      (result) => {
        item.status = 'success'
        item.stage = 'done'
        emit('success', result)
      },
      (error) => {
        item.status = 'error'
        item.error = error
        emit('error', error)
      },
      props.uploadMode,
      props.maxConcurrency
    )

    item.uploader = uploader

    item.stage = 'hash'
    await uploader.computeFileMd5((progress) => {
      item.hashProgress = progress
    })

    item.stage = 'upload'
    await uploader.init()

    await uploader.uploadAll()

    item.stage = 'merge'
    await uploader.merge(props.categoryId, props.description)

  } catch (error: any) {
    item.status = 'error'
    item.error = error.message || '上传失败'
    emit('error', item.error || '上传失败')
  }
}

const cancelUpload = async (id: string) => {
  const item = uploadingFiles.value.find(f => f.id === id)
  if (item && item.uploader) {
    await item.uploader.cancel()
    item.status = 'error'
    item.error = '上传已取消'
  }
}

const retryUpload = async (id: string) => {
  const item = uploadingFiles.value.find(f => f.id === id)
  if (item) {
    item.status = 'uploading'
    item.error = null
    item.hashProgress = 0
    item.uploadProgress = 0
    item.uploadedChunks = 0
    item.stage = 'hash'
    
    // 重置取消状态（用于并行上传模式）
    if (item.uploader && typeof item.uploader.resetCancel === 'function') {
      item.uploader.resetCancel()
    }
    
    await processFile(item)
  }
}

const getFileIcon = (type?: string) => {
  if (!type) return icons.InsertDriveFileOutlined
  if (type.startsWith('image/')) return icons.ImageOutlined
  if (type.startsWith('video/')) return icons.VideoLibraryOutlined
  if (type.startsWith('audio/')) return icons.AudioFileOutlined
  if (type.includes('text/') || type.includes('pdf')) return icons.DescriptionOutlined
  return icons.InsertDriveFileOutlined
}
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover {
  border-color: #1890ff;
  background: #f0f5ff;
}

.upload-area.is-dragging {
  border-color: #1890ff;
  background: #e6f7ff;
  transform: scale(1.02);
}

.file-input {
  display: none;
}

.upload-icon {
  margin-bottom: 16px;
}

.upload-icon .icon {
  font-size: 48px;
  color: #999;
}

.upload-area:hover .upload-icon .icon,
.upload-area.is-dragging .upload-icon .icon {
  color: #1890ff;
}

.upload-text {
  .title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #999;
  }
}

.upload-list {
  margin-top: 24px;
}

.upload-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.file-icon {
  font-size: 24px;
  color: #1890ff;
}

.file-details {
  .file-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .file-size {
    font-size: 12px;
    color: #999;
  }
}

.progress-container {
  margin-bottom: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.chunk-info {
  color: #999;
}

.progress-bar {
  height: 6px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.action-icon {
  margin-right: 4px;
}

.success-icon {
  margin-right: 4px;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: #fff2f0;
  border-radius: 4px;
  font-size: 14px;
  color: #ff4d4f;
}

.error-icon {
  font-size: 16px;
}
</style>