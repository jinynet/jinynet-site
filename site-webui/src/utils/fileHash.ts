import SparkMD5 from 'spark-md5'

/**
 * Worker 实例缓存（复用同一 Worker）
 */
let _md5Worker: Worker | null = null
function getMd5Worker(): Worker | null {
  try {
    if (!_md5Worker) {
      _md5Worker = new Worker(new URL('./md5.worker.ts', import.meta.url), { type: 'module' })
    }
    return _md5Worker
  } catch { return null }
}

/**
 * Web Worker 方式计算文件 MD5（推荐，不阻塞主线程）
 */
function md5WithWorker(file: File, chunkSize: number, onProgress?: (p: number) => void): Promise<string> {
  return new Promise((resolve, reject) => {
    const worker = getMd5Worker()
    if (!worker) {
      // Worker 不可用时回退到主线程
      md5MainThread(file, chunkSize, onProgress).then(resolve).catch(reject)
      return
    }
    const handler = (e: MessageEvent) => {
      const { type, progress, md5, message } = e.data
      if (type === 'progress') onProgress?.(progress)
      else if (type === 'done') { worker.removeEventListener('message', handler); resolve(md5) }
      else if (type === 'error') { worker.removeEventListener('message', handler); reject(new Error(message)) }
    }
    worker.addEventListener('message', handler)
    worker.postMessage({ file, chunkSize })
  })
}

/**
 * 主线程方式计算文件 MD5（回退方案）
 */
function md5MainThread(file: File, chunkSize: number, onProgress?: (p: number) => void): Promise<string> {
  return new Promise((resolve, reject) => {
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()
    let offset = 0

    fileReader.onload = function (e) {
      try {
        spark.append(e.target?.result as ArrayBuffer)
        offset += chunkSize
        onProgress?.(Math.round(Math.min((offset / file.size) * 100, 100)))
        if (offset < file.size) loadNext()
        else resolve(spark.end())
      } catch (e) { reject(e) }
    }
    fileReader.onerror = (e) => reject(new Error(`读取失败: ${(e.target as any)?.error?.message || '未知错误'}`))
    fileReader.onabort = () => reject(new Error('读取被取消'))

    function loadNext() {
      fileReader.readAsArrayBuffer(file.slice(offset, offset + chunkSize))
    }
    loadNext()
  })
}

/**
 * 计算文件 MD5（优先使用 Web Worker，不可用时回退主线程）
 * @param file 文件对象
 * @param onProgress 进度回调 (0-100)
 * @param chunkSize 分块大小，默认 2MB
 */
export async function calculateFileMd5(
  file: File,
  onProgress?: (progress: number) => void,
  chunkSize: number = 2 * 1024 * 1024
): Promise<string> {
  return md5WithWorker(file, chunkSize, onProgress)
}

/**
 * 计算单个分片的 MD5（主线程，分片小不影响性能）
 */
export async function calculateChunkMd5(chunk: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const spark = new SparkMD5.ArrayBuffer()
    const fileReader = new FileReader()
    fileReader.onload = function (e) {
      try { spark.append(e.target?.result as ArrayBuffer); resolve(spark.end()) } catch (e) { reject(e) }
    }
    fileReader.onerror = (e) => reject(new Error(`分片读取失败: ${(e.target as any)?.error?.message || '未知错误'}`))
    fileReader.readAsArrayBuffer(chunk)
  })
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
