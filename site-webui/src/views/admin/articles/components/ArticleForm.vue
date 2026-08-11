<template>
  <div class="article-form">
    <div class="sticky top-10 lg:top-6 z-20 bg-white dark:bg-gray-800 py-3 sm:py-4 -mx-4 sm:-mx-6 px-4 sm:px-6 border-b border-gray-200 dark:border-gray-700 mb-4 sm:mb-6">
      <div class="flex items-center justify-between">
        <h1 class="text-lg sm:text-xl font-bold text-gray-900 dark:text-gray-100">{{ isEdit ? '编辑文章' : '创建文章' }}</h1>
        <div class="flex items-center gap-2">
          <n-button size="small" sm-size="medium" @click="showAiAssistant = true">
            <template #icon>
              <n-icon :component="Robot" />
            </template>
            AI 帮写
          </n-button>
          <n-button size="small" sm-size="medium" @click="handleSaveDraft" :loading="isSavingDraft">
            保存草稿
          </n-button>
          <n-button v-if="isEdit" size="small" sm-size="medium" @click="handlePreview">
            预览
          </n-button>
          <n-button size="small" sm-size="medium" type="primary" @click="handlePublish" :loading="isSubmitting">
            {{ isEdit ? (form.isPublished ? '更新并发布' : '发布文章') : '发布文章' }}
          </n-button>
        </div>
      </div>
    </div>

    <div class="article-form__body">
      <div class="article-form__sidebar">
        <n-card title="基本信息" :bordered="false" class="article-form__meta-card">
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
            <n-form-item label="文章标题" path="title">
              <n-input v-model:value="form.title" placeholder="请输入文章标题" />
            </n-form-item>

            <n-form-item label="URL别名" path="slug">
              <n-input v-model:value="form.slug" placeholder="请输入URL别名（可选）" />
            </n-form-item>

            <n-form-item label="文章分类" path="category">
              <n-select
                v-model:value="form.category"
                :options="categoryOptions"
                label-field="name"
                value-field="slug"
                placeholder="请选择分类（可选）"
                clearable
                filterable
                tag
                @create="handleCategoryCreate"
              />
            </n-form-item>

            <n-form-item label="文章标签" path="tags">
              <n-select
                v-model:value="form.tags"
                :options="tagOptions"
                label-field="name"
                value-field="slug"
                placeholder="请选择标签（可选）"
                multiple
                clearable
                filterable
                tag
                @create="handleTagCreate"
              />
            </n-form-item>

            <n-form-item label="文章摘要" path="excerpt">
              <n-input v-model:value="form.excerpt" type="textarea" placeholder="请输入文章摘要（可选）" :rows="3" />
            </n-form-item>

            <n-form-item label="封面图片" path="coverImage">
              <n-input v-model:value="form.coverImage" placeholder="请输入封面图片URL（可选）" />
            </n-form-item>

            <n-form-item label="发布状态">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-500">{{ form.isPublished ? '已发布' : '草稿' }}</span>
                <n-switch v-model="form.isPublished" />
              </div>
            </n-form-item>
          </n-form>
        </n-card>
      </div>

      <div class="article-form__main">
        <n-card :bordered="false" class="article-form__content-card">
          <template #header>
            <div class="article-form__card-header flex items-center gap-2">
              <span class="text-lg font-semibold text-gray-900 dark:text-gray-100">文章内容</span>
              <span class="text-xs text-gray-400 dark:text-gray-500">Markdown 支持</span>
              <span v-if="vditorLoadStatus" class="text-xs text-gray-400 dark:text-gray-500">{{ vditorLoadStatus }}</span>
              <n-spin v-if="isLoading" size="small" />
            </div>
          </template>
          <div class="article-form__card-body">
            <div ref="vditorContainer" class="article-form__editor">
              <div v-if="isLoading && !form.content" class="article-form__skeleton">
                <n-skeleton height="40px" style="margin-bottom: 12px;" />
                <n-skeleton height="20px" style="margin-bottom: 8px;" width="80%" />
                <n-skeleton height="20px" style="margin-bottom: 8px;" width="95%" />
                <n-skeleton height="20px" style="margin-bottom: 8px;" width="70%" />
                <n-skeleton height="20px" style="margin-bottom: 8px;" width="85%" />
                <n-skeleton height="20px" width="60%" />
              </div>
            </div>
          </div>
        </n-card>
      </div>
    </div>

    <!-- AI 帮写助手抽屉 -->
    <AiWritingAssistant
      v-model:visible="showAiAssistant"
      :editor-content="vditorInstance?.getValue() || form.content"
      @insert-content="handleAiInsert"
      @replace-content="handleAiReplace"
      @apply-excerpt="handleAiExcerpt"
      @apply-title="handleAiTitle"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NSelect, NSwitch, NButton, NSkeleton, NSpin, NIcon, useMessage } from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { useDebounceFn } from '@vueuse/core'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { useTheme } from '@/composables/useTheme'
