import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/api/axios'

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
  online?: boolean
}

export interface ProfileInfo {
  name: string
  title: string
  bio: string
  stats: { label: string; value: string }[]
  introduction: string
}

export interface ContactInfo {
  email: string
  phone: string
  location: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const userContacts = ref<Array<{ contactType: string; contactValue: string }>>([])
  const isLoading = ref(false)
  const hasLoaded = ref(false)

  const fetchUserInfo = async (force = false): Promise<UserInfo | null> => {
    if (hasLoaded.value && !force && userInfo.value) {
      return userInfo.value
    }

    isLoading.value = true
    try {
      const response = await axios.get('/user/info')
      userInfo.value = response.data || null
      hasLoaded.value = true
      return userInfo.value
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    } finally {
      isLoading.value = false
    }
  }

  const fetchUserContacts = async (force = false): Promise<typeof userContacts.value> => {
    if (hasLoaded.value && !force && userContacts.value.length > 0) {
      return userContacts.value
    }

    try {
      const response = await axios.get('/user/contacts')
      userContacts.value = response.data || []
      return userContacts.value
    } catch (error) {
      console.error('获取用户联系方式失败:', error)
      return []
    }
  }

  const getProfileInfo = (): ProfileInfo => {
    const info = userInfo.value
    return {
      name: (info?.nickname || info?.name || '技术博主') as string,
      title: info?.title || '全栈开发者 / 技术博主',
      bio: info?.bio || '热爱技术，热爱分享，专注于Web开发和技术创新',
      introduction: info?.summary || '大家好！我是一名全栈开发者，拥有多年的软件开发经验。\n\n' +
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
  }

  const getContactInfo = (): ContactInfo | null => {
    if (!userInfo.value) {
      return null
    }
    const info = userInfo.value
    const phoneContact = userContacts.value.find(c => c.contactType === 'phone')
    
    return {
      email: info.email || '',
      phone: phoneContact?.contactValue || info.phone || '',
      location: info.location || ''
    }
  }

  const clearUserInfo = () => {
    userInfo.value = null
    userContacts.value = []
    hasLoaded.value = false
  }

  return {
    userInfo,
    userContacts,
    isLoading,
    hasLoaded,
    fetchUserInfo,
    fetchUserContacts,
    getProfileInfo,
    getContactInfo,
    clearUserInfo
  }
})
