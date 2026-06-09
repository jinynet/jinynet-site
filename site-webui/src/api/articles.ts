import axios from './axios'

// 文章列表视图 - 后端 ArticleList DTO
export interface ArticleList {
  id: number
  title: string
  slug: string
  excerpt: string | null
  coverImage: string | null
  status: 'draft' | 'published' | 'private'
  viewCount: number
  likeCount: number
  publishedAt: string | null
  updatedAt: string
}

// 文章详情视图 - 后端 ArticleDetail DTO
export interface ArticleDetail extends ArticleList {
  content: string
}

// 分类对象
export interface CategoryInput {
  id?: number
  name: string
  slug: string
  description: string | null
  sortOrder: number | 0
}

// 标签对象
export interface TagInput {
  id?: number
  name: string
  slug: string
  description: string | null
  sortOrder: number | 0
}

// 文章表单 - 后端 ArticleForm DTO
export interface ArticleForm {
  title: string
  slug: string
  content: string
  excerpt: string | null
  coverImage: string | null
  status: 'draft' | 'published' | 'private'
  category?: CategoryInput | null
  tags?: TagInput[]
}

// 文章查询条件 - 后端 ArticleSpecification DTO
export interface ArticleQuery {
  pageIndex: number
  pageSize: number
  title?: string           // like 模糊搜索
  excerpt?: string         // like 模糊搜索
  content?: string         // like 模糊搜索
  status?: string          // eq 精确匹配
  publishedAtStart?: string // ge 大于等于
  publishedAtEnd?: string   // le 小于等于
  orderBy?: string         // 排序字段，如 "updatedAt desc"
}

// 文章分类
export interface ArticleCategory {
  id: number
  name: string
  slug: string
  description: string | null
  sortOrder: number
}

// 文章标签
export interface ArticleTag {
  id: number
  name: string
  slug: string
  color: string | null
  description: string | null
  sortOrder: number
}

// 文章管理 API - 完整路径: /api/admin/articles
export const getArticles = (params: ArticleQuery) => {
  return axios.get('/admin/articles', { params })
}

export const getArticleById = (id: string | number) => {
  return axios.get(`/admin/articles/${id}`)
}

export const createArticle = (data: ArticleForm) => {
  return axios.post('/admin/articles', data)
}

export const updateArticle = (id: string | number, data: Partial<ArticleForm>) => {
  return axios.put(`/admin/articles/${id}`, data)
}

export const publishArticle = (data: ArticleForm) => {
  return axios.post('/admin/articles/publish', data)
}

export const updateAndPublishArticle = (id: string | number, data: Partial<ArticleForm>) => {
  return axios.put(`/admin/articles/${id}/publish`, data)
}

export const deleteArticle = (id: string | number) => {
  return axios.delete(`/admin/articles/${id}`)
}

// 分类管理 API - 完整路径: /api/admin/categories
export const getCategories = () => {
  return axios.get('/admin/articles/categories')
}

export const createCategory = (data: { name: string; slug?: string; description?: string; sortOrder?: number }) => {
  return axios.post('/admin/articles/categories', data)
}

export const updateCategory = (id: number, data: Partial<ArticleCategory>) => {
  return axios.put(`/admin/articles/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return axios.delete(`/admin/articles/categories/${id}`)
}

// 标签管理 API - 完整路径: /api/admin/tags
export const getTags = () => {
  return axios.get('/admin/articles/tags')
}

export const createTag = (data: { name: string; slug?: string; color?: string; description?: string; sortOrder?: number }) => {
  return axios.post('/admin/articles/tags', data)
}

export const updateTag = (id: number, data: Partial<ArticleTag>) => {
  return axios.put(`/admin/articles/tags/${id}`, data)
}

export const deleteTag = (id: number) => {
  return axios.delete(`/admin/articles/tags/${id}`)
}
