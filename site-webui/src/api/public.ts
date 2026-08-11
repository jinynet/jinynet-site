import axios from './axios'
import type {
  PostedArticleListItem,
  PostedArticleDetail,
  PostedProjectListItem,
  UserInfoPublic,
  UserSkillPublic,
  UserContactPublic,
  PostedArticleQuery,
} from '@/types'

// 类型重新导出（向后兼容），实际定义见 @/types
export type {
  PostedArticleListItem,
  PostedArticleDetail,
  PostedProjectListItem,
  UserInfoPublic,
  UserSkillPublic,
  UserContactPublic,
  PostedArticleQuery,
}

/** @deprecated 请使用 PostedArticleListItem */
export type PostedArticleList = PostedArticleListItem
/** @deprecated 请使用 PostedProjectListItem */
export type PostedProjectList = PostedProjectListItem

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
