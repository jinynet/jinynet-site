<template>
  <n-drawer
    :show="visible"
    :width="500"
    placement="right"
    @update:show="handleVisibleChange"
  >
    <n-drawer-content title="AI 帮写" closable>
      <!-- 模式切换标签页 -->
      <n-tabs v-model:value="activeTab" type="line" animated class="ai-tabs">
        <!-- 生成文章 -->
        <n-tab-pane name="generate" tab="生成文章">
          <div class="ai-form">
            <n-form label-placement="top" size="small">
              <n-form-item label="文章主题">
                <n-input
                  v-model:value="generateForm.topic"
                  placeholder="请输入文章主题，如：Vue 3 组合式 API 入门"
                />
              </n-form-item>
              <n-form-item label="关键词（可选）">
                <n-input
                  v-model:value="generateForm.keywords"
                  placeholder="多个关键词用逗号分隔"
                />
              </n-form-item>
              <n-form-item label="写作风格">
                <n-select
                  v-model:value="generateForm.style"
                  :options="styleOptions"
                  placeholder="请选择写作风格"
                />
              </n-form-item>
            </n-form>
          </div>
        </n-tab-pane>

        <!-- 续写内容 -->
        <n-tab-pane name="continue" tab="续写内容">
          <div class="ai-form">
            <div class="ai-content-preview">
              <div class="ai-content-preview__label">当前内容预览</div>
              <div class="ai-content-preview__body">{{ contentPreview }}</div>
            </div>
            <n-form label-placement="top" size="small">
              <n-form-item label="续写方向（可选）">
                <n-input
                  v-model:value="continueForm.direction"
                  type="textarea"
                  :rows="2"
                  placeholder="如：总结全文、展开某个观点、添加示例代码等"
                />
              </n-form-item>
            </n-form>
          </div>
        </n-tab-pane>

        <!-- 润色优化 -->
        <n-tab-pane name="optimize" tab="润色优化">
          <div class="ai-form">
            <div class="ai-content-preview">
              <div class="ai-content-preview__label">当前内容预览</div>
              <div class="ai-content-preview__body">{{ contentPreview }}</div>
            </div>
            <n-form label-placement="top" size="small">
              <n-form-item label="优化类型">
                <n-select
                  v-model:value="optimizeForm.type"
                  :options="optimizeTypeOptions"
                  placeholder="请选择优化类型"
                />
              </n-form-item>
            </n-form>
          </div>
        </n-tab-pane>

        <!-- 生成摘要 -->
        <n-tab-pane name="summarize" tab="生成摘要">
          <div class="ai-form">
            <div class="ai-content-preview">
              <div class="ai-content-preview__label">当前内容预览</div>
              <div class="ai-content-preview__body">{{ contentPreview }}</div>
            </div>
            <n-form label-placement="top" size="small">
              <n-form-item>
                <div class="ai-hint">点击下方生成按钮，AI 将根据文章内容自动生成摘要。</div>
              </n-form-item>
            </n-form>
          </div>
        </n-tab-pane>

        <!-- 生成标题 -->
        <n-tab-pane name="title" tab="生成标题">
          <div class="ai-form">
            <div class="ai-content-preview">
              <div class="ai-content-preview__label">当前内容预览</div>
              <div class="ai-content-preview__body">{{ contentPreview }}</div>
            </div>
            <n-form label-placement="top" size="small">
              <n-form-item>
                <div class="ai-hint">点击下方生成按钮，AI 将根据文章内容自动生成标题建议。</div>
              </n-form-item>
            </n-form>
          </div>
        </n-tab-pane>
      </n-tabs>

      <!-- 生成按钮 -->
      <div class="ai-generate-bar">
        <n-button
          v-if="!isGenerating"
          type="primary"
          block
          @click="handleGenerate"
        >
          <template #icon>
            <n-icon :component="Wand" />
          </template>
          生成
        </n-button>
        <n-button
          v-else
          type="error"
          block
          @click="handleStop"
        >
          <template #icon>
            <n-icon :component="PlayerStop" />
          </template>
          停止生成
        </n-button>
      </div>

      <!-- 结果展示区 -->
      <div class="ai-result">
        <div class="ai-result__header">
          <span class="ai-result__title">生成结果</span>
          <span v-if="isGenerating" class="ai-result__loading">
            <n-spin size="small" />
            <span class="ai-result__loading-text">正在生成...</span>
          </span>
          <span v-else-if="resultContent" class="ai-result__done">已完成</span>
          <!-- 源码/预览切换 -->
          <div v-if="resultContent" class="ai-result__view-toggle">
            <n-button-group size="tiny">
              <n-button :type="resultViewMode === 'source' ? 'primary' : 'default'" @click="resultViewMode = 'source'">源码</n-button>
              <n-button :type="resultViewMode === 'preview' ? 'primary' : 'default'" @click="resultViewMode = 'preview'">预览</n-button>
            </n-button-group>
          </div>
        </div>
        <div class="ai-result__body">
          <!-- 源码模式 -->
          <n-input
            v-if="resultViewMode === 'source'"
            v-model:value="resultContent"
            type="textarea"
            readonly
            :rows="14"
            placeholder="AI 生成的内容将显示在这里..."
            class="ai-result__textarea"
          />
          <!-- 预览模式 -->
          <div
            v-else
            class="ai-result__preview prose prose-sm max-w-none"
            :class="{ 'prose-invert': isDark }"
            v-html="renderedResult"
          ></div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <template #footer>
        <div class="ai-footer">
          <!-- 生成文章 / 续写 / 润色 -->
          <template v-if="activeTab === 'generate' || activeTab === 'continue' || activeTab === 'optimize'">
            <n-button size="small" :disabled="!resultContent || isGenerating" @click="handleInsert">
              <template #icon><n-icon :component="Edit" /></template>
              插入到编辑器
            </n-button>
            <n-button size="small" :disabled="!resultContent || isGenerating" @click="handleReplace">
              <template #icon><n-icon :component="FileText" /></template>
              替换内容
            </n-button>
            <n-button size="small" :disabled="!resultContent || isGenerating" @click="handleCopy">
              <template #icon><n-icon :component="Copy" /></template>
              复制
            </n-button>
            <n-button size="small" :disabled="isGenerating" @click="handleRegenerate">
              <template #icon><n-icon :component="Refresh" /></template>
              重新生成
            </n-button>
          </template>

          <!-- 生成摘要 -->
          <template v-else-if="activeTab === 'summarize'">
            <n-button size="small" type="primary" :disabled="!resultContent || isGenerating" @click="handleApplyExcerpt">
              <template #icon><n-icon :component="Check" /></template>
              应用为摘要
            </n-button>
            <n-button size="small" :disabled="!resultContent || isGenerating" @click="handleCopy">
              <template #icon><n-icon :component="Copy" /></template>
              复制
            </n-button>
            <n-button size="small" :disabled="isGenerating" @click="handleRegenerate">
              <template #icon><n-icon :component="Refresh" /></template>
              重新生成
            </n-button>
          </template>

          <!-- 生成标题 -->
          <template v-else-if="activeTab === 'title'">
            <n-button size="small" type="primary" :disabled="!resultContent || isGenerating" @click="handleApplyTitle">
              <template #icon><n-icon :component="Check" /></template>
              应用为标题
            </n-button>
            <n-button size="small" :disabled="!resultContent || isGenerating" @click="handleCopy">
              <template #icon><n-icon :component="Copy" /></template>
              复制
            </n-button>
            <n-button size="small" :disabled="isGenerating" @click="handleRegenerate">
              <template #icon><n-icon :component="Refresh" /></template>
              重新生成
            </n-button>
          </template>
        </div>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  NDrawer, NDrawerContent, NTabs, NTabPane, NForm, NFormItem,
  NInput, NSelect, NButton, NButtonGroup, NIcon, NSpin, useMessage
} from 'naive-ui'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { Wand, PlayerStop, Edit, FileText, Copy, Check, Refresh } from '@/icons'
import { useTheme } from '@/composables/useTheme'
import {
  generateArticle, continueArticle, optimizeArticle,
  summarizeArticle, generateTitle,
  type AiStreamHandler
} from '@/api/articles'

