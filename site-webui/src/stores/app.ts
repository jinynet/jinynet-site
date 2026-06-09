import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const siteConfig = ref({
    title: '个人技术平台',
    description: '分享技术文章和项目经验',
    logo: '',
    keywords: [],
    favicon: '',
    icp: '',
    securityRecord: '',
    copyright: ''
  })

  const updateSiteConfig = (config: Partial<typeof siteConfig.value>) => {
    siteConfig.value = { ...siteConfig.value, ...config }
  }

  const loadSiteConfig = (config: {
    site_title?: string
    site_description?: string
    site_keywords?: string
    site_logo?: string
    site_favicon?: string
    site_icp?: string
    site_security_record?: string
    site_copyright?: string
  }) => {
    if (config.site_title) siteConfig.value.title = config.site_title
    if (config.site_description) siteConfig.value.description = config.site_description
    if (config.site_keywords) {
      try {
        siteConfig.value.keywords = JSON.parse(config.site_keywords)
      } catch {
        siteConfig.value.keywords = []
      }
    }
    if (config.site_logo) siteConfig.value.logo = config.site_logo
    if (config.site_favicon) siteConfig.value.favicon = config.site_favicon
    if (config.site_icp) siteConfig.value.icp = config.site_icp
    if (config.site_security_record) siteConfig.value.securityRecord = config.site_security_record
    if (config.site_copyright) siteConfig.value.copyright = config.site_copyright
  }

  return {
    siteConfig,
    updateSiteConfig,
    loadSiteConfig
  }
})
