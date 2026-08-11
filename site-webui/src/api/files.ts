import axios from '@/api/axios'
import { calculateFileMd5, calculateChunkMd5 } from '@/utils/fileHash'

export interface FileInfo {
  id: number; url: string; path: string; filename: string
  originalFilename: string; fileExt: string; fileSize: number
  contentType: string | null; platform: string | null; basePath: string | null
  categoryId: number | null; categoryName: string | null; description: string | null
  isPublic: boolean; fileType: string; createdAt: string; updatedAt: string
}

export interface FileCategory {
  id: number; name: string; code: string; description: string | null
  icon: string | null; sortOrder: number; createdAt: string; updatedAt: string
}

export interface FileMetadataForm {
  originalFilename?: string; categoryId?: number; description?: string; isPublic?: boolean
}

export interface UploadProgress { loaded: number; total: number; percentage: number }
export interface ChunkUploadResult { chunkNumber: number; chunkMd5: string; uploadedCount: number; totalChunks: number; isComplete: boolean; progress: number }
export interface InitUploadResult { uploadId: string; chunkSize: number; totalChunks: number; uploadedChunks: number[] }
export interface MergeChunksResult { fileName: string; fileSize: number; fileUrl: string; filePath: string; uploadId: string }
export interface ChunkedUploadProgress { uploadId: string; totalChunks: number; uploadedChunks: number[]; uploadedCount: number; progress: number; status: string }
export interface FileUploadTask {
  id: number; uploadId: string; fileName: string; fileSize: number; fileMd5: string
  chunkSize: number; totalChunks: number; uploadedChunks: number
  status: string; platform: string | null; createdAt: string; updatedAt: string
}

// ==================== 文件 CRUD ====================

export const uploadFile = (file: File, categoryId?: number, description?: string, isPublic?: boolean, onProgress?: (p: UploadProgress) => void) => {
  const fd = new FormData(); fd.append('file', file)
  if (categoryId !== undefined) fd.append('categoryId', String(categoryId))
  if (description) fd.append('description', description)
  if (isPublic !== undefined) fd.append('isPublic', String(isPublic))
  return axios.post('/files/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' }, onUploadProgress: (e) => { if (onProgress && e.total) onProgress({ loaded: e.loaded, total: e.total, percentage: Math.round((e.loaded * 100) / e.total) }) } })
}

export const getFileList = (categoryId?: number, keyword?: string, page = 1, pageSize = 10) => {
  const p: any = {}; if (categoryId !== undefined) p.categoryId = categoryId; if (keyword) p.originalFilename = keyword; p.pageIndex = page; p.pageSize = pageSize
  return axios.get('/files', { params: p })
}

export const getFileById = (id: number) => axios.get(`/files/${id}`)
export const updateFile = (id: number, data: FileMetadataForm) => axios.put(`/files/${id}`, data)
export const deleteFile = (id: number) => axios.delete(`/files/${id}`)
export const batchDeleteFiles = (ids: number[]) => axios.delete('/files/batch', { data: ids })

// ==================== 文件分类 ====================

export const getCategories = () => axios.get('/files/categories')
export const createCategory = (data: { name: string; code: string; description?: string; icon?: string; sortOrder?: number }) => axios.post('/files/categories', data)
export const updateCategory = (id: number, data: { name: string; code: string; description?: string; icon?: string; sortOrder?: number }) => axios.put(`/files/categories/${id}`, data)
export const deleteCategory = (id: number) => axios.delete(`/files/categories/${id}`)

// ==================== 大文件分片上传 ====================

export const initLargeFileUpload = (fileName: string, fileSize: number, chunkSize?: number, fileMd5?: string) =>
  axios.post('/files/large/init', null, { params: { fileName, fileSize, chunkSize, fileMd5 } })