// 主题
const { isDark } = useTheme()

// Markdown 渲染器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' + hljs.highlight(str, { language: lang }).value + '</code></pre>'
      } catch (_) { /* ignore */ }
    }
    const div = document.createElement('div')
    div.textContent = str
    return '<pre class="hljs"><code>' + div.innerHTML + '</code></pre>'
  }
})

// Props
const props = defineProps<{
  visible: boolean
  editorContent: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'insertContent', content: string): void
  (e: 'replaceContent', content: string): void
  (e: 'applyExcerpt', content: string): void
  (e: 'applyTitle', content: string): void
}>()

const message = useMessage()

// 当前激活的标签页
const activeTab = ref('generate')

// 生成文章表单
const generateForm = ref({
  topic: '',
  keywords: '',
  style: '技术博客'
})

// 续写表单
const continueForm = ref({
  direction: ''
})

// 润色表单
const optimizeForm = ref({
  type: '文笔润色'
})

// 风格选项
const styleOptions = [
  { label: '技术博客', value: '技术博客' },
  { label: '随笔', value: '随笔' },
  { label: '教程', value: '教程' },
  { label: '资讯', value: '资讯' }
]

// 优化类型选项
const optimizeTypeOptions = [
  { label: '语法修正', value: '语法修正' },
  { label: '文笔润色', value: '文笔润色' },
  { label: '精简提炼', value: '精简提炼' },
  { label: '扩写丰富', value: '扩写丰富' }
]

