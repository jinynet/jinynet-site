import axios from './axios'

// 统一搜索结果
export interface SearchResult {
  type: 'article' | 'project'
  id: number
  title: string
  content: string | null
  excerpt: string | null
  description: string | null
  score: number
  categoryId: number | null
  categoryName: string | null
  tags: string | null
  stacks: string | null
}

// 全局搜索 API
export const searchAll = (keyword: string, limit: number = 20) => {
  return axios.get('/search', { params: { keyword, limit } })
}

// 文章搜索 API
export const searchArticles = (keyword: string, limit: number = 10) => {
  return axios.get('/articles/search', { params: { keyword, limit } })
}

// 项目搜索 API
export const searchProjects = (keyword: string, limit: number = 10) => {
  return axios.get('/projects/search', { params: { keyword, limit } })
}

// 重建所有索引 API
export const rebuildIndex = () => {
  return axios.post('/search/rebuild-index')
}

// 重建文章索引 API
export const rebuildArticleIndex = () => {
  return axios.post('/search/rebuild-article-index')
}

// 重建项目索引 API
export const rebuildProjectIndex = () => {
  return axios.post('/search/rebuild-project-index')
}