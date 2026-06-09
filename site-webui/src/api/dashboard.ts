import axios from './axios'

export interface DashboardStats {
  articles: number
  projects: number
  views: number
  categories: number
}

export interface DashboardArticle {
  id: number
  title: string
  slug: string
  status: string
  view_count?: number
  viewCount?: number
  published_at?: string | null
  publishedAt?: string | null
  updated_at?: string
  updatedAt?: string
  category?: {
    id: number
    name: string
  }
}

export interface SystemMonitorData {
  cpu: {
    cores: number
    physicalCores: number
    usage: number
    name: string
    vendor: string
  }
  memory: {
    total: string
    totalBytes: number
    used: string
    usedBytes: number
    available: string
    availableBytes: number
    usagePercent: number
  }
  disk: {
    disks: Array<{
      name: string
      model: string
      size: string
      sizeBytes: number
    }>
    fileSystems: Array<{
      name: string
      mount: string
      type: string
      total: string
      totalBytes: number
      used: string
      usedBytes: number
      free: string
      freeBytes: number
      usagePercent: number
    }>
    total: string
    totalBytes: number
    used: string
    usedBytes: number
    usagePercent: number
  }
  network: {
    interfaces: Array<{
      name: string
      displayName: string
      mac: string
      ipv4: string[]
      ipv6: string[]
      bytesSent: string
      bytesSentBytes: number
      bytesReceived: string
      bytesReceivedBytes: number
      speed: string
      speedBytes: number
    }>
    totalSent: string
    totalSentBytes: number
    totalReceived: string
    totalReceivedBytes: number
  }
  system: {
    os: string
    hostname: string
    uptime: string
    uptimeSeconds: number
    processCount: number
    threadCount: number
    jvmVersion: string
    jvmVendor: string
    jvmTotalMemory: string
    jvmTotalMemoryBytes: number
    jvmMaxMemory: string
    jvmMaxMemoryBytes: number
    jvmFreeMemory: string
    jvmFreeMemoryBytes: number
    jvmUsedMemory: string
    jvmUsedMemoryBytes: number
  }
}

export const getDashboardStats = () => {
  return axios.get<{ data: DashboardStats }>('/admin/dashboard/stats')
}

export const getLatestArticles = () => {
  return axios.get<{ data: DashboardArticle[] }>('/admin/dashboard/latest-articles')
}

export const getHotArticles = () => {
  return axios.get<{ data: DashboardArticle[] }>('/admin/dashboard/hot-articles')
}

export const getSystemMonitor = () => {
  return axios.get<{ data: SystemMonitorData }>('/admin/dashboard/system-monitor')
}
