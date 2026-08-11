/**
 * 项目相关类型定义
 */

export type ProjectStatus = 'active' | 'completed' | 'paused'

/**
 * 项目技术栈
 */
export interface ProjectStack {
  id: number
  name: string
  category: 'language' | 'framework' | 'database' | 'tools'
  icon: string | null
  color: string | null
  description: string | null
  sortOrder: number
}

/**
 * 项目列表项 - 管理端视图 (后端 ProjectList DTO)
 */
export interface ProjectListItem {
  id: number
  name: string
  slug: string
  description: string | null
  coverImage: string | null
  projectUrl: string | null
  repoUrl: string | null
  status: ProjectStatus
  startDate: string | null
  endDate: string | null
  role: string | null
  sortOrder: number
  published: boolean
  createdAt: string
}

/**
 * 已发布项目列表项 - 前端视图
 */
export interface PostedProjectListItem extends ProjectListItem {
  techStacks?: string[]
  stacks?: ProjectStack[] | null
}

/**
 * 项目详情 (后端 ProjectDetail DTO)
 */
export interface ProjectDetail extends ProjectListItem {
  content: string | null
  contribution: string | null
  stacks?: ProjectStack[]
}

/**
 * 项目表单 (后端 ProjectForm DTO)
 */
export interface ProjectForm {
  name: string
  slug: string
  description: string | null
  content: string | null
  coverImage: string | null
  projectUrl: string | null
  repoUrl: string | null
  status: ProjectStatus
  startDate: string | undefined
  endDate: string | undefined
  role: string | null
  contribution: string | null
  sortOrder: number
  published: boolean
  stacks: { id: number }[]
}

/**
 * 项目查询条件 (后端 ProjectSpecification DTO)
 */
export interface ProjectQuery {
  pageIndex: number
  pageSize: number
  name?: string
  description?: string
  content?: string
  status?: string
  startDate?: string
  endDate?: string
  orderBy?: string
}
