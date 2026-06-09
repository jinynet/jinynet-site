<template>
  <div class="tab-content">
    <n-form label-placement="top" class="space-y-3">
      <n-form-item label="选择文件" required>
        <n-upload v-model:file-list="simpleFileList" :max="1" :auto-upload="false" @change="onSimpleFileChange">
          <n-upload-dragger class="w-full">
            <div class="flex flex-col items-center justify-center py-6">
              <Upload class="w-10 h-10 text-gray-400 mb-2" />
              <p>点击或拖拽文件到此处</p>
              <p class="text-sm text-gray-400">适合小于 50MB 的文件</p>
            </div>
          </n-upload-dragger>
        </n-upload>
      </n-form-item>
      <div v-if="simpleFileTooLarge" class="p-3 bg-yellow-50 dark:bg-yellow-900/20 text-yellow-700 dark:text-yellow-400 rounded text-sm -mt-1">
        文件较大（{{ formatSize(simpleFileSize!) }}），建议切换到「大文件上传」以获得更好体验
      </div>
      <div class="grid grid-cols-2 gap-3">
        <n-form-item label="显示名称"><n-input v-model:value="form.title" placeholder="可选" /></n-form-item>
        <n-form-item label="文件类型"><n-select v-model:value="form.fileType" :options="fileTypeOptions" /></n-form-item>
      </div>
      <n-form-item label="分类"><n-select v-model:value="form.categoryId" placeholder="选择分类" clearable :options="categoryOptions" /></n-form-item>
      <n-form-item label="描述"><n-input v-model:value="form.description" type="textarea" placeholder="可选" :rows="2" /></n-form-item>
      <n-form-item><n-switch v-model:value="form.isPublic" /><span class="ml-2">公开访问</span></n-form-item>
    </n-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Upload } from '@/icons'
import { NUpload, NUploadDragger, NForm, NFormItem, NInput, NSelect, NSwitch, useMessage, type UploadFileInfo } from 'naive-ui'
import { uploadFile } from '@/api/files'

const props = defineProps<{
  form: { title: string; fileType?: string; categoryId?: number; description: string; isPublic: boolean }
  fileTypeOptions: { label: string; value: any }[]
  categoryOptions: { label: string; value: any }[]
  uploading: boolean
  clearFormStorage: () => void
}>()

const emit = defineEmits<{
  'update:uploading': [value: boolean]
  success: []
  'update:show': [value: boolean]
}>()

const LARGE_MIN_SIZE = 50 * 1024 * 1024

const message = useMessage()
const simpleFileList = ref<UploadFileInfo[]>([])

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

function onSimpleFileChange(data: { file: UploadFileInfo; fileList: UploadFileInfo[] }) {
  const f = data.fileList[0]?.file as File | undefined
  if (f) { props.form.fileType = detectFileType(f.name); props.form.title = stripExt(f.name) }
}

const simpleFileSize = computed(() => (simpleFileList.value[0]?.file as File | undefined)?.size ?? 0)
const simpleFileTooLarge = computed(() => simpleFileSize.value >= LARGE_MIN_SIZE)

function formatSize(b: number) {
  if (b === 0) return '0 B'
  const k = 1024; const s = ['B','KB','MB','GB','TB']
  const i = Math.floor(Math.log(b) / Math.log(k))
  return parseFloat((b / Math.pow(k, i)).toFixed(i > 0 ? 2 : 0)) + ' ' + s[i]
}

async function handleSimpleUpload() {
  const f = simpleFileList.value[0]?.file as File | undefined
  if (!f) return message.error('请选择文件')
  emit('update:uploading', true)
  try {
    await uploadFile(f, props.form.categoryId, props.form.description, props.form.isPublic)
    props.clearFormStorage()
    emit('update:show', false); emit('success'); message.success('上传成功')
  } catch {
    message.error('上传失败')
  } finally {
    emit('update:uploading', false)
  }
}

defineExpose({ handleSimpleUpload, simpleFileList })
</script>
