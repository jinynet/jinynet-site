import type { ProjectList, ProjectStack } from '@/api/projects'
import { h } from 'vue'
import { NTag, NButton } from 'naive-ui'

export const statusOptions = [
  { label: '全部', value: undefined },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '暂停', value: 'paused' }
]

export const projectStatusOptions = [
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '暂停', value: 'paused' }
]

export const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'default' => {
  switch (status) {
    case 'active': return 'success'
    case 'completed': return 'default'
    case 'paused': return 'warning'
    default: return 'default'
  }
}

export const getStatusText = (status: string): string => {
  switch (status) {
    case 'active': return '进行中'
    case 'completed': return '已完成'
    case 'paused': return '暂停'
    default: return status
  }
}

export interface TableColumn {
  title: string
  key: string
  ellipsis?: boolean
  width?: number
  minWidth?: number
  sorter?: boolean
  render?: (row: any) => any
}

export const projectColumns: TableColumn[] = [
  { title: '项目名称', key: 'name', ellipsis: true, minWidth: 180 },
  { title: '别名', key: 'slug', ellipsis: true, minWidth: 120 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: ProjectList) => {
      const type = getStatusType(row.status)
      const text = getStatusText(row.status)
      return h(NTag, { type, size: 'small' }, () => text)
    }
  },
  { title: '角色', key: 'role', width: 120, ellipsis: true },
  {
    title: '时间',
    key: 'dateRange',
    width: 180,
    render: (row: ProjectList) => {
      if (!row.startDate) return '—'
      const start = row.startDate.split('T')[0]
      const end = row.endDate ? row.endDate.split('T')[0] : '进行中'
      return `${start} ~ ${end}`
    }
  },
  {
    title: '项目链接',
    key: 'projectUrl',
    width: 120,
    ellipsis: true,
    render: (row: ProjectList) => {
      if (row.projectUrl) {
        return h('a', { href: row.projectUrl, target: '_blank', class: 'text-blue-500 hover:underline', onClick: (e: Event) => e.stopPropagation() }, '访问')
      }
      return '—'
    }
  },
  {
    title: '代码仓库',
    key: 'repoUrl',
    width: 100,
    ellipsis: true,
    render: (row: ProjectList) => {
      if (row.repoUrl) {
        return h('a', { href: row.repoUrl, target: '_blank', class: 'text-blue-500 hover:underline', onClick: (e: Event) => e.stopPropagation() }, '访问')
      }
      return '—'
    }
  },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 120,
    render: (row: ProjectList) => {
      return row.createdAt ? row.createdAt.split('T')[0] : '—'
    }
  }
]

export const projectActions = (callbacks: {
  onEdit: (id: number | string) => void
  onDelete: (id: number | string) => void
}) => ({
  title: '操作',
  key: 'actions',
  width: 100,
  fixed: 'right' as const,
  render: (row: ProjectList) => {
    return h('div', { class: 'flex gap-1' }, [
      h(NButton, { text: true, size: 'tiny' as const, onClick: () => callbacks.onEdit(row.id) }, () => '编辑'),
      h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => callbacks.onDelete(row.id) }, () => '删除')
    ])
  }
})

export const stackColumns: TableColumn[] = [
  { title: '技术栈名称', key: 'name', width: 150 },
  {
    title: '类别',
    key: 'category',
    width: 120,
    sorter: true,
    render: (row: ProjectStack) => {
      const categoryMap: Record<string, string> = {
        language: '编程语言',
        framework: '框架',
        database: '数据库',
        middleware: '中间件',
        tools: '工具'
      }
      return categoryMap[row.category] || row.category
    }
  },
  {
    title: '图标',
    key: 'icon',
    width: 80,
    render: (row: ProjectStack) => {
      if (row.icon) {
        return h('img', { src: row.icon, class: 'w-6 h-6 object-contain' })
      }
      return '—'
    }
  },
  {
    title: '颜色',
    key: 'color',
    width: 100,
    render: (row: ProjectStack) => {
      if (row.color) {
        return h('div', {
          class: 'w-8 h-8 rounded-full border-2 border-gray-300',
          style: { backgroundColor: row.color }
        })
      }
      return '—'
    }
  },
  { title: '描述', key: 'description', ellipsis: true, minWidth: 150 },
  { title: '排序', key: 'sortOrder', width: 80 }
]

export const stackActions = (callbacks: {
  onEdit: (row: ProjectStack) => void
  onDelete: (row: ProjectStack) => void
}) => ({
  title: '操作',
  key: 'actions',
  width: 100,
  fixed: 'right' as const,
  render: (row: ProjectStack) => {
    return h('div', { class: 'flex gap-1' }, [
      h(NButton, { text: true, size: 'tiny' as const, onClick: () => callbacks.onEdit(row) }, () => '编辑'),
      h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => callbacks.onDelete(row) }, () => '删除')
    ])
  }
})

export const stackFormConfig = {
  name: { label: '技术栈名称', placeholder: '请输入技术栈名称', required: true },
  category: { label: '类别', required: true },
  icon: { label: '图标URL', placeholder: '请输入图标URL', required: false },
  color: { label: '颜色', placeholder: '#666666', required: false },
  description: { label: '描述', placeholder: '请输入描述', required: false }
}

export const stackCategoryOptions = [
  { label: '编程语言', value: 'language' },
  { label: '技术框架', value: 'framework' },
  { label: '数据库', value: 'database' },
  { label: '中间件', value: 'middleware' },
  { label: '工具', value: 'tools' }
]

export const defaultStackForm = (): {
  name: string
  category: 'language' | 'framework' | 'database' | 'tools'
  icon: string
  color: string
  description: string
  sortOrder: number
} => ({
  name: '',
  category: 'language',
  icon: '',
  color: '',
  description: '',
  sortOrder: 0
})