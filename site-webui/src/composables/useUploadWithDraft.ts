import { ref, computed, onUnmounted } from 'vue'
import type { Ref } from 'vue'
import { LargeFileUploader } from '@/api/files'

/**
 * 上传状态
 */
export const UploadState = {
  IDLE: 'idle',
  CREATING_DRAFT: 'creating_draft',
  COMPUTING_MD5: 'computing_md5',
  UPLOADING: 'uploading',
  MERGING: 'merging',
  SUCCESS: 'success',
  ERROR: 'error'
} as const

export type UploadStateValue = (typeof UploadState)[keyof typeof UploadState]

/**
 * 上传步骤
 */
export interface UploadStep {
  id: string
  label: string
  state: 'pending' | 'active' | 'completed' | 'error'
}

/**
 * useUploadWithDraft 配置项
 */
export interface UseUploadWithDraftOptions<TForm> {
  createDraft: (form: TForm, coverFile?: File | null) => Promise<{ id: number }>
  updateDraft?: (id: number, form: TForm) => Promise<unknown>
  onSuccess?: (result: unknown, businessId: number) => void
  onError?: (error: Error) => void
  onProgressUpdate?: (msg: string) => void
  storageKey?: string
}

/**
 * useUploadWithDraft 返回值
 */
export interface UseUploadWithDraftReturn<TForm> {
  uploadState: Ref<UploadStateValue>
  uploadProgress: Ref<number>
  uploadedChunks: Ref<number>
  totalChunks: Ref<number>
  computingMd5: Ref<boolean>
  md5Progress: Ref<number>
  businessId: Ref<number | null>
  uploadId: Ref<string | null>
  steps: Ref<UploadStep[]>
  uploader: Ref<unknown>
  startUpload: (file: File, form: TForm, coverFile?: File | null) => Promise<void>
  resumeUpload: () => Promise<void>
  cancelUpload: () => void
  pauseUpload: () => void
  clearDraft: () => void
  setBusinessId: (id: number) => void
}

