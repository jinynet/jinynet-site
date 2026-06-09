<template>
  <div>
    <div v-if="loading" class="flex justify-center items-center py-12">
      <n-spin size="large" />
    </div>
    
    <div v-else>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-6 mb-6">
        <n-card hoverable class="cursor-pointer" @click="goTo('/admin/articles')">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-gray-500 text-sm">文章总数</p>
              <p class="text-3xl font-bold text-dark dark:text-gray-100 mt-1">{{ stats.articles }}</p>
            </div>
            <div class="w-12 h-12 bg-gray-900/10 dark:bg-gray-100/10 rounded-lg flex items-center justify-center">
              <Document class="w-6 h-6 text-gray-900 dark:text-gray-300" />
            </div>
          </div>
        </n-card>

        <n-card hoverable class="cursor-pointer" @click="goTo('/admin/projects')">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-gray-500 text-sm">项目数量</p>
              <p class="text-3xl font-bold text-dark dark:text-gray-100 mt-1">{{ stats.projects }}</p>
            </div>
            <div class="w-12 h-12 bg-gray-900/10 dark:bg-gray-100/10 rounded-lg flex items-center justify-center">
              <Folder class="w-6 h-6 text-gray-900 dark:text-gray-300" />
            </div>
          </div>
        </n-card>

        <n-card hoverable class="cursor-pointer" @click="goTo('/admin/articles')">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-gray-500 text-sm">总阅读量</p>
              <p class="text-3xl font-bold text-dark dark:text-gray-100 mt-1">{{ formatNumber(stats.views) }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-500/10 rounded-lg flex items-center justify-center">
              <Eye class="w-6 h-6 text-blue-500" />
            </div>
          </div>
        </n-card>

        <n-card hoverable class="cursor-pointer" @click="goToCategory">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-gray-500 text-sm">分类数量</p>
              <p class="text-3xl font-bold text-dark dark:text-gray-100 mt-1">{{ stats.categories }}</p>
            </div>
            <div class="w-12 h-12 bg-orange-500/10 rounded-lg flex items-center justify-center">
              <HashTag class="w-6 h-6 text-orange-500" />
            </div>
          </div>
        </n-card>
      </div>

      <!-- 系统监控 - 可折叠区域 -->
      <n-collapse>
        <n-collapse-item title="系统监控" name="system-monitor">
          <template #header>
            <div class="flex items-center justify-between w-full pr-4">
              <span class="font-medium">系统监控</span>
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-2 text-sm">
                  <span class="text-gray-500">CPU: {{ systemMonitor.cpu?.usage ?? 0 }}%</span>
                  <span class="text-gray-500">内存: {{ systemMonitor.memory?.usagePercent ?? 0 }}%</span>
                  <span class="text-gray-500">磁盘: {{ systemMonitor.disk?.usagePercent ?? 0 }}%</span>
                </div>
                <n-button size="small" @click.stop="refreshMonitor" :loading="refreshing">
                  <template #icon>
                    <Refresh class="w-4 h-4" />
                  </template>
                  刷新
                </n-button>
              </div>
            </div>
          </template>

          <div class="space-y-4">
            <!-- 快速概览 -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
              <n-card title="CPU" size="small">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-xl font-bold">{{ systemMonitor.cpu?.usage ?? 0 }}%</span>
                  <Cpu class="w-6 h-6 text-blue-500" />
                </div>
                <n-progress type="line" :percentage="systemMonitor.cpu?.usage ?? 0" :color="getCpuColor(systemMonitor.cpu?.usage ?? 0)" />
                <p class="text-xs text-gray-500 mt-2 truncate">{{ systemMonitor.cpu?.name || '-' }}</p>
              </n-card>

              <n-card title="内存" size="small">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-xl font-bold">{{ systemMonitor.memory?.usagePercent ?? 0 }}%</span>
                  <Database class="w-6 h-6 text-green-500" />
                </div>
                <n-progress type="line" :percentage="systemMonitor.memory?.usagePercent ?? 0" :color="getMemoryColor(systemMonitor.memory?.usagePercent ?? 0)" />
                <p class="text-xs text-gray-500 mt-2">{{ systemMonitor.memory?.used || '-' }} / {{ systemMonitor.memory?.total || '-' }}</p>
              </n-card>

              <n-card title="磁盘" size="small">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-xl font-bold">{{ systemMonitor.disk?.usagePercent ?? 0 }}%</span>
                  <Database class="w-6 h-6 text-yellow-500" />
                </div>
                <n-progress type="line" :percentage="systemMonitor.disk?.usagePercent ?? 0" :color="getDiskColor(systemMonitor.disk?.usagePercent ?? 0)" />
                <p class="text-xs text-gray-500 mt-2">{{ systemMonitor.disk?.used || '-' }} / {{ systemMonitor.disk?.total || '-' }}</p>
              </n-card>

              <n-card title="系统" size="small">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-xl font-bold">{{ systemMonitor.system?.processCount || 0 }}</span>
                  <Terminal class="w-6 h-6 text-purple-500" />
                </div>
                <p class="text-xs text-gray-500">进程数</p>
                <p class="text-xs text-gray-500 mt-1 truncate">{{ systemMonitor.system?.os || '-' }}</p>
              </n-card>
            </div>

            <!-- 详细信息 -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <!-- 网络流量 -->
              <n-card title="网络流量" size="small">
                <div class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <ArrowUp class="w-4 h-4 text-green-500" />
                      <span class="text-sm">上传</span>
                    </div>
                    <span class="font-medium text-sm">{{ systemMonitor.network?.totalSent || '0 B' }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <ArrowDown class="w-4 h-4 text-blue-500" />
                      <span class="text-sm">下载</span>
                    </div>
                    <span class="font-medium text-sm">{{ systemMonitor.network?.totalReceived || '0 B' }}</span>
                  </div>
                </div>
              </n-card>

              <!-- JVM信息 -->
              <n-card title="JVM信息" size="small">
                <div class="space-y-2 text-sm">
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Java版本</span>
                    <span class="font-medium">{{ systemMonitor.system?.jvmVersion || '-' }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">已使用内存</span>
                    <span class="font-medium">{{ systemMonitor.system?.jvmUsedMemory || '-' }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">最大内存</span>
                    <span class="font-medium">{{ systemMonitor.system?.jvmMaxMemory || '-' }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">运行时间</span>
                    <span class="font-medium">{{ systemMonitor.system?.uptime || '-' }}</span>
                  </div>
                </div>
              </n-card>
            </div>

            <!-- 刷新间隔提示 -->
            <div class="text-xs text-gray-400 text-center">
              系统监控数据每 10 秒自动刷新一次
            </div>
          </div>
        </n-collapse-item>
      </n-collapse>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6 items-start">
        <div class="lg:col-span-2 space-y-6">
          <n-card title="最新文章">
          <div v-if="latestArticles.length === 0" class="flex flex-col items-center justify-center py-12">
            <div class="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mb-4">
              <Document class="w-8 h-8 text-gray-400 dark:text-gray-500" />
            </div>
            <p class="text-gray-500 mb-2">暂无文章</p>
          </div>
          <div v-else class="space-y-3">
            <div
              v-for="article in latestArticles"
              :key="article.id"
              class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
            >
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-gray-700 font-medium truncate">{{ article.title }}</span>
                  <n-tag :type="article.status === 'published' ? 'success' : 'warning'" size="small">
                    {{ article.status === 'published' ? '已发布' : article.status === 'draft' ? '草稿' : '私密' }}
                  </n-tag>
                </div>
                <div class="flex items-center gap-3 text-sm text-gray-400 dark:text-gray-500">
                  <span>{{ article.category?.name || '-' }}</span>
                  <span>{{ formatNumber(article.viewCount ?? 0) }} 阅读</span>
                </div>
              </div>
              <div class="flex items-center gap-2 ml-3">
                <n-button text size="small" @click="viewArticle(article.id)">查看</n-button>
                <n-button text size="small" @click="editArticle(article.id)">编辑</n-button>
              </div>
            </div>
          </div>
        </n-card>

        <n-card title="热门文章">
          <div v-if="hotArticles.length === 0" class="flex flex-col items-center justify-center py-12">
            <div class="w-16 h-16 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mb-4">
              <Eye class="w-8 h-8 text-gray-400 dark:text-gray-500" />
            </div>
            <p class="text-gray-500 mb-2">暂无热门文章</p>
            <p class="text-gray-400 dark:text-gray-500 text-sm">暂无阅读数据，发布文章后将根据阅读量排序</p>
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="(article, index) in hotArticles"
              :key="article.id"
              class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer"
              @click="viewArticle(article.id)"
            >
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <span
                  class="w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0"
                  :class="getRankClass(index)"
                >
                  {{ index + 1 }}
                </span>
                <span class="text-gray-700 truncate">{{ article.title }}</span>
              </div>
              <span class="text-gray-400 dark:text-gray-500 text-sm flex-shrink-0 ml-3">{{ formatNumber(article.viewCount ?? 0) }} 阅读</span>
            </div>
          </div>
        </n-card>
        </div>

        <n-card title="快捷操作" class="lg:col-span-1">
          <div class="space-y-4">
            <div
              class="flex items-center p-4 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer"
              @click="goTo('/admin/articles/add')"
            >
              <div class="w-12 h-12 bg-green-500/10 rounded-lg flex items-center justify-center mr-4 flex-shrink-0">
                <Plus class="w-6 h-6 text-green-500" />
              </div>
              <div>
                <p class="text-gray-700 dark:text-gray-200 font-medium">写文章</p>
                <p class="text-gray-400 dark:text-gray-500 text-sm">快速创建新文章</p>
              </div>
            </div>
            <div
              class="flex items-center p-4 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer"
              @click="goTo('/admin/projects')"
            >
              <div class="w-12 h-12 bg-blue-500/10 rounded-lg flex items-center justify-center mr-4 flex-shrink-0">
                <Grid class="w-6 h-6 text-blue-500" />
              </div>
              <div>
                <p class="text-gray-700 dark:text-gray-200 font-medium">项目管理</p>
                <p class="text-gray-400 dark:text-gray-500 text-sm">管理您的项目</p>
              </div>
            </div>
            <div
              class="flex items-center p-4 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer"
              @click="goTo('/admin/settings')"
            >
              <div class="w-12 h-12 bg-orange-500/10 rounded-lg flex items-center justify-center mr-4 flex-shrink-0">
                <Settings class="w-6 h-6 text-orange-500" />
              </div>
              <div>
                <p class="text-gray-700 dark:text-gray-200 font-medium">系统设置</p>
                <p class="text-gray-400 dark:text-gray-500 text-sm">配置系统参数</p>
              </div>
            </div>
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Folder, Eye, HashTag, Plus, Settings, Grid, Cpu, Database, Terminal, ArrowUp, ArrowDown, Refresh } from '@/icons'
import { NCard, NTag, NButton, NSpin, NProgress, NCollapse, NCollapseItem, useMessage } from 'naive-ui'
import { getDashboardStats, getLatestArticles, getHotArticles, getSystemMonitor, type DashboardArticle, type SystemMonitorData } from '@/api/dashboard'

const router = useRouter()
const message = useMessage()

const loading = ref(true)
const refreshing = ref(false)
const stats = ref({
  articles: 0,
  projects: 0,
  views: 0,
  categories: 0
})

const latestArticles = ref<DashboardArticle[]>([])
const hotArticles = ref<DashboardArticle[]>([])
const systemMonitor = ref<SystemMonitorData>({
  cpu: { cores: 0, physicalCores: 0, usage: 0, name: '', vendor: '' },
  memory: { total: '', totalBytes: 0, used: '', usedBytes: 0, available: '', availableBytes: 0, usagePercent: 0 },
  disk: { disks: [], fileSystems: [], total: '', totalBytes: 0, used: '', usedBytes: 0, usagePercent: 0 },
  network: { interfaces: [], totalSent: '', totalSentBytes: 0, totalReceived: '', totalReceivedBytes: 0 },
  system: {
    os: '', hostname: '', uptime: '', uptimeSeconds: 0, processCount: 0, threadCount: 0,
    jvmVersion: '', jvmVendor: '', jvmTotalMemory: '', jvmTotalMemoryBytes: 0,
    jvmMaxMemory: '', jvmMaxMemoryBytes: 0, jvmFreeMemory: '', jvmFreeMemoryBytes: 0,
    jvmUsedMemory: '', jvmUsedMemoryBytes: 0
  }
})

let refreshTimer: number | null = null

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const getRankClass = (index: number) => {
  const classes = ['bg-yellow-500', 'bg-gray-400', 'bg-orange-500']
  return classes[index] || 'bg-gray-300'
}

const getCpuColor = (percent: number) => {
  if (percent >= 80) return '#f0a020'
  if (percent >= 90) return '#e53935'
  return '#18a058'
}

const getMemoryColor = (percent: number) => {
  if (percent >= 80) return '#f0a020'
  if (percent >= 90) return '#e53935'
  return '#2080f0'
}

const getDiskColor = (percent: number) => {
  if (percent >= 80) return '#f0a020'
  if (percent >= 90) return '#e53935'
  return '#f0a020'
}

const editArticle = (id: number) => {
  router.push(`/admin/articles/edit/${id}`)
}

const viewArticle = (id: number) => {
  router.push(`/articles/id/${id}`)
}

const goTo = (path: string) => {
  router.push(path)
}

const goToCategory = () => {
  router.push({ path: '/admin/articles', query: { tab: 'categories' } })
}

const transformArticle = (article: DashboardArticle): DashboardArticle => {
  return {
    ...article,
    viewCount: article.viewCount ?? article.view_count ?? 0,
    publishedAt: article.publishedAt ?? article.published_at ?? null,
    updatedAt: article.updatedAt ?? article.updated_at ?? ''
  }
}

const loadSystemMonitor = async () => {
  try {
    refreshing.value = true
    const res = await getSystemMonitor()
    if (res.data) {
      systemMonitor.value = res.data as any
    }
  } catch (error) {
    console.error('加载系统监控数据失败:', error)
    message.error('获取系统监控数据失败')
  } finally {
    refreshing.value = false
  }
}

const refreshMonitor = async () => {
  await loadSystemMonitor()
}

const loadData = async () => {
  try {
    loading.value = true
    const [statsRes, latestRes, hotRes] = await Promise.all([
      getDashboardStats(),
      getLatestArticles(),
      getHotArticles(),
      loadSystemMonitor()
    ])
    
    if (statsRes.data) {
      stats.value = statsRes.data as any
    }
    const latestData = (latestRes.data as any)
    if (Array.isArray(latestData)) {
      latestArticles.value = latestData.map(transformArticle)
    } else if (latestData?.data && Array.isArray(latestData.data)) {
      latestArticles.value = latestData.data.map(transformArticle)
    }
    const hotData = (hotRes.data as any)
    if (Array.isArray(hotData)) {
      hotArticles.value = hotData.map(transformArticle)
    } else if (hotData?.data && Array.isArray(hotData.data)) {
      hotArticles.value = hotData.data.map(transformArticle)
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  refreshTimer = window.setInterval(() => {
    loadSystemMonitor()
  }, 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>
