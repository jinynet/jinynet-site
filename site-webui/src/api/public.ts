import axios from './axios'

// 首页文章列表视图 - 后端 ArticleList DTO（已发布）
export interface PostedArticleList {
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
  categoryName?: string
  tags?: string[]
}

// 首页文章详情视图 - 后端 ArticleDetail DTO
export interface PostedArticleDetail extends PostedArticleList {
  content: string
}

// 首页项目列表视图
export interface PostedProjectList {
  id: number
  name: string
  slug: string
  description: string | null
  coverImage: string | null
  projectUrl: string | null
  repoUrl: string | null
  status: 'active' | 'completed' | 'paused'
  startDate: string | null
  endDate: string | null
  role: string | null
  sortOrder: number
  createdAt: string
  techStacks?: string[]
}

// 首页用户信息视图
export interface UserInfoPublic {
  id: number
  name: string
  nickname: string | null
  avatar: string | null
  title: string | null
  email: string | null
  location: string | null
  summary: string | null
  bio: string | null
}

// 首页技能视图
export interface UserSkillPublic {
  id: number
  name: string
  category: string
  level: number
}

// 首页用户联系方式视图
export interface UserContactPublic {
  id: number
  contactType: string
  contactValue: string
  displayName: string | null
  icon: string | null
}

// 文章查询参数
export interface PostedArticleQuery {
  pageIndex: number
  pageSize: number
  title?: string
  categoryId?: number
  tagId?: number
}

// 已发布文章 API - 完整路径: /api/articles
export const getPostedArticles = (params: PostedArticleQuery) => {
  return axios.get('/articles', { params })
}

export const getPostedArticleBySlug = (slug: string) => {
  return axios.get(`/articles/${slug}`)
}

export const getPostedArticleById = (id: string | number) => {
  return axios.get(`/articles/id/${id}`)
}

export const getPostedArticleCategories = () => {
  return axios.get('/articles/categories')
}

export const getPostedArticleTags = () => {
  return axios.get('/articles/tags')
}

export const getLatestArticles = (limit = 3) => {
  return axios.get('/articles/latest', { params: { limit } })
}

export const getHotArticles = (limit = 5) => {
  return axios.get('/articles/hot', { params: { limit } })
}

// 已发布项目 API - 完整路径: /api/projects
export const getPostedProjects = () => {
  return axios.get('/projects')
}

export const getPostedProjectBySlug = (slug: string) => {
  return axios.get(`/projects/${slug}`)
}

// 首页用户信息 API - 完整路径: /api/user
export const getPublicUserInfo = () => {
  return axios.get('/user/info')
}

export const getPublicUserSkills = () => {
  return axios.get('/user/skills')
}

export const getPublicUserContacts = () => {
  return axios.get('/user/contacts')
}
