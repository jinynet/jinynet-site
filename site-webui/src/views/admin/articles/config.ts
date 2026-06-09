export const statusOptions = [
  { label: '全部', value: undefined },
  { label: '已发布', value: 'published' },
  { label: '草稿', value: 'draft' },
  { label: '私密', value: 'private' }
]

export const getStatusType = (status: string): 'success' | 'warning' | 'info' | 'default' => {
  switch (status) {
    case 'published': return 'success'
    case 'draft': return 'warning'
    case 'private': return 'info'
    default: return 'default'
  }
}

export const getStatusText = (status: string): string => {
  switch (status) {
    case 'published': return '已发布'
    case 'draft': return '草稿'
    case 'private': return '私密'
    default: return status
  }
}