<template>
  <div class="file-manager">
    <div class="toolbar">
      <n-space>
        <n-button type="primary" @click="showUploadModal = true">
          <template #icon>
            <component :is="icons.CloudUploadOutlined" />
          </template>
          上传文件
        </n-button>
        <n-button @click="fetchFiles">
          <template #icon>
            <component :is="icons.RefreshOutlined" />
          </template>
          刷新
        </n-button>
      </n-space>

      <div class="flex-1"></div>

      <n-space>
        <n-select
          v-model:value="filterCategory"
          :options="categoryOptions"
          placeholder="文件分类"
          clearable
          style="width: 150px"
          @update:value="fetchFiles"
        />
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索文件..."
          clearable
          style="width: 200px"
          @keyup.enter="fetchFiles"
        >
          <template #prefix>
            <component :is="icons.SearchOutlined" class="text-gray-400" />
          </template>
        </n-input>
      </n-space>
    </div>

    <div class="file-grid">
      <n-spin :show="loading">
        <div v-if="files.length === 0" class="empty-state">
          <component :is="icons.FolderOpenOutlined" class="empty-icon" />
          <p>暂无文件</p>
          <n-button type="primary" @click="showUploadModal = true">上传文件</n-button>
        </div>

        <div v-else class="grid-container">
          <div
            v-for="file in files"
            :key="file.id"
            class="file-card"
            @click="handlePreview(file)"
          >
            <div class="file-preview">
              <img
                v-if="isImage(file)"
                :src="file.url"
                :alt="file.originalFilename"
                class="preview-image"
              />
              <div v-else-if="isVideo(file)" class="preview-video">
                <component :is="icons.VideoLibraryOutlined" class="preview-icon video-icon" />
              </div>
              <div v-else-if="isAudio(file)" class="preview-audio">
                <component :is="icons.AudioFileOutlined" class="preview-icon audio-icon" />
              </div>
              <div v-else-if="isDocument(file)" class="preview-document">
                <component :is="icons.DescriptionOutlined" class="preview-icon document-icon" />
              </div>
              <div v-else class="preview-other">
                <component :is="icons.InsertDriveFileOutlined" class="preview-icon other-icon" />
              </div>
            </div>

            <div class="file-info">
              <div class="file-name" :title="file.originalFilename">
                {{ file.originalFilename }}
              </div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
                <span class="file-type">{{ getFileTypeLabel(file.fileType) }}</span>
              </div>
            </div>

            <div class="file-actions" @click.stop>
              <n-button text size="small" @click="handlePreview(file)">
                <template #icon>
                  <component :is="icons.VisibilityOutlined" />
                </template>
              </n-button>
              <n-button text size="small" @click="handleDownload(file)">
                <template #icon>
                  <component :is="icons.DownloadOutlined" />
                </template>
              </n-button>
              <n-button text size="small" @click="handleDelete(file)">
                <template #icon>
                  <component :is="icons.DeleteOutlined" />
                </template>
              </n-button>
            </div>
          </div>
        </div>
      </n-spin>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <n-pagination
        v-model:page="currentPage"
        :page-count="totalPages"
        :page-size="pageSize"
        show-size-picker
        :page-sizes="[12, 24, 48]"
        @update:page="fetchFiles"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <n-modal
      v-model:show="showUploadModal"
      preset="card"
      title="上传文件"
      style="width: 600px"
      :mask-closable="false"
    >
      <FileUploader
        :category-id="filterCategory"
        :upload-mode="uploadMode"
        :max-concurrency="maxConcurrency"
        @success="handleUploadSuccess"
        @error="handleUploadError"
      />

      <div class="upload-settings">
        <n-divider>上传设置</n-divider>
        <n-space align="center">
          <span>上传模式：</span>
          <n-radio-group v-model:value="uploadMode" size="small">
            <n-radio-button value="serial">串行上传</n-radio-button>
            <n-radio-button value="parallel">并行上传</n-radio-button>
          </n-radio-group>
          <n-input-number
            v-if="uploadMode === 'parallel'"
            v-model:value="maxConcurrency"
            :min="1"
            :max="10"
            size="small"
            style="width: 120px"
          >
            <template #prefix>并发数：</template>
          </n-input-number>
        </n-space>
      </div>
    </n-modal>

    <n-modal
      v-model:show="showPreviewModal"
      preset="card"
      :title="previewFile?.originalFilename"
      style="width: 90%; max-width: 1200px"
      :mask-closable="true"
    >
      <div class="preview-container">
        <img
          v-if="previewFile && isImage(previewFile)"
          :src="previewFile.url"
          :alt="previewFile.originalFilename"
          class="preview-full-image"
        />

        <video
          v-else-if="previewFile && isVideo(previewFile)"
          :src="previewFile.url"
          controls
          class="preview-full-video"
        />

        <audio
          v-else-if="previewFile && isAudio(previewFile)"
          :src="previewFile.url"
          controls
          class="preview-full-audio"
        />

        <iframe
          v-else-if="previewFile && isPdf(previewFile)"
          :src="previewFile.url"
          class="preview-full-document"
        />

        <div v-else class="preview-unsupported">
          <component :is="icons.ErrorOutlineOutlined" class="error-icon" />
          <p>暂不支持预览此类型文件</p>
          <n-button type="primary" @click="handleDownload(previewFile!)">
            <template #icon>
              <component :is="icons.DownloadOutlined" />
            </template>
            下载文件
          </n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NButton,
  NSpace,
  NSelect,
  NInput,
  NSpin,
  NModal,
  NPagination,
  NDivider,
  NRadioGroup,
  NRadioButton,
  NInputNumber,
  useMessage,
  useDialog
} from 'naive-ui'
import {
  CloudUploadOutlined,
  RefreshOutlined,
  SearchOutlined,
  FolderOpenOutlined,
  VideoLibraryOutlined,
  AudioFileOutlined,
  DescriptionOutlined,
  InsertDriveFileOutlined,
  VisibilityOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ErrorOutlineOutlined
} from '@vicons/material'
import FileUploader from './FileUploader.vue'
import { getFileList, deleteFile, type FileInfo, type UploadMode } from '@/api/files'