import { getArticleById, createArticle, updateArticle, publishArticle, updateAndPublishArticle, getCategories, getTags, type ArticleForm, type CategoryInput, type TagInput, type ArticleTag } from '@/api/articles'
import { uploadFile, type FileInfo } from '@/api/files'
import { Robot } from '@/icons'
import AiWritingAssistant from './AiWritingAssistant.vue'

const router = useRouter()
const { isDark } = useTheme()
const route = useRoute()
const message = useMessage()

let vditorLoadStartTime = 0
let vditorReadyResolver: (() => void) | null = null

const formRef = ref<FormInst | null>(null)
const vditorContainer = ref<HTMLElement | null>(null)
const isSubmitting = ref(false)
const isSavingDraft = ref(false)
const isLoading = ref(false)
const vditorLoadStatus = ref('')
const showAiAssistant = ref(false)
let vditorInstance: Vditor | null = null
let vditorReady = false
let pendingContent: string | null = null

const vditorReadyPromise = new Promise<void>((resolve) => {
  vditorReadyResolver = resolve
})

const isEdit = computed(() => !!route.params.id)

const form = ref({
  title: '',
  slug: '',
  content: '',
  excerpt: '',
  coverImage: '',
  isPublished: false,
  category: null as string | null,
  tags: [] as string[]
})

const rules = {
  title: {
    required: true,
    message: '请输入文章标题',
    trigger: ['input', 'blur']
  }
}

const categories = ref<CategoryInput[]>([])
const tags = ref<TagInput[]>([])

const categoryOptions = computed(() =>
  categories.value.map(c => ({ label: c.name, value: c.slug, ...c }))
)

const tagOptions = computed(() =>
  tags.value.map(t => ({ label: t.name, value: t.slug, ...t  }))
)

const debouncedInput = useDebounceFn((value: string) => {
  form.value.content = value
}, 150)

const handlePreview = () => {
  router.push(`/admin/articles/preview/${route.params.id}`)
}

const handleCategoryCreate = (name: string) => {
  const option = { label: name, value: name, name: name, slug: name.toLowerCase().replace(/\s+/g, '-'), description: null, sortOrder: 0 }
  return option
}

const handleTagCreate = (name: string) => {
  const option = { label: name, value: name, name: name, slug: name.toLowerCase().replace(/\s+/g, '-'), description: null, sortOrder: 0 }
  return option
}

const initVditor = () => {
  vditorLoadStartTime = performance.now()
  if (!vditorContainer.value || vditorInstance) return

  const container = vditorContainer.value
  if (!container) {
    console.warn('Vditor container is null')
    return
  }

  container.style.minHeight = '500px'

  try {
    vditorLoadStatus.value = '加载中...'
    
    // 使用类型断言避免TS报错
    const vditorConfig: any = {
      height: 700,
      lang: 'zh_CN',
      theme: isDark.value ? 'dark' : 'light',
      mode: 'wysiwyg',  // 使用所见即所得模式（ir模式存在兼容性问题）
      customWysiwygToolbar: () => {},
      cache: { 
        enable: true,   // 启用本地缓存，加速刷新后的加载
        type: 'localStorage',
        id: 'article-editor'  // 必须提供唯一ID
      },
      // 完全禁用预览功能以加速初始化
      preview: {
        enable: false  // 禁用预览面板
      },
      image: {
        isPreview: false, // 禁用图片预览
      },
      upload: {
        fieldName: 'file',
        accept: 'image/*',
        multiple: false,
        validate: (files: File[]) => {
          const file = files[0]
          if (!file) return '请选择图片'
          return file.type.startsWith('image/') ? true : '仅支持上传图片'
        },
        handler: async (files: File[]) => {
          console.log('Vditor upload handler called:', files)
          const file = files[0]
          if (!file) return '请选择图片'

          try {
            const result = await uploadFile(file, undefined, 'article image', true)
            const data = result.data as FileInfo | undefined
            console.log('Upload result:', data)
            if (!data?.url || !vditorInstance) {
              message.error('上传失败，未获取到图片URL')
              return '上传失败'
            }

            const imageName = data.originalFilename || data.filename || file.name
            vditorInstance.insertValue(`![${imageName}](${data.url})`)
            message.success('图片上传成功')
            return null
          } catch (err) {
            console.error('Upload error:', err)
            message.error('上传失败，请重试')
            return '上传失败'
          }
        },
      },
      // 禁用目录生成
      outline: {
        enable: false,
        position: 'right'
      },
      after: () => {
        const loadTime = performance.now() - vditorLoadStartTime
        const displayTime = loadTime >= 1000 
          ? `${(loadTime / 1000).toFixed(1)}s` 
          : `${Math.round(loadTime)}ms`
        console.log(`✅ Vditor 编辑器完全准备好，总耗时 ${loadTime.toFixed(2)}ms`)
        vditorLoadStatus.value = `就绪 (${displayTime})`
        vditorReady = true
        if (vditorReadyResolver) {
          vditorReadyResolver()
          vditorReadyResolver = null
        }
        if (pendingContent && vditorInstance) {
          console.log('📝 设置缓存的编辑器内容')
          try {
            vditorInstance.setValue(pendingContent)
            pendingContent = null
          } catch (e) {
            console.warn('Vditor setValue error:', e)
          }
        }
      },
      input: (value: string) => {
        debouncedInput(value)
      }
    }
    console.log('⏳ 创建Vditor实例...')
    vditorInstance = new Vditor(container, vditorConfig)
    console.log('⏳ Vditor实例已创建，等待完全初始化...')
    vditorLoadStatus.value = '初始化中...'
  } catch (e) {
    console.error('Vditor initialization error:', e)
    vditorLoadStatus.value = '加载失败'
    container.innerHTML = '<div style="padding: 20px; text-align: center; color: #999;">编辑器加载失败，请刷新页面重试</div>'
  }
}

