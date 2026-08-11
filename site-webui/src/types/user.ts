/**
 * 用户相关类型定义
 */

/**
 * 用户联系方式类型
 */
export type ContactType =
  | 'email'
  | 'phone'
  | 'github'
  | 'linkedin'
  | 'wechat'
  | 'website'
  | 'other'

/**
 * 用户信息 - 管理端完整视图 (后端 UserInfo DTO)
 */
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
  /** 前端运行时字段，后端不返回 */
  online?: boolean
}

/**
 * 用户信息 - 前端公开视图
 */
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

/**
 * 用户技能 - 管理端
 */
export interface UserSkills {
  id: number
  name: string
  category: 'frontend' | 'backend' | 'database' | 'tools' | 'other'
  level: number
  description: string | null
  sortOrder: number
}

/**
 * 用户技能 - 前端公开视图
 */
export interface UserSkillPublic {
  id: number
  name: string
  category: string
  level: number
}

/**
 * 用户联系方式 - 管理端
 */
export interface UserContact {
  id: number
  contactType: ContactType | string
  contactValue: string
  displayName: string | null
  icon: string | null
  sortOrder: number
}

/**
 * 用户联系方式 - 前端公开视图
 */
export interface UserContactPublic {
  id: number
  contactType: string
  contactValue: string
  displayName: string | null
  icon: string | null
}

/**
 * 教育经历 (后端 UserEducation DTO)
 */
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

/**
 * 工作经验 (后端 UserWork DTO)
 */
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

/**
 * 前端展示用的个人资料聚合信息
 */
export interface ProfileInfo {
  name: string
  title: string
  bio: string
  stats: { label: string; value: string }[]
  introduction: string
}

/**
 * 联系方式聚合信息
 */
export interface ContactInfo {
  email: string
  phone: string
  location: string
}