export const uploadChunk = (uploadId: string, chunkNumber: number, chunk: File, chunkMd5?: string, signal?: AbortSignal, onProgress?: (p: UploadProgress) => void) => {
  const fd = new FormData(); fd.append('uploadId', uploadId); fd.append('chunkNumber', String(chunkNumber)); fd.append('chunk', chunk)
  if (chunkMd5) fd.append('chunkMd5', chunkMd5)
  return axios.post('/files/large/chunk', fd, { headers: { 'Content-Type': 'multipart/form-data' }, signal, onUploadProgress: (e) => { if (onProgress && e.total) onProgress({ loaded: e.loaded, total: e.total, percentage: Math.round((e.loaded * 100) / e.total) }) } })
}

export const getUploadedChunks = (uploadId: string) => axios.get('/files/large/chunks', { params: { uploadId } })

export const mergeChunks = (uploadId: string, categoryId?: number, description?: string, originalFilename?: string, isPublic?: boolean) =>
  axios.post('/files/large/merge', null, { params: { uploadId, categoryId, description, originalFilename, isPublic }, timeout: 120_000 })

export const cancelUpload = (uploadId: string) => axios.delete('/files/large/cancel', { params: { uploadId } })
export const getUploadProgress = (uploadId: string) => axios.get('/files/large/progress', { params: { uploadId } })

// ==================== 上传任务管理 ====================

export const pauseUpload = (uploadId: string) => axios.post('/files/large/pause', null, { params: { uploadId } })
export const resumeUpload = (uploadId: string) => axios.post('/files/large/resume', null, { params: { uploadId } })
export const getUploadTasks = () => axios.get('/files/large/tasks')
export const deleteTask = (uploadId: string) => axios.delete(`/files/large/tasks/${uploadId}`)

// ==================== LargeFileUploader ====================

export type UploadMode = 'serial' | 'parallel'

class Semaphore {
  private count: number; private queue: (() => void)[] = []
  constructor(count: number) { this.count = count }
  acquire(): Promise<void> { return new Promise(resolve => { if (this.count > 0) { this.count--; resolve() } else this.queue.push(resolve) }) }
  release(): void { this.count++; if (this.queue.length > 0) this.queue.shift()?.() }
}

class LargeFileUploader {
  private uploadId = ''
  private file: File
  private chunkSize: number
  private totalChunks = 0
  private uploadedChunks: Set<number> = new Set()
  private fileMd5 = ''
  private onProgress: ((progress: number, uploadedChunks: number, stage: string) => void) | null
  private onComplete: ((result: MergeChunksResult) => void) | null
  private onError: ((error: string) => void) | null
  private uploadMode: UploadMode = 'serial'
  private maxConcurrency = 3
  private isCancelled = false
  private isPaused = false
  private abortControllers: Map<number, AbortController> = new Map()
  private pausePromise: Promise<void> | null = null
  private pauseResolve: (() => void) | null = null
  private isMerging = false
  private maxRetries = 3
  private retryDelays = [1000, 2000, 4000, 8000, 16000]

  /** 带指数退避的重试执行 */
  private async retryWithBackoff<T>(fn: () => Promise<T>, onRetry?: (attempt: number) => void): Promise<T> {
    let lastErr: any
    for (let attempt = 0; attempt <= this.maxRetries; attempt++) {
      try { return await fn() }
      catch (e: any) {
        lastErr = e
        if (e.message === '上传已暂停' || this.isCancelled) throw e
        if (attempt < this.maxRetries) {
          onRetry?.(attempt + 1)
          await new Promise(r => setTimeout(r, this.retryDelays[attempt] || 16000))
        }
      }
    }
    throw lastErr
  }

  constructor(file: File, chunkSize = 5 * 1024 * 1024, onProgress?: (p: number, c: number, s: string) => void, onComplete?: (r: MergeChunksResult) => void, onError?: (e: string) => void, uploadMode: UploadMode = 'serial', maxConcurrency = 3) {
    this.file = file; this.chunkSize = chunkSize; this.totalChunks = Math.ceil(file.size / chunkSize)
    this.onProgress = onProgress || null; this.onComplete = onComplete || null; this.onError = onError || null
    this.uploadMode = uploadMode; this.maxConcurrency = Math.max(1, Math.min(maxConcurrency, 10))
  }