const destroyVditor = () => {
  if (vditorInstance) {
    try {
      vditorInstance.destroy()
    } catch (e) {
      console.warn('Vditor destroy error:', e)
    }
    vditorInstance = null
  }
  vditorReady = false
  pendingContent = null
  vditorReadyResolver = null
}

const loadArticle = async (id: string) => {
  isLoading.value = true
  try {
    const response = await getArticleById(id)
    const data = response.data
    if (data) {
      form.value.title = data.title || ''
      form.value.slug = data.slug || ''
      form.value.content = data.content || ''
      form.value.excerpt = data.excerpt || ''
      form.value.coverImage = data.coverImage || ''
      form.value.isPublished = data.status === 'published'
      form.value.category = data.category?.slug || null
      form.value.tags = data.tags?.map((tag: ArticleTag) => tag.slug) || []
      
      // 记录内容大小，用于性能分析
      const contentSize = new Blob([form.value.content || '']).size
      console.log(`📄 文章内容大小: ${(contentSize / 1024).toFixed(2)} KB`)
      
      // 根据编辑器状态设置内容
      if (vditorReady && vditorInstance && form.value.content) {
        console.log('📝 编辑器已准备好，直接设置内容')
        const setStartTime = performance.now()
        try {
          vditorInstance.setValue(form.value.content)
          console.log(`✅ 内容设置完成，耗时 ${(performance.now() - setStartTime).toFixed(2)}ms`)
        } catch (e) {
          console.warn('Vditor setValue error:', e)
        }
      } else if (form.value.content) {
        console.log('⏳ 编辑器未准备好，缓存内容等待设置')
        pendingContent = form.value.content
      }
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    message.error('加载文章失败')
  } finally {
    isLoading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await getCategories()
    if (response.data) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    const response = await getTags()
    if (response.data) {
      tags.value = response.data
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

onMounted(async () => {
  console.time('总加载时间')
  
  await nextTick()
  
  // 并行加载分类和标签（不等待结果）
  const categoriesPromise = loadCategories()
  const tagsPromise = loadTags()
  
  // 初始化编辑器
  initVditor()
  
  if (isEdit.value) {
    const id = route.params.id as string
    // 文章加载与编辑器就绪并行执行
    await Promise.all([
      vditorReadyPromise,
      loadArticle(id)
    ])
  }
  
  await Promise.all([categoriesPromise, tagsPromise])
  
  console.timeEnd('总加载时间')
})

onUnmounted(() => {
  destroyVditor()
})

const convertCategoryToObject = (category: string | null): CategoryInput | null => {
  if (!category) return null
  const cat = categories.value.find(c => c.slug === category)
  return cat ? cat : { name: category, slug: category, description: null, sortOrder: 0 }
}

const convertTagsToObjects = (selectedTags: string[] | null): TagInput[] => {
  if (!selectedTags) return []
  return selectedTags.map(tag => {
    const t = tags.value.find(t => t.slug === tag)
    return t ? t : { name: tag, slug: tag, description: null, sortOrder: 0 }
  })
}

const handleSaveDraft = async () => {
  const content = vditorInstance?.getValue() || form.value.content

  const articleData: ArticleForm = {
    title: form.value.title.trim() || '未命名草稿',
    slug: form.value.slug.trim() || (form.value.title.trim() ? generateSlug(form.value.title) : ''),
    content: content,
    excerpt: form.value.excerpt.trim() || null,
    coverImage: form.value.coverImage || null,
    status: 'draft',
    category: convertCategoryToObject(form.value.category),
    tags: convertTagsToObjects(form.value.tags)
  }

  try {
    isSavingDraft.value = true
    if (isEdit.value) {
      const id = route.params.id as string
      await updateArticle(id, articleData)
      message.success('草稿保存成功')
    } else {
      const response = await createArticle(articleData)
      message.success('草稿保存成功')
      if (response.data?.id) {
        router.push(`/admin/articles/edit/${response.data.id}`)
        return
      }
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    message.error('保存草稿失败')
  } finally {
    isSavingDraft.value = false
  }
}

const handlePublish = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    message.error('请输入文章标题')
    return
  }

  const content = vditorInstance?.getValue() || form.value.content

  const articleData: ArticleForm = {
    title: form.value.title.trim(),
    slug: form.value.slug.trim() || generateSlug(form.value.title),
    content: content,
    excerpt: form.value.excerpt.trim() || null,
    coverImage: form.value.coverImage || null,
    status: 'published',
    category: convertCategoryToObject(form.value.category),
    tags: convertTagsToObjects(form.value.tags)
  }

  try {
    isSubmitting.value = true
    if (isEdit.value) {
      const id = route.params.id as string
      await updateAndPublishArticle(id, articleData)
      message.success('文章更新并发布成功')
    } else {
      await publishArticle(articleData)
      message.success('文章创建并发布成功')
    }
    router.push('/admin/articles')
  } catch (error) {
    console.error('发布文章失败:', error)
    message.error('文章发布失败')
  } finally {
    isSubmitting.value = false
  }
}

const generateSlug = (title: string) => {
  return title
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
}

// ==================== AI 帮写处理 ====================

// 插入 AI 生成的内容到编辑器
const handleAiInsert = (content: string) => {
  if (vditorInstance) {
    vditorInstance.insertValue(content)
  } else {
    form.value.content += content
  }
}

// 替换编辑器全部内容
const handleAiReplace = (content: string) => {
  if (vditorInstance) {
    vditorInstance.setValue(content)
  }
  form.value.content = content
}

// 应用为摘要
const handleAiExcerpt = (content: string) => {
  form.value.excerpt = content
}

// 应用为标题
const handleAiTitle = (content: string) => {
  form.value.title = content
}
</script>

<style scoped>
.article-form {
  min-height: 100vh;
  background-color: #f8fafc;
}

html.dark .article-form {
  background-color: #111827;
}

.article-form__body {
  display: block;
  padding: 0 2px;
}

@media (min-width: 1024px) {
  .article-form__body {
    display: flex;
    gap: 24px;
    max-width: 1400px;
    margin: 0 auto;
  }
  
  .article-form__sidebar {
    width: 320px;
    flex-shrink: 0;
  }
  
  .article-form__main {
    flex: 1;
    min-width: 0;
  }
}

.article-form__meta-card {
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.article-form__content-card {
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.article-form__card-header {
  padding-bottom: 12px;
  margin-bottom: 4px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

html.dark .article-form__card-header {
  border-bottom-color: #374151;
  color: #e5e7eb;
}

.article-form__editor {
  width: 100%;
  min-height: 600px;
}

.article-form__skeleton {
  padding: 20px;
  min-height: 500px;
}

:deep(.n-form-item) {
  margin-bottom: 16px;
}

:deep(.n-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.n-form-item-label) {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 6px;
}

html.dark :deep(.n-form-item-label) {
  color: #d1d5db;
}

:deep(.n-input),
:deep(.n-select) {
  font-size: 14px;
  border-radius: 8px;
}

:deep(.vditor) {
  border-radius: 8px;
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid #e2e8f0;
}

html.dark :deep(.vditor-toolbar) {
  border-bottom-color: #374151;
}

:deep(.vditor-content) {
  min-height: 500px;
}

@media (max-width: 1023px) {
  .article-form__body {
    display: block;
  }
  
  .article-form__editor {
    min-height: 400px;
  }
  
  :deep(.vditor-content) {
    min-height: 350px;
  }
}

@media (max-width: 640px) {
  .article-form__body {
    padding: 0 4px;
  }
  
  .article-form__editor {
    min-height: 350px;
  }
  
  :deep(.n-card-body) {
    padding: 12px;
  }
  
  :deep(.n-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.n-input),
  :deep(.n-select) {
    font-size: 14px;
  }
  
  :deep(.vditor-content) {
    min-height: 280px;
  }
}
</style>
