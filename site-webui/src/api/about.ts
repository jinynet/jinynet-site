import axios from './axios'

export interface ProfileInfo {
  name: string
  title: string
  bio: string
  stats: { label: string; value: string }[]
  introduction: string
}

export interface UserInfo {
  id?: number
  name: string
  nickname?: string
  avatar?: string
  title?: string
  email?: string
  phone?: string
  location?: string
  summary?: string
  bio?: string
}

export interface UserEducation {
  id: number
  startYear: string
  endYear?: string
  description: string
  sortOrder: number
}

export interface UserSkills {
  id: number
  name: string
  level: number
  sortOrder: number
}

export interface WorkExperience {
  id: number
  company: string
  companyNameShow?: string
  position: string
  startDate: string
  endDate: string
  description: string
  techs: string[]
}

export interface SkillItem {
  name: string
  level: number
  bgClass: string
  iconClass: string
}

export interface ContactInfo {
  email: string
  phone: string
  location: string
}

export const getProfileInfo = async (): Promise<{ data: ProfileInfo }> => {
  const response = await axios.get('/user/info')
  const userInfo = response.data
  
  const profileInfo: ProfileInfo = {
    name: userInfo?.nickname || userInfo?.name || '技术博主',
    title: userInfo?.title || '全栈开发者 / 技术博主',
    bio: userInfo?.bio || '热爱技术，热爱分享，专注于Web开发和技术创新',
    introduction: userInfo?.summary || '大家好！我是一名全栈开发者，拥有多年的软件开发经验。\n\n' +
      '我热爱编程，喜欢探索新技术，并且乐于分享自己的学习心得和实践经验。\n\n' +
      '在我的个人技术平台上，你可以找到关于前端、后端、DevOps等方面的技术文章和教程。\n\n' +
      '希望这些内容能够帮助到正在学习编程的你！',
    stats: [
      { label: '文章数', value: '120+' },
      { label: '项目数', value: '50+' },
      { label: 'GitHub Stars', value: '2.5k+' },
      { label: '访问量', value: '50k+' }
    ]
  }
  
  return { data: profileInfo }
}

export const getEducation = async (): Promise<{ data: { year: string; description: string }[] }> => {
  const response = await axios.get('/user/educations')
  const educations = response.data
  
  if (educations && educations.length > 0) {
    const items = educations.map((e: UserEducation) => ({
      year: `${e.startYear} - ${e.endYear || '至今'}`,
      description: e.description
    }))
    return { data: items }
  }
  
  return {
    data: [
    ]
  }
}

export interface UserWork {
  id: number
  companyName: string
  companyNameShow?: string
  position: string
  startDate: string
  endDate?: string
  description: string
  achievements?: string
  sortOrder: number
}

export const getWorkExperience = async (): Promise<{ data: WorkExperience[] }> => {
  const response = await axios.get('/user/works')
  const works = response.data

  if (works && works.length > 0) {
    const items = works.map((w: UserWork) => ({
      id: w.id,
      company: w.companyName,
      companyNameShow: w.companyNameShow,
      position: w.position,
      startDate: w.startDate,
      endDate: w.endDate || '至今',
      description: w.description,
      techs: w.achievements ? w.achievements.split(',').map(t => t.trim()) : []
    }))
    return { data: items }
  }

  return {
    data: []
  }
}

export const getSkills = async (): Promise<{ data: SkillItem[] }> => {
  const response = await axios.get('/user/skills')
  const skills = response.data
  
  if (skills && skills.length > 0) {
    const items = skills.map((s: UserSkills) => ({
      name: s.name,
      level: s.level,
      bgClass: 'bg-blue-100',
      iconClass: 'text-blue-600'
    }))
    return { data: items }
  }
  
  return {
    data: [
      { name: '前端开发', level: 5, bgClass: 'bg-blue-100', iconClass: 'text-blue-600' },
      { name: '后端开发', level: 5, bgClass: 'bg-green-100', iconClass: 'text-green-600' },
      { name: '数据库设计', level: 4, bgClass: 'bg-purple-100', iconClass: 'text-purple-600' },
      { name: '系统架构', level: 4, bgClass: 'bg-orange-100', iconClass: 'text-orange-600' },
      { name: 'DevOps', level: 3, bgClass: 'bg-red-100', iconClass: 'text-red-600' },
      { name: 'AI技术', level: 2, bgClass: 'bg-cyan-100', iconClass: 'text-cyan-600' }
    ]
  }
}

export const getTechStack = () => {
  return axios.get('/about/tech-stack')
}

export const getContactInfo = async (): Promise<{ data: ContactInfo }> => {
  const response = await axios.get('/user/info')
  const userInfo = response.data
  
  const contactInfo: ContactInfo = {
    email: userInfo?.email || 'hello@example.com',
    phone: userInfo?.phone || '138-0000-0000',
    location: userInfo?.location || '北京市朝阳区'
  }
  
  return { data: contactInfo }
}