// 结果内容
const resultContent = ref('')

// 结果视图模式：源码 / 预览
const resultViewMode = ref<'source' | 'preview'>('source')

// 渲染后的 HTML
const renderedResult = computed(() => {
  if (!resultContent.value) return ''
  try {
    return md.render(resultContent.value)
  } catch {
    return resultContent.value
  }
})

// 生成状态
const isGenerating = ref(false)

// AbortController
let abortController: AbortController | null = null

// 当前内容预览（截断显示）
const contentPreview = computed(() => {
  const content = props.editorContent || ''
  if (content.length <= 300) return content
  return content.slice(0, 300) + '...'
})

// 处理抽屉显示/隐藏
const handleVisibleChange = (val: boolean) => {
  emit('update:visible', val)
  if (!val && isGenerating.value) {
    handleStop()
  }
}

// 执行 AI 生成
const doGenerate = async () => {
  // 根据不同模式校验输入
  if (activeTab.value === 'generate' && !generateForm.value.topic.trim()) {
    message.warning('请输入文章主题')
    return
  }

  const needsContent = activeTab.value !== 'generate'
  if (needsContent && !props.editorContent?.trim()) {
    message.warning('编辑器内容为空，无法执行此操作')
    return
  }

  // 清空结果，开始生成时默认源码模式
  resultContent.value = ''
  resultViewMode.value = 'source'
  isGenerating.value = true

  // 创建 AbortController
  abortController = new AbortController()

  // 流式回调
  const handler: AiStreamHandler = {
    onContent: (chunk: string) => {
      resultContent.value += chunk
    },
    onComplete: (fullContent: string) => {
      isGenerating.value = false
      abortController = null
      if (fullContent) {
        // 生成完成后自动切换到预览模式
        resultViewMode.value = 'preview'
        message.success('生成完成')
      }
    },
    onError: (error: Error) => {
      isGenerating.value = false
      abortController = null
      message.error(`生成失败: ${error.message}`)
    }
  }

  try {
    switch (activeTab.value) {
      case 'generate':
        await generateArticle(
          {
            topic: generateForm.value.topic.trim(),
            keywords: generateForm.value.keywords.trim() || undefined,
            style: generateForm.value.style || undefined
          },
          handler,
          abortController.signal
        )
        break
      case 'continue':
        await continueArticle(
          {
            content: props.editorContent,
            direction: continueForm.value.direction.trim() || undefined
          },
          handler,
          abortController.signal
        )
        break
      case 'optimize':
        await optimizeArticle(
          {
            content: props.editorContent,
            type: optimizeForm.value.type || undefined
          },
          handler,
          abortController.signal
        )
        break
      case 'summarize':
        await summarizeArticle(
          { content: props.editorContent },
          handler,
          abortController.signal
        )
        break
      case 'title':
        await generateTitle(
          { content: props.editorContent },
          handler,
          abortController.signal
        )
        break
    }
  } catch (error) {
    isGenerating.value = false
    abortController = null
    message.error('生成请求异常')
  }
}