  setUploadMode(mode: UploadMode) { this.uploadMode = mode }
  setMaxConcurrency(c: number) { this.maxConcurrency = Math.max(1, Math.min(c, 10)) }
  getUploadMode() { return this.uploadMode }
  getMaxConcurrency() { return this.maxConcurrency }
  isUploadPaused() { return this.isPaused }

  pause() {
    this.isPaused = true
    if (!this.pausePromise) this.pausePromise = new Promise(r => { this.pauseResolve = r })
    this.abortControllers.forEach(c => c.abort()); this.abortControllers.clear()
  }

  resumeUpload() {
    this.isPaused = false
    this.pauseResolve?.(); this.pausePromise = null; this.pauseResolve = null
  }

  private async waitIfPaused() { if (this.isPaused && this.pausePromise) await this.pausePromise }

  async cancel() {
    this.isCancelled = true; this.isPaused = false
    this.abortControllers.forEach(c => c.abort()); this.abortControllers.clear()
    this.pauseResolve?.(); this.pausePromise = null; this.pauseResolve = null
    if (this.uploadId) try { await cancelUpload(this.uploadId) } catch {}
  }

  resetCancel() { this.isCancelled = false }

  async computeFileMd5(onHashProgress?: (p: number) => void) {
    try {
      this.fileMd5 = await calculateFileMd5(this.file, p => { this.onProgress?.(p, 0, 'hash'); onHashProgress?.(p) })
      return this.fileMd5
    } catch (e: any) { this.onError?.(e.message || 'MD5失败'); throw e }
  }

  getFileMd5() { return this.fileMd5 }

  async init() {
    const r: any = await initLargeFileUpload(this.file.name, this.file.size, this.chunkSize, this.fileMd5)
    const d = (r && r.data && r.data.uploadId) ? r.data : (r && r.uploadId) ? r : null
    if (d && d.uploadId) {
      this.uploadId = d.uploadId; this.totalChunks = Number(d.totalChunks) || 0
      if (d.uploadedChunks?.length > 0) {
        this.uploadedChunks = new Set(d.uploadedChunks.map(Number))
        this.onProgress?.((this.uploadedChunks.size / this.totalChunks) * 100, this.uploadedChunks.size, 'upload')
      }
      return this.uploadId
    }
    console.error('初始化上传失败:', r)
    throw new Error('初始化失败: ' + ((r && r.data && r.data.msg) || (r && r.msg) || JSON.stringify(r || {})))
  }

  async resume() {
    if (!this.uploadId) throw new Error('请先初始化上传')
    const r = await getUploadedChunks(this.uploadId)
    if (r.data) {
      this.uploadedChunks = new Set(r.data.uploadedChunks || [])
      this.onProgress?.((this.uploadedChunks.size / this.totalChunks) * 100, this.uploadedChunks.size, 'upload')
    }
  }

  async uploadAll(verifyChunkMd5 = true, mode?: UploadMode) {
    if (!this.uploadId) await this.init()
    return (mode || this.uploadMode) === 'parallel' ? this.uploadParallel(verifyChunkMd5) : this.uploadSerial(verifyChunkMd5)
  }

