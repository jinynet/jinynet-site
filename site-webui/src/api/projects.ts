import axios from './axios'
import type {
  ProjectListItem,
  ProjectDetail,
  ProjectForm,
  ProjectQuery,
  ProjectStack,
} from '@/types'

// 类型重新导出（向后兼容），实际定义见 @/types
export type {
  ProjectListItem,
  ProjectDetail,
  ProjectForm,
  ProjectQuery,
  ProjectStack,
}

/** @deprecated 请使用 ProjectListItem */
export type ProjectList = ProjectListItem

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
