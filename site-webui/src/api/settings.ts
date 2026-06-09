import axios from './axios'

export interface SiteSettings {
  title: string
  description: string
  keywords: string[]
  logo?: string
  favicon?: string
  icp?: string
  securityRecord?: string
  copyright?: string
}

export interface ThemeSettings {
  themeMode: 'light' | 'dark' | 'system'
  primaryColor: string
  accentColor: string
  fontFamily: string
  fontSize: 'sm' | 'md' | 'lg'
  layoutMode: 'boxed' | 'full-width'
  animationEnabled: boolean
}

export interface PublicSettings {
  site_title?: string
  site_description?: string
  site_keywords?: string
  site_logo?: string
  site_favicon?: string
  site_icp?: string
  site_security_record?: string
  site_copyright?: string
}

export const getSettings = () => {
  return axios.get('/admin/settings')
}

export const updateSettings = (data: SiteSettings) => {
  return axios.put('/admin/settings', data)
}

export const getPublicSettings = () => {
  return axios.get('/settings/public')
}