// 生成
const handleGenerate = () => {
  doGenerate()
}

// 停止生成
const handleStop = () => {
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  isGenerating.value = false
  message.info('已停止生成')
}

// 重新生成
const handleRegenerate = () => {
  doGenerate()
}

// 插入到编辑器
const handleInsert = () => {
  if (!resultContent.value) return
  emit('insertContent', resultContent.value)
  message.success('已插入到编辑器')
}

// 替换内容
const handleReplace = () => {
  if (!resultContent.value) return
  emit('replaceContent', resultContent.value)
  message.success('已替换编辑器内容')
}

// 应用为摘要
const handleApplyExcerpt = () => {
  if (!resultContent.value) return
  emit('applyExcerpt', resultContent.value)
  message.success('已应用为摘要')
}

// 应用为标题
const handleApplyTitle = () => {
  if (!resultContent.value) return
  emit('applyTitle', resultContent.value)
  message.success('已应用为标题')
}

// 复制
const handleCopy = async () => {
  if (!resultContent.value) return
  try {
    await navigator.clipboard.writeText(resultContent.value)
    message.success('已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

// 抽屉关闭时重置状态
watch(() => props.visible, (val) => {
  if (!val) {
    if (isGenerating.value) {
      handleStop()
    }
    resultContent.value = ''
    resultViewMode.value = 'source'
  }
})
</script>

<style scoped>
.ai-tabs {
  margin-bottom: 12px;
}

.ai-form {
  padding-top: 4px;
}

.ai-content-preview {
  margin-bottom: 16px;
}

.ai-content-preview__label {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 6px;
}

html.dark .ai-content-preview__label {
  color: #d1d5db;
}

.ai-content-preview__body {
  max-height: 120px;
  overflow-y: auto;
  padding: 10px 12px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
  white-space: pre-wrap;
  word-break: break-word;
}

html.dark .ai-content-preview__body {
  background-color: #1f2937;
  border-color: #374151;
  color: #9ca3af;
}

.ai-hint {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.6;
}

html.dark .ai-hint {
  color: #6b7280;
}

.ai-generate-bar {
  margin-bottom: 16px;
}

.ai-result {
  display: flex;
  flex-direction: column;
  min-height: 300px;
}

.ai-result__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ai-result__title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

html.dark .ai-result__title {
  color: #e5e7eb;
}

.ai-result__loading {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ai-result__loading-text {
  font-size: 12px;
  color: #6366f1;
}

.ai-result__done {
  font-size: 12px;
  color: #059669;
}

.ai-result__body {
  flex: 1;
}

.ai-result__view-toggle {
  margin-left: auto;
}

.ai-result__textarea {
  min-height: 300px;
}

:deep(.ai-result__textarea .n-input__textarea-el) {
  min-height: 300px !important;
  line-height: 1.7;
}

/* 预览模式样式 */
.ai-result__preview {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background-color: #ffffff;
  font-size: 14px;
  line-height: 1.7;
}

html.dark .ai-result__preview {
  background-color: #1f2937;
  border-color: #374151;
  color: #d1d5db;
}

.ai-result__preview :deep(h1),
.ai-result__preview :deep(h2),
.ai-result__preview :deep(h3),
.ai-result__preview :deep(h4) {
  font-weight: 600;
  margin-top: 1.2em;
  margin-bottom: 0.6em;
  line-height: 1.3;
}

.ai-result__preview :deep(h1) { font-size: 1.5em; }
.ai-result__preview :deep(h2) { font-size: 1.3em; }
.ai-result__preview :deep(h3) { font-size: 1.15em; }
.ai-result__preview :deep(h4) { font-size: 1em; }

html.dark .ai-result__preview :deep(h1),
html.dark .ai-result__preview :deep(h2),
html.dark .ai-result__preview :deep(h3),
html.dark .ai-result__preview :deep(h4) {
  color: #f1f5f9;
}

.ai-result__preview :deep(p) {
  margin: 0.6em 0;
}

.ai-result__preview :deep(ul),
.ai-result__preview :deep(ol) {
  padding-left: 1.5em;
  margin: 0.6em 0;
}

.ai-result__preview :deep(li) {
  margin: 0.3em 0;
}

.ai-result__preview :deep(blockquote) {
  border-left: 3px solid #cbd5e1;
  padding-left: 1em;
  margin: 0.8em 0;
  color: #64748b;
}

html.dark .ai-result__preview :deep(blockquote) {
  border-left-color: #475569;
  color: #94a3b8;
}

.ai-result__preview :deep(code) {
  background-color: #f1f5f9;
  padding: 0.15em 0.4em;
  border-radius: 3px;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Consolas', monospace;
}

html.dark .ai-result__preview :deep(code) {
  background-color: #374151;
}

.ai-result__preview :deep(pre) {
  margin: 0.8em 0;
  border-radius: 6px;
  overflow-x: auto;
}

.ai-result__preview :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 0.85em;
  line-height: 1.6;
}

.ai-result__preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.8em 0;
  font-size: 0.9em;
}

.ai-result__preview :deep(th),
.ai-result__preview :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 6px 12px;
  text-align: left;
}

html.dark .ai-result__preview :deep(th),
html.dark .ai-result__preview :deep(td) {
  border-color: #374151;
}

.ai-result__preview :deep(th) {
  background-color: #f8fafc;
  font-weight: 600;
}

html.dark .ai-result__preview :deep(th) {
  background-color: #1f2937;
}

.ai-result__preview :deep(a) {
  color: #3b82f6;
  text-decoration: none;
}

.ai-result__preview :deep(a:hover) {
  text-decoration: underline;
}

.ai-result__preview :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}

