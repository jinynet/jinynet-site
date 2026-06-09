import axios from './axios'

// 项目列表视图 - 后端 ProjectList DTO
export interface ProjectList {
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
  published: boolean
  createdAt: string
}

// 项目详情视图 - 后端 ProjectDetail DTO
export interface ProjectDetail extends ProjectList {
  content: string | null
  contribution: string | null
  stacks?: ProjectStack[]
}

// 项目表单 - 后端 ProjectForm DTO
export interface ProjectForm {
  name: string
  slug: string
  description: string | null
  content: string | null
  coverImage: string | null
  projectUrl: string | null
  repoUrl: string | null
  status: 'active' | 'completed' | 'paused'
  startDate: string | undefined
  endDate: string | undefined
  role: string | null
  contribution: string | null
  sortOrder: number
  published: boolean
  stacks: { id: number }[]
}

// 项目查询条件 - 后端 ProjectSpecification DTO
export interface ProjectQuery {
  pageIndex: number
  pageSize: number
  name?: string        // like 模糊搜索
  description?: string // like 模糊搜索
  content?: string     // like 模糊搜索
  status?: string      // eq 精确匹配
  startDate?: string   // ge 大于等于
  endDate?: string     // le 小于等于
  orderBy?: string
}

// 项目技术栈
export interface ProjectStack {
  id: number
  name: string
  category: 'language' | 'framework' | 'database' | 'tools'
  icon: string | null
  color: string | null
  description: string | null
  sortOrder: number
}

// 项目管理 API - 完整路径: /api/admin/projects
export const getProjects = (params: ProjectQuery) => {
  return axios.get('/admin/projects', { params })
}

export const getProjectById = (id: number | string) => {
  return axios.get(`/admin/projects/${id}`)
}

export const createProject = (data: ProjectForm) => {
  return axios.post('/admin/projects', data)
}

export const updateProject = (id: number | string, data: Partial<ProjectForm>) => {
  return axios.put(`/admin/projects/${id}`, data)
}

export const deleteProject = (id: number | string) => {
  return axios.delete(`/admin/projects/${id}`)
}

// 技术栈管理 API - 完整路径: /api/admin/project-stacks
export const getProjectStacks = () => {
  return axios.get('/admin/project-stacks')
}

export const createProjectStack = (data: { name: string; category: string; icon?: string; color?: string }) => {
  return axios.post('/admin/project-stacks', data)
}

export const updateProjectStack = (id: number, data: Partial<ProjectStack>) => {
  return axios.put(`/admin/project-stacks/${id}`, data)
}

export const deleteProjectStack = (id: number) => {
  return axios.delete(`/admin/project-stacks/${id}`)
}
