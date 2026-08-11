import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons()
  ],
  shortcuts: {
    // ---- 语义化背景/文字（自动响应暗色模式）----
    // 用法：class="bg-page" / class="bg-card" / class="text-heading"
    // 替代模板中大量 isDark ? 'bg-gray-900' : 'bg-gray-50' 三元表达式
    'bg-page': 'bg-gray-50 dark:bg-gray-900',
    'bg-card': 'bg-white dark:bg-gray-800',
    'bg-card-hover': 'bg-gray-50 dark:bg-gray-700',
    'bg-subtle': 'bg-gray-100 dark:bg-gray-800',
    'bg-input': 'bg-white dark:bg-gray-700',
    // 页面底部/导航栏这类"始终深色"的背景，亮色模式也用深灰/纯黑
    'bg-footer': 'bg-gray-900 dark:bg-black',

    // ---- 语义化文字色 ----
    'text-heading': 'text-gray-900 dark:text-white',
    'text-body': 'text-gray-700 dark:text-gray-300',
    'text-muted': 'text-gray-500 dark:text-gray-400',
    'text-faint': 'text-gray-400 dark:text-gray-500',
    'text-on-primary': 'text-white dark:text-white',
    // 用于深底区域（Footer/Hero深色渐变）的文字
    'text-on-dark': 'text-gray-200 dark:text-gray-200',
    'text-on-dark-muted': 'text-gray-400 dark:text-gray-400',

    // ---- 语义化边框 ----
    'border-base': 'border-gray-200 dark:border-gray-700',
    'border-strong': 'border-gray-300 dark:border-gray-600',
    // 深底区域的分割线（Footer/Hero）
    'border-on-dark': 'border-white/10 dark:border-white/10',

    // ---- 语义化悬停 ----
    'hover:bg-card-hover': 'hover:bg-gray-50 dark:hover:bg-gray-700',
    'hover:text-heading': 'hover:text-gray-900 dark:hover:text-white',

    // ---- 统一圆角档位（禁止散落自定义 rounded-full/rounded-2xl）----
    'r-sm': 'rounded',
    'r-md': 'rounded-lg',
    'r-lg': 'rounded-xl',
    'r-pill': 'rounded-full',

    // ---- 统一阴影档位 ----
    'shadow-card': 'shadow-sm',
    'shadow-card-hover': 'shadow-md',
    'shadow-elevated': 'shadow-lg',

    // ---- 通用卡片交互态：悬浮微抬升 + 阴影过渡 ----
    // 用法：<div class="card-elevated r-md bg-card">
    'card-elevated':
      'bg-card border border-base transition-all duration-200 ease-out will-change-transform shadow-card hover:shadow-card-hover hover:-translate-y-0.5',

    // ---- 常用按钮主色（依赖运行时注入的 --primary-color）----
    'text-accent': 'text-[var(--accent-color)]',
    'bg-accent': 'bg-[var(--accent-color)]',
    // 半透明强调色标签（文章分类/技术栈 Badge）
    'accent-pill':
      'r-pill px-3 py-1 text-xs sm:text-sm inline-flex items-center',

    // ---- 兼容旧定义（不建议新增使用）----
    'bg-primary': 'bg-gray-900',
    'text-primary': 'text-gray-900',
    'text-dark': 'text-gray-800',
    'hover:underline': 'hover:decoration-gray-900 hover:underline-offset-4'
  }
})
