/// <reference lib="webworker" />
/**
 * MD5 计算 Web Worker
 * 使用 FileReaderSync + SparkMD5 在 Worker 线程计算，避免阻塞主线程
 */
import SparkMD5 from 'spark-md5'

self.onmessage = (e: MessageEvent<{ file: File; chunkSize: number }>) => {
  const { file, chunkSize } = e.data
  const spark = new SparkMD5.ArrayBuffer()
  const reader = new FileReaderSync()
  let offset = 0

  try {
    while (offset < file.size) {
      const end = Math.min(offset + chunkSize, file.size)
      const slice = file.slice(offset, end)
      const buffer = reader.readAsArrayBuffer(slice)
      spark.append(buffer)
      offset = end
      self.postMessage({ type: 'progress', progress: Math.round((offset / file.size) * 100) })
    }
    self.postMessage({ type: 'done', md5: spark.end() })
  } catch (err: any) {
    self.postMessage({ type: 'error', message: err.message || 'MD5 计算失败' })
  }
}