const icons = {
  CloudUploadOutlined,
  RefreshOutlined,
  SearchOutlined,
  FolderOpenOutlined,
  VideoLibraryOutlined,
  AudioFileOutlined,
  DescriptionOutlined,
  InsertDriveFileOutlined,
  VisibilityOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ErrorOutlineOutlined
}

const message = useMessage()
const dialog = useDialog()

const files = ref<FileInfo[]>([])
const loading = ref(false)
const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const previewFile = ref<FileInfo | null>(null)

const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(1)
const total = ref(0)

const filterCategory = ref<number | undefined>(undefined)
const searchKeyword = ref('')
const uploadMode = ref<UploadMode>('serial')
const maxConcurrency = ref(3)

const categoryOptions = ref<Array<{ label: string; value?: number }>>([
  { label: '全部', value: undefined },
  { label: '视频', value: 1 },
  { label: '图片', value: 2 },
  { label: '文档', value: 3 },
  { label: '音频', value: 4 }
])

const fetchFiles = async () => {
  loading.value = true
  try {
    const response = await getFileList(
      filterCategory.value || undefined,
      searchKeyword.value || undefined
    )
    if (response.data) {
      files.value = response.data
      total.value = files.value.length
      totalPages.value = Math.ceil(total.value / pageSize.value)
    }
  } catch (error) {
    message.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchFiles()
}

const isImage = (file: FileInfo) => {
  return file.fileType === 'image' || file.fileExt?.match(/jpg|jpeg|png|gif|webp|svg/i)
}

const isVideo = (file: FileInfo) => {
  return file.fileType === 'video' || file.fileExt?.match(/mp4|webm|ogg|avi|mov/i)
}

const isAudio = (file: FileInfo) => {
  return file.fileType === 'audio' || file.fileExt?.match(/mp3|wav|ogg|aac|flac/i)
}

const isDocument = (file: FileInfo) => {
  return file.fileType === 'document' || file.fileExt?.match(/pdf|doc|docx|xls|xlsx|ppt|pptx|txt/i)
}

const isPdf = (file: FileInfo) => {
  return file.fileExt?.toLowerCase() === 'pdf'
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileTypeLabel = (type: string): string => {
  const labels: Record<string, string> = {
    image: '图片',
    video: '视频',
    audio: '音频',
    document: '文档',
    other: '其他'
  }
  return labels[type] || '其他'
}

const handlePreview = (file: FileInfo) => {
  previewFile.value = file
  showPreviewModal.value = true
}

const handleDownload = (file: FileInfo) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.originalFilename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  message.success('开始下载')
}

const handleDelete = (file: FileInfo) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除文件 "${file.originalFilename}" 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteFile(file.id)
        message.success('删除成功')
        fetchFiles()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleUploadSuccess = () => {
  message.success('上传成功')
  showUploadModal.value = false
  fetchFiles()
}

const handleUploadError = (error: string) => {
  message.error(error)
}

onMounted(() => {
  fetchFiles()
})
</script>

<style scoped>
.file-manager {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.flex-1 {
  flex: 1;
}

.file-grid {
  min-height: 400px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #ccc;
}

.file-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.file-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.file-preview {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-icon {
  font-size: 48px;
}

.video-icon {
  color: #722ed1;
}

.audio-icon {
  color: #1890ff;
}

.document-icon {
  color: #52c41a;
}

.other-icon {
  color: #8c8c8c;
}

.file-info {
  padding: 12px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #8c8c8c;
}

.file-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  border-top: 1px solid #f0f0f0;
  opacity: 0;
  transition: opacity 0.3s;
}

.file-card:hover .file-actions {
  opacity: 1;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.upload-settings {
  margin-top: 16px;
}

.preview-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-full-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.preview-full-video {
  width: 100%;
  max-height: 70vh;
}

.preview-full-audio {
  width: 100%;
}

.preview-full-document {
  width: 100%;
  height: 70vh;
  border: none;
}

.preview-unsupported {
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
}

.error-icon {
  font-size: 48px;
  color: #ff4d4f;
  margin-bottom: 16px;
}
</style>
