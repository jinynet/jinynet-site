import { defineAsyncComponent } from 'vue'
import type { Component } from 'vue'

/**
 * 联系方式/社交平台图标白名单
 * 
 * 仅按需懒加载这些图标，避免全量导入 10557 个 @vicons/material 图标。
 * 若后端 icon 字段不在白名单中，该联系方式的图标将不显示。
 * 
 * 如需新增图标，先在 node_modules/@vicons/material/es/ 确认文件存在，然后在此添加即可。
 */
const ALLOWED_ICONS = new Set([
  // === 基础联系方式 ===
  'EmailOutlined',
  'AlternateEmailOutlined',
  'ContactMailOutlined',
  'PhoneOutlined',
  'ContactPhoneOutlined',
  'LocalPhoneOutlined',
  'LocationOnOutlined',
  'MapOutlined',
  'LanguageOutlined',
  'PublicOutlined',
  'LinkOutlined',

  // === 通讯 ===
  'ChatOutlined',
  'ForumOutlined',

  // === 社交平台（仅 @vicons/material 中存在的） ===
  'DiscordFilled', 'DiscordOutlined',
  'FacebookFilled', 'FacebookOutlined',
  'RedditFilled', 'RedditOutlined',
  'SnapchatFilled', 'SnapchatOutlined',
  'TelegramFilled', 'TelegramOutlined',
  'TiktokFilled', 'TiktokOutlined',
  'WechatFilled', 'WechatOutlined',
  'WhatsappFilled', 'WhatsappOutlined',
  'GiteSharp',
  // === 通用 UI 图标 ===
  'PersonOutlined',
  'HomeOutlined',
  'ShareOutlined',
  'MoreHorizOutlined',

])

const iconCache = new Map<string, Component>()

/**
 * 按需懒加载 @vicons/material 图标（仅限白名单）
 * @param name 图标组件名，如 EmailOutlined
 * @returns 异步组件，若不在白名单或加载失败则返回 null
 */
export function loadMaterialIcon(name: string): Component | null {
  if (!name || !ALLOWED_ICONS.has(name)) {
    if (name) console.warn(`[iconLoader] 未在白名单中的图标: ${name}`)
    return null
  }

  const cached = iconCache.get(name)
  if (cached) return cached

  const loader = buildLoader(name)
  if (!loader) return null

  const comp = defineAsyncComponent({
    loader,
    loadingComponent: { render: () => null },
    delay: 0,
  })

  iconCache.set(name, comp)
  return comp
}

/** 静态 import() 映射 —— Vite 可静态分析，每个图标编译为独立 chunk */
function buildLoader(name: string): (() => Promise<{ default: Component }>) | null {
  switch (name) {
    case 'EmailOutlined':         return () => import('@vicons/material/es/EmailOutlined.js')
    case 'AlternateEmailOutlined': return () => import('@vicons/material/es/AlternateEmailOutlined.js')
    case 'ContactMailOutlined':   return () => import('@vicons/material/es/ContactMailOutlined.js')
    case 'PhoneOutlined':         return () => import('@vicons/material/es/PhoneOutlined.js')
    case 'ContactPhoneOutlined':  return () => import('@vicons/material/es/ContactPhoneOutlined.js')
    case 'LocationOnOutlined':    return () => import('@vicons/material/es/LocationOnOutlined.js')
    case 'MapOutlined':           return () => import('@vicons/material/es/MapOutlined.js')
    case 'LanguageOutlined':      return () => import('@vicons/material/es/LanguageOutlined.js')
    case 'PublicOutlined':        return () => import('@vicons/material/es/PublicOutlined.js')
    case 'LinkOutlined':          return () => import('@vicons/material/es/LinkOutlined.js')
    case 'ChatOutlined':          return () => import('@vicons/material/es/ChatOutlined.js')
    case 'ForumOutlined':         return () => import('@vicons/material/es/ForumOutlined.js')
    case 'DiscordFilled':         return () => import('@vicons/material/es/DiscordFilled.js')
    case 'DiscordOutlined':       return () => import('@vicons/material/es/DiscordOutlined.js')
    case 'FacebookFilled':        return () => import('@vicons/material/es/FacebookFilled.js')
    case 'FacebookOutlined':      return () => import('@vicons/material/es/FacebookOutlined.js')
    case 'RedditFilled':          return () => import('@vicons/material/es/RedditFilled.js')
    case 'RedditOutlined':        return () => import('@vicons/material/es/RedditOutlined.js')
    case 'SnapchatFilled':        return () => import('@vicons/material/es/SnapchatFilled.js')
    case 'SnapchatOutlined':      return () => import('@vicons/material/es/SnapchatOutlined.js')
    case 'TelegramFilled':        return () => import('@vicons/material/es/TelegramFilled.js')
    case 'TelegramOutlined':      return () => import('@vicons/material/es/TelegramOutlined.js')
    case 'TiktokFilled':          return () => import('@vicons/material/es/TiktokFilled.js')
    case 'TiktokOutlined':        return () => import('@vicons/material/es/TiktokOutlined.js')
    case 'WechatFilled':          return () => import('@vicons/material/es/WechatFilled.js')
    case 'WechatOutlined':        return () => import('@vicons/material/es/WechatOutlined.js')
    case 'WhatsappFilled':        return () => import('@vicons/material/es/WhatsappFilled.js')
    case 'WhatsappOutlined':      return () => import('@vicons/material/es/WhatsappOutlined.js')
    case 'PersonOutlined':        return () => import('@vicons/material/es/PersonOutlined.js')
    case 'HomeOutlined':          return () => import('@vicons/material/es/HomeOutlined.js')
    case 'ShareOutlined':         return () => import('@vicons/material/es/ShareOutlined.js')
    case 'MoreHorizOutlined':     return () => import('@vicons/material/es/MoreHorizOutlined.js')
    case 'LocalPhoneOutlined':    return () => import('@vicons/material/es/LocalPhoneOutlined.js')
    case 'GiteSharp':             return () => import('@vicons/material/es/GiteSharp.js')
    default: return null
  }
}