export function useUploadWithDraft<TForm>(
  options: UseUploadWithDraftOptions<TForm>
): UseUploadWithDraftReturn<TForm> {
  const {
    createDraft,
    onSuccess,
    onError,
    onProgressUpdate,
    storageKey = 'upload_draft'
  } = options

  const uploadState = ref<UploadStateValue>(UploadState.IDLE)
  const uploadProgress = ref(0)
  const uploadedChunks = ref(0)
  const totalChunks = ref(0)
  const computingMd5 = ref(false)
  const md5Progress = ref(0)
  const businessId = ref<number | null>(null)
  const uploadId = ref<string | null>(null)
  const uploader = ref<unknown>(null)
  const currentForm = ref<TForm | null>(null)
  const currentFile = ref<File | null>(null)
  const currentCoverFile = ref<File | null>(null)

  const steps = computed<UploadStep[]>(() => {
    const activeStates = [
      UploadState.CREATING_DRAFT,
      UploadState.COMPUTING_MD5,
      UploadState.UPLOADING,
      UploadState.MERGING
    ] as const

    const labels = [
      { id: 'creating_draft', label: '创建草稿' },
      { id: 'computing_md5', label: '计算文件校验' },
      { id: 'uploading', label: '上传文件' },
      { id: 'merging', label: '处理文件' }
    ]

    return labels.map(({ id, label }) => {
      const stepIndex = activeStates.indexOf(id as typeof activeStates[number])
      const activeIndex = activeStates.indexOf(uploadState.value as typeof activeStates[number])
      let state: 'pending' | 'active' | 'completed' | 'error' = 'pending'

      if (uploadState.value === UploadState.SUCCESS) {
        state = 'completed'
      } else if (uploadState.value === UploadState.ERROR) {
        if (activeIndex >= stepIndex) {
          state = 'completed'
        } else if (activeIndex === stepIndex) {
          state = 'error'
        }
      } else if (activeIndex === stepIndex) {
        state = 'active'
      } else if (activeIndex > stepIndex) {
        state = 'completed'
      }

      return { id, label, state }
    })
  })

  const saveDraftToStorage = () => {
    if (currentForm.value && businessId.value && uploadId.value && currentFile.value) {
      const draftData = {
        form: currentForm.value,
        businessId: businessId.value,
        uploadId: uploadId.value,
        fileName: currentFile.value.name,
        fileSize: currentFile.value.size,
        lastModified: currentFile.value.lastModified,
        hasCoverFile: !!currentCoverFile.value,
        timestamp: Date.now()
      }
      localStorage.setItem(storageKey, JSON.stringify(draftData))
    }
  }

  const loadDraftFromStorage = () => {
    const saved = localStorage.getItem(storageKey)
    if (saved) {
      return JSON.parse(saved)
    }
    return null
  }

  const clearDraft = () => {
    localStorage.removeItem(storageKey)
    businessId.value = null
    uploadId.value = null
    currentForm.value = null
    currentFile.value = null
    currentCoverFile.value = null
  }

  /** 设置已有的业务 ID（用于从列表恢复草稿上传） */
  const setBusinessId = (id: number) => {
    businessId.value = id
  }

  const updateState = (newState: UploadStateValue) => {
    uploadState.value = newState
    if (newState === UploadState.ERROR) {
      saveDraftToStorage()
    }
  }

  const startUpload = async (
    file: File,
    form: TForm,
    coverFile?: File | null
  ) => {
    currentForm.value = form
    currentFile.value = file
    currentCoverFile.value = coverFile || null

    try {
      // 如果已有 businessId（上次失败重试），跳过创建草稿步骤
      if (businessId.value) {
        updateState(UploadState.COMPUTING_MD5)
      } else {
        updateState(UploadState.CREATING_DRAFT)
        const draftResponse = await createDraft(form, coverFile)
        businessId.value = draftResponse.id
        updateState(UploadState.COMPUTING_MD5)
      }

      totalChunks.value = Math.ceil(file.size / (5 * 1024 * 1024))

      uploader.value = new LargeFileUploader(
        file,
        5 * 1024 * 1024,
        (progress, uploaded) => {
          uploadProgress.value = Math.round(progress)
          uploadedChunks.value = uploaded
        },
        (result) => {
          updateState(UploadState.SUCCESS)
          clearDraft()
          if (onSuccess && businessId.value) {
            onSuccess(result, businessId.value)
          }
        },
        (errorMsg) => {
          if (onProgressUpdate) onProgressUpdate(errorMsg)
        },
        'parallel',
        3
      )

      await (uploader.value as LargeFileUploader).computeFileMd5((progress) => {
        md5Progress.value = Math.round(progress)
      })

      uploadId.value = (uploader.value as LargeFileUploader).getUploadId()

      updateState(UploadState.UPLOADING)
      saveDraftToStorage()
      await (uploader.value as LargeFileUploader).uploadAll()

      updateState(UploadState.MERGING)
      await (uploader.value as LargeFileUploader).merge()
    } catch (error) {
      handleError(error as Error)
    }
  }

  const resumeUpload = async () => {
    const draft = loadDraftFromStorage()
    if (!draft || !businessId.value) {
      if (onProgressUpdate) onProgressUpdate('没有可恢复的上传任务')
      return
    }

    // 如果 uploader 或文件引用丢失，尝试从存储恢复
    if (!uploader.value && currentFile.value) {
      uploader.value = new LargeFileUploader(
        currentFile.value,
        5 * 1024 * 1024,
        (progress, uploaded) => {
          uploadProgress.value = Math.round(progress)
          uploadedChunks.value = uploaded
        },
        (result) => {
          updateState(UploadState.SUCCESS)
          clearDraft()
          if (onSuccess && businessId.value) {
            onSuccess(result, businessId.value)
          }
        },
        (errorMsg) => {
          if (onProgressUpdate) onProgressUpdate(errorMsg)
        },
        'parallel',
        3
      )
    }

    if (!uploader.value || !currentFile.value) {
      if (onProgressUpdate) onProgressUpdate('无法恢复上传任务，请重新选择文件')
      updateState(UploadState.IDLE)
      return
    }

    try {
      updateState(UploadState.COMPUTING_MD5)
      await (uploader.value as LargeFileUploader).computeFileMd5((progress) => {
        md5Progress.value = Math.round(progress)
      })

      updateState(UploadState.UPLOADING)
      await (uploader.value as LargeFileUploader).uploadAll()

      updateState(UploadState.MERGING)
      await (uploader.value as LargeFileUploader).merge()
    } catch (error) {
      handleError(error as Error)
    }
  }

  const pauseUpload = () => {
    if (uploader.value) {
      (uploader.value as LargeFileUploader).pause()
    }
  }

  const cancelUpload = () => {
    if (uploader.value) {
      (uploader.value as LargeFileUploader).cancel()
    }
    clearDraft()
    updateState(UploadState.IDLE)
  }

  const handleError = (error: Error) => {
    if (error.message === '上传已取消') {
      updateState(UploadState.IDLE)
      return
    }
    updateState(UploadState.ERROR)
    console.error('上传失败:', error)
    if (onError) onError(error)
  }

  onUnmounted(() => {
    if (uploader.value && uploadState.value !== UploadState.SUCCESS) {
      saveDraftToStorage()
    }
  })

  return {
    uploadState,
    uploadProgress,
    uploadedChunks,
    totalChunks,
    computingMd5,
    md5Progress,
    businessId,
    uploadId,
    uploader,
    steps,
    startUpload,
    resumeUpload,
    cancelUpload,
    pauseUpload,
    clearDraft,
    setBusinessId
  }
}