  private async uploadSerial(verifyChunkMd5: boolean) {
    for (let i = 1; i <= this.totalChunks; i++) {
      if (this.isCancelled) throw new Error('上传已取消')
      await this.waitIfPaused()
      if (this.isCancelled) throw new Error('上传已取消')
      if (this.uploadedChunks.has(i)) continue
      const chunk = this.file.slice((i - 1) * this.chunkSize, Math.min(i * this.chunkSize, this.file.size))
      try {
        const md5 = verifyChunkMd5 ? await calculateChunkMd5(chunk) : undefined
        await this.retryWithBackoff(() => this.uploadChunk(i, chunk, md5),
          (attempt) => this.onProgress?.(Math.round((this.uploadedChunks.size / this.totalChunks) * 100), this.uploadedChunks.size, `retry-${attempt}`))
        this.uploadedChunks.add(i); this.notifyProgress()
      } catch (e: any) {
        if (e.message === '上传已暂停') { i--; continue }
        if (!this.isCancelled) { this.onError?.(e.message); throw e }
      }
    }
  }

  private async uploadParallel(verifyChunkMd5: boolean) {
    const sem = new Semaphore(this.maxConcurrency); const tasks: Promise<void>[] = []; const failed: number[] = []
    for (let i = 1; i <= this.totalChunks; i++) if (!this.uploadedChunks.has(i)) tasks.push(this.processChunk(i, sem, verifyChunkMd5, failed))
    await Promise.all(tasks)
    if (failed.length > 0 && !this.isCancelled) {
      for (const i of failed) if (!this.uploadedChunks.has(i)) {
        await sem.acquire()
        try { await this.waitIfPaused(); const c = this.file.slice((i - 1) * this.chunkSize, Math.min(i * this.chunkSize, this.file.size)); const md5 = verifyChunkMd5 ? await calculateChunkMd5(c) : undefined; await this.uploadChunk(i, c, md5); this.uploadedChunks.add(i); this.notifyProgress() } catch {}
        finally { sem.release() }
      }
    }
  }

  private async processChunk(i: number, sem: Semaphore, v: boolean, failed: number[]) {
    await sem.acquire()
    try {
      if (this.isCancelled) return; await this.waitIfPaused(); if (this.isCancelled) return; if (this.uploadedChunks.has(i)) return
      const c = this.file.slice((i - 1) * this.chunkSize, Math.min(i * this.chunkSize, this.file.size))
      const ctrl = new AbortController(); this.abortControllers.set(i, ctrl)
      try {
        const md5 = v ? await calculateChunkMd5(c) : undefined
        await this.retryWithBackoff(() => uploadChunk(this.uploadId, i, c as File, md5, ctrl.signal))
        this.uploadedChunks.add(i); this.notifyProgress()
      }
      catch (e: any) { if (e.message === '上传已暂停') failed.push(i); else if (!this.isCancelled) { this.onError?.(e.message); throw e } }
      finally { this.abortControllers.delete(i) }
    } finally { sem.release() }
  }

  private notifyProgress() { this.onProgress?.(Math.round((this.uploadedChunks.size / this.totalChunks) * 100), this.uploadedChunks.size, 'upload') }
  private async uploadChunk(n: number, c: Blob, md5?: string) { await uploadChunk(this.uploadId, n, c as File, md5) }

  async merge(categoryId?: number, description?: string, originalFilename?: string, isPublic?: boolean) {
    if (!this.uploadId) throw new Error('请先上传文件')
    if (this.isMerging) throw new Error('正在合并中')
    this.isMerging = true
    try {
      const r = await mergeChunks(this.uploadId, categoryId, description, originalFilename, isPublic)
      if (r.data) { const result = r.data as MergeChunksResult; this.onComplete?.(result); return result }
      throw new Error('合并失败')
    } catch (e: any) { this.onError?.(e.message || '合并失败'); throw e } finally { this.isMerging = false }
  }

  async getProgress() {
    if (!this.uploadId) throw new Error('请先初始化上传')
    const r = await getUploadProgress(this.uploadId)
    if (r.data) return r.data as ChunkedUploadProgress
    throw new Error('获取进度失败')
  }

  getUploadedChunks() { return Array.from(this.uploadedChunks) }
  getTotalChunks() { return this.totalChunks }
  getUploadId() { return this.uploadId }
  getFile() { return this.file }
}

export { LargeFileUploader }
