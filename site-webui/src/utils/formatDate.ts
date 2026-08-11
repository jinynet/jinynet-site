/**
 * 日期格式化工具
 *
 * 统一项目中所有日期展示格式，避免各组件重复实现导致格式不一致。
 */

/**
 * 格式化为 YYYY-MM-DD（例如 2026-08-11）
 */
export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

/**
 * 格式化为 YYYY-MM-DD HH:mm（例如 2026-08-11 14:30）
 */
export function formatDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/**
 * 格式化为中文长日期（例如 2026年8月11日）
 */
export function formatDateLong(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

/**
 * 格式化日期区间，例如 2026-01-01 - 2026-08-11，结束时间为空显示"至今"
 */
export function formatDateRange(
  start: string | null | undefined,
  end: string | null | undefined
): string {
  if (!start) return ''
  const startDate = start.split('T')[0]
  const endDate = end ? end.split('T')[0] : '至今'
  return `${startDate} - ${endDate}`
}
