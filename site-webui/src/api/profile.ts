import axios from './axios'

// 个人信息 - 后端 UserInfo DTO
export interface UserInfo {
  id: number
  name: string
  nickname: string | null
  avatar: string | null
  title: string | null
  email: string | null
  phone: string | null
  location: string | null
  summary: string | null
  bio: string | null
}

// 技能 - 后端 UserSkills DTO
export interface UserSkills {
  id: number
  name: string
  category: 'frontend' | 'backend' | 'database' | 'tools' | 'other'
  level: number
  description: string | null
  sortOrder: number
}

// 联系方式 - 后端 UserContact DTO
export interface UserContact {
  id: number
  contactType: 'email' | 'phone' | 'github' | 'linkedin' | 'wechat' | 'website' | 'other'
  contactValue: string
  displayName: string | null
  icon: string | null
  sortOrder: number
}

// 教育经历 - 后端 UserEducation DTO
export interface UserEducation {
  id: number
  schoolName: string
  major: string | null
  degree: 'bachelor' | 'master' | 'doctor' | 'other' | null
  startDate: string
  endDate: string | null
  description: string | null
  sortOrder: number
}

// 工作经验 - 后端 UserWork DTO
export interface UserWork {
  id: number
  companyName: string
  position: string
  startDate: string
  endDate: string | null
  description: string | null
  achievements: string | null
  sortOrder: number
}

// 个人信息 API - 完整路径: /api/admin/user/info
export const getUserInfo = () => {
  return axios.get('/admin/user/info')
}

export const updateUserInfo = (data: Partial<UserInfo>) => {
  return axios.put('/admin/user/info', data)
}

// 技能管理 API - 完整路径: /api/admin/user/skills
export const getSkills = () => {
  return axios.get('/admin/user/skills')
}

export const createSkill = (data: Omit<UserSkills, 'id'>) => {
  return axios.post('/admin/user/skills', data)
}

export const updateSkill = (data: Partial<UserSkills>) => {
  return axios.put('/admin/user/skills', data)
}

export const deleteSkill = (id: number) => {
  return axios.delete(`/admin/user/skills/${id}`)
}

// 联系方式 API - 完整路径: /api/admin/user/contacts
export const getContacts = () => {
  return axios.get('/admin/user/contacts')
}

export const createContact = (data: Omit<UserContact, 'id'>) => {
  return axios.post('/admin/user/contacts', data)
}

export const updateContact = (data: Partial<UserContact>) => {
  return axios.put('/admin/user/contacts', data)
}

export const deleteContact = (id: number) => {
  return axios.delete(`/admin/user/contacts/${id}`)
}

// 教育经历 API - 完整路径: /api/admin/user/educations
export const getEducations = () => {
  return axios.get('/admin/user/educations')
}

export const createEducation = (data: Omit<UserEducation, 'id'>) => {
  return axios.post('/admin/user/educations', data)
}

export const updateEducation = (data: Partial<UserEducation>) => {
  return axios.put('/admin/user/educations', data)
}

export const deleteEducation = (id: number) => {
  return axios.delete(`/admin/user/educations/${id}`)
}

// 工作经验 API - 完整路径: /api/admin/user/works
export const getWorkExperiences = () => {
  return axios.get('/admin/user/works')
}

export const createWorkExperience = (data: Omit<UserWork, 'id'>) => {
  return axios.post('/admin/user/works', data)
}

export const updateWorkExperience = (data: Partial<UserWork>) => {
  return axios.put('/admin/user/works', data)
}

export const deleteWorkExperience = (id: number) => {
  return axios.delete(`/admin/user/works/${id}`)
}
