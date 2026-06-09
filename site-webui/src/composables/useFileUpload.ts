/**
 * 文件上传状态管理（简化版 — 无草稿逻辑）。
 *
 * 状态机: IDLE → COMPUTING_MD5 → UPLOADING → MERGING → SUCCESS
 *                                        ↘ ERROR
 */

import { ref } from 'vue'
import { LargeFileUploader, type MergeChunksResult, type UploadMode } from '@/api/files'

export const UploadState = {
  IDLE: 'idle',
  COMPUTING_MD5: 'computing_md5',
  UPLOADING: 'uploading',
  MERGING: 'merging',
  SUCCESS: 'success',
  ERROR: 'error'
} as const

export type UploadStateValue = typeof UploadState[keyof typeof UploadState]

export function useFileUpload() {
  const uploadState = ref<UploadStateValue>(UploadState.IDLE)
  const uploader = ref<LargeFileUploader | null>(null)
  const currentFile = ref<File | null>(null)
  const progress = ref(0)
  const uploadedChunks = ref(0)
  const totalChunks = ref(0)
  const uploadId = ref<string>('')
  const errorMessage = ref('')
  const isPaused = ref(false)

  function updateState(s: UploadStateValue) { uploadState.value = s }

  async function startUpload(file: File, chunkSize = 5 * 1024 * 1024, mode: UploadMode = 'serial', concurrency = 3) {
    currentFile.value = file
    errorMessage.value = ''
    totalChunks.value = Math.ceil(file.size / chunkSize)

    uploader.value = new LargeFileUploader(file, chunkSize,
      (p, c, stage) => {
        if (stage === 'hash') updateState(UploadState.COMPUTING_MD5)
        else updateState(UploadState.UPLOADING)
        progress.value = p; uploadedChunks.value = c
      },
      (_result: MergeChunksResult) => {
        updateState(UploadState.SUCCESS)
        progress.value = 100
      },
      (err: string) => {
        updateState(UploadState.ERROR)
        errorMessage.value = err
      },
      mode, concurrency
    )

    try {
      updateState(UploadState.COMPUTING_MD5)
      await uploader.value.computeFileMd5()
      await uploader.value.init()
      uploadId.value = uploader.value.getUploadId()
      totalChunks.value = uploader.value.getTotalChunks()
      await uploader.value.uploadAll()
      updateState(UploadState.MERGING)
      await uploader.value.merge()
    } catch (e: any) {
      if (e.message !== '上传已暂停' && e.message !== '上传已取消')
        updateState(UploadState.ERROR)
    }
  }

  function pause() {
    uploader.value?.pause()
    isPaused.value = true
  }

  function resume() {
    uploader.value?.resumeUpload()
    isPaused.value = false
  }

  async function cancel() {
    await uploader.value?.cancel()
    updateState(UploadState.IDLE)
  }

  function reset() {
    uploader.value = null; currentFile.value = null
    progress.value = 0; uploadedChunks.value = 0; totalChunks.value = 0
    uploadId.value = ''; errorMessage.value = ''; isPaused.value = false
    updateState(UploadState.IDLE)
  }

  return {
    uploadState, uploader, currentFile, progress, uploadedChunks, totalChunks,
    uploadId, errorMessage, isPaused,
    startUpload, pause, resume, cancel, reset
  }
}