.ai-result__preview :deep(hr) {
  border: none;
  border-top: 1px solid #e2e8f0;
  margin: 1.2em 0;
}

html.dark .ai-result__preview :deep(hr) {
  border-top-color: #374151;
}

/* 深色模式代码高亮覆盖 */
html.dark .ai-result__preview :deep(.hljs) {
  background-color: #0d1117 !important;
  color: #c9d1d9 !important;
}

html.dark .ai-result__preview :deep(.hljs-keyword),
html.dark .ai-result__preview :deep(.hljs-selector-tag),
html.dark .ai-result__preview :deep(.hljs-built_in) {
  color: #ff7b72 !important;
}

html.dark .ai-result__preview :deep(.hljs-string),
html.dark .ai-result__preview :deep(.hljs-attr) {
  color: #a5d6ff !important;
}

html.dark .ai-result__preview :deep(.hljs-comment) {
  color: #8b949e !important;
}

html.dark .ai-result__preview :deep(.hljs-number),
html.dark .ai-result__preview :deep(.hljs-literal) {
  color: #79c0ff !important;
}

html.dark .ai-result__preview :deep(.hljs-title),
html.dark .ai-result__preview :deep(.hljs-section),
html.dark .ai-result__preview :deep(.hljs-function .hljs-title) {
  color: #d2a8ff !important;
}

html.dark .ai-result__preview :deep(.hljs-variable),
html.dark .ai-result__preview :deep(.hljs-template-variable) {
  color: #ffa657 !important;
}

html.dark .ai-result__preview :deep(.hljs-type),
html.dark .ai-result__preview :deep(.hljs-class .hljs-title) {
  color: #f0883e !important;
}

.ai-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}
</style>
