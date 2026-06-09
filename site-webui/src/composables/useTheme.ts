import { ref, computed, watch } from 'vue'
import { darkTheme, lightTheme, type GlobalThemeOverrides } from 'naive-ui'
import { getPublicSettings } from '@/api/settings'

export interface ThemeConfig {
  themeMode: 'light' | 'dark' | 'system'
  primaryColor: string
  primaryColorHover: string
  primaryColorPressed: string
  accentColor: string
  successColor: string
  successColorHover: string
  successColorPressed: string
  warningColor: string
  warningColorHover: string
  warningColorPressed: string
  errorColor: string
  errorColorHover: string
  errorColorPressed: string
  infoColor: string
  infoColorHover: string
  infoColorPressed: string
  fontFamily: string
  fontSize: 'sm' | 'md' | 'lg'
  layoutMode: 'boxed' | 'full-width'
  animationEnabled: boolean
  borderRadius: 'sm' | 'md' | 'lg'
}

const defaultTheme: ThemeConfig = {
  themeMode: 'light',
  primaryColor: '#27272A',
  primaryColorHover: '#3F3F46',
  primaryColorPressed: '#18181B',
  accentColor: '#52525B',
  successColor: '#059669',
  successColorHover: '#10B981',
  successColorPressed: '#047857',
  warningColor: '#B45309',
  warningColorHover: '#D97706',
  warningColorPressed: '#92400E',
  errorColor: '#B91C1C',
  errorColorHover: '#DC2626',
  errorColorPressed: '#991B1B',
  infoColor: '#3B82F6',
  infoColorHover: '#60A5FA',
  infoColorPressed: '#2563EB',
  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  fontSize: 'md',
  layoutMode: 'full-width',
  animationEnabled: true,
  borderRadius: 'md'
}

const themeConfig = ref<ThemeConfig>({ ...defaultTheme })

export function useTheme() {
  const isDark = computed(() => {
    const mode = themeConfig.value.themeMode
    if (mode === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return mode === 'dark'
  })

  const currentTheme = computed(() => {
    const mode = themeConfig.value.themeMode
    if (mode === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? darkTheme : lightTheme
    }
    return mode === 'dark' ? darkTheme : lightTheme
  })

  const borderRadiusValue = computed(() => {
    const map = { sm: '4px', md: '8px', lg: '12px' }
    return map[themeConfig.value.borderRadius]
  })

  /**
   * 计算颜色的 sRGB 相对亮度（W3C 标准公式）
   * 值域 0（最暗）~ 1（最亮）
   */
  function getLuminance(hex: string): number {
    const r = parseInt(hex.slice(1, 3), 16) / 255
    const g = parseInt(hex.slice(3, 5), 16) / 255
    const b = parseInt(hex.slice(5, 7), 16) / 255
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
  }

  /**
   * 深色模式下若颜色过暗（亮度 < 0.25），在 HSL 空间中仅提升亮度，保留色相和饱和度
   * 避免 primaryColor 用作背景/填充色时在深色底色上"消失"
   */
  function getDarkSafeColor(hex: string): string {
    const luminance = getLuminance(hex)
    // 亮度足够则无需调整
    if (luminance >= 0.25) return hex

    const r = parseInt(hex.slice(1, 3), 16)
    const g = parseInt(hex.slice(3, 5), 16)
    const b = parseInt(hex.slice(5, 7), 16)

    // RGB → HSL
    const rNorm = r / 255, gNorm = g / 255, bNorm = b / 255
    const max = Math.max(rNorm, gNorm, bNorm)
    const min = Math.min(rNorm, gNorm, bNorm)
    const delta = max - min
    const l = (max + min) / 2
    let h = 0
    let s = 0
    if (delta !== 0) {
      s = l > 0.5 ? delta / (2 - max - min) : delta / (max + min)
      if (max === rNorm)      h = ((gNorm - bNorm) / delta + (gNorm < bNorm ? 6 : 0)) * 60
      else if (max === gNorm) h = ((bNorm - rNorm) / delta + 2) * 60
      else                    h = ((rNorm - gNorm) / delta + 4) * 60
    }

    // 仅提升亮度：原始亮度 [0, 0.25) → HSL L [0.35, 0.45]
    const ratio = luminance / 0.25
    const targetL = 0.35 + ratio * 0.10

    // HSL → RGB
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1 / 6) return p + (q - p) * 6 * t
      if (t < 1 / 2) return q
      if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6
      return p
    }
    const q = targetL < 0.5 ? targetL * (1 + s) : targetL + s - targetL * s
    const p = 2 * targetL - q
    const nr = Math.round(hue2rgb(p, q, h / 360 + 1 / 3) * 255)
    const ng = Math.round(hue2rgb(p, q, h / 360) * 255)
    const nb = Math.round(hue2rgb(p, q, h / 360 - 1 / 3) * 255)

    return `#${nr.toString(16).padStart(2, '0')}${ng.toString(16).padStart(2, '0')}${nb.toString(16).padStart(2, '0')}`
  }

  const themeOverrides = computed<GlobalThemeOverrides>(() => {
    const { 
      primaryColor, primaryColorHover, primaryColorPressed,
      accentColor,
      successColor, successColorHover, successColorPressed,
      warningColor, warningColorHover, warningColorPressed,
      errorColor, errorColorHover, errorColorPressed,
      infoColor, infoColorHover, infoColorPressed,
      fontFamily, fontSize 
    } = themeConfig.value

    const borderRadius = borderRadiusValue.value
    const borderRadiusSmall = borderRadius === '4px' ? '2px' : borderRadius === '8px' ? '4px' : '6px'

    // 深色模式下，若用户主色过暗则推导可见的安全色用于背景填充场景
    const darkSafePrimary = isDark.value ? getDarkSafeColor(primaryColor) : primaryColor
    const darkSafePrimaryHover = isDark.value ? getDarkSafeColor(primaryColorHover) : primaryColorHover
    const darkSafePrimaryPressed = isDark.value ? getDarkSafeColor(primaryColorPressed) : primaryColorPressed

    return {
      common: {
        primaryColor: darkSafePrimary,
        primaryColorHover: darkSafePrimaryHover,
        primaryColorPressed: darkSafePrimaryPressed,
        primaryColorSuppl: accentColor,
        successColor,
        successColorHover,
        successColorPressed,
        warningColor,
        warningColorHover,
        warningColorPressed,
        errorColor,
        errorColorHover,
        errorColorPressed,
        infoColor,
        infoColorHover,
        infoColorPressed,
        fontFamily,
        fontSize: getFontSize(fontSize),
        borderRadius,
        borderRadiusSmall
      },
      Card: {
        borderRadius,
        borderRadiusSmall
      },
      Button: {
        borderRadius,
        textColor: isDark.value ? '#e5e7eb' : '#1f2937',
        textColorHover: isDark.value ? '#ffffff' : '#111827',
        textColorPressed: isDark.value ? '#d1d5db' : '#374151',
        borderColor: isDark.value ? '#4b5563' : '#d1d5db',
        borderColorHover: isDark.value ? '#6b7280' : '#9ca3af',
        borderColorPressed: isDark.value ? '#374151' : '#b3b8c0',
        colorPrimary: darkSafePrimary,
        colorPrimaryHover: darkSafePrimaryHover,
        colorPrimaryPressed: darkSafePrimaryPressed,
        textColorPrimary: '#ffffff'
      },
      Tag: {
        borderRadius: borderRadiusSmall,
        textColor: isDark.value ? '#e5e7eb' : '#374151',
        color: isDark.value ? '#4b5563' : '#e5e7eb',
        border: isDark.value ? '#6b7280' : '#d1d5db',
        textColorHover: isDark.value ? '#ffffff' : '#111827',
        colorHover: isDark.value ? '#5b6575' : '#d1d5db',
        borderHover: isDark.value ? '#8b95a5' : '#9ca3af',
        colorPrimary: darkSafePrimary,
      },
      Input: {
        borderRadius,
        textColor: isDark.value ? '#e5e7eb' : '#1f2937',
        placeholderTextColor: isDark.value ? '#6b7280' : '#9ca3af',
        borderColor: isDark.value ? '#4b5563' : '#d1d5db',
        borderColorHover: isDark.value ? '#6b7280' : '#9ca3af',
        borderColorFocus: primaryColor,
        backgroundColor: isDark.value ? '#1f2937' : '#ffffff',
        caretColor: primaryColor
      },
      Select: {
        borderRadius,
        textColor: isDark.value ? '#e5e7eb' : '#1f2937',
        borderColor: isDark.value ? '#4b5563' : '#d1d5db',
        borderColorHover: isDark.value ? '#6b7280' : '#9ca3af',
        borderColorFocus: primaryColor,
        backgroundColor: isDark.value ? '#1f2937' : '#ffffff',
        optionTextColor: isDark.value ? '#e5e7eb' : '#1f2937',
        optionTextColorHover: '#ffffff',
        optionBackgroundColorHover: darkSafePrimary,
        menuBackgroundColor: isDark.value ? '#1f2937' : '#ffffff',
        menuBorderColor: isDark.value ? '#4b5563' : '#d1d5db'
      },
      Radio: {
        colorPrimary: primaryColor,
        colorHoverPrimary: primaryColorHover,
        colorPressedPrimary: primaryColorPressed,
        textColor: isDark.value ? '#e5e7eb' : '#1f2937',
        textColorDisabled: isDark.value ? '#4b5563' : '#9ca3af'
      },
      Switch: {
        colorPrimary: primaryColor,
        colorHoverPrimary: primaryColorHover,
        colorPressedPrimary: primaryColorPressed
      },
      Table: {
        borderColor: isDark.value ? '#374151' : '#e5e7eb',
        headerTextColor: isDark.value ? '#f3f4f6' : '#1f2937',
        headerBackgroundColor: isDark.value ? '#1f2937' : '#f9fafb',
        rowBackgroundColor: isDark.value ? '#111827' : '#ffffff',
        rowBackgroundColorHover: isDark.value ? '#374151' : '#f3f4f6',
        textColor: isDark.value ? '#d1d5db' : '#374151'
      },
      Tabs: {
        colorPrimary: primaryColor,
        textColor: isDark.value ? '#e5e7eb' : '#6b7280',
        textColorActive: isDark.value ? '#ffffff' : '#1f2937',
        borderColor: isDark.value ? '#374151' : '#e5e7eb',
        tabBackgroundColor: isDark.value ? '#111827' : '#ffffff',
        tabBackgroundColorActive: isDark.value ? '#1f2937' : '#ffffff'
      },
      Dialog: {
        borderRadius,
        titleTextColor: isDark.value ? '#ffffff' : '#1f2937',
        contentTextColor: isDark.value ? '#d1d5db' : '#374151',
        backgroundColor: isDark.value ? '#1f2937' : '#ffffff',
        borderColor: isDark.value ? '#374151' : '#e5e7eb'
      },
      Tooltip: {
        textColor: '#ffffff',
        backgroundColor: isDark.value ? '#374151' : '#1f2937',
        borderRadius: borderRadiusSmall
      },
      Spin: {
        color: darkSafePrimary
      },
      Avatar: {
        textColor: '#ffffff',
        color: darkSafePrimary,
        colorHover: darkSafePrimaryHover
      },
      Progress: {
        colorPrimary: darkSafePrimary
      },
      Badge: {
        color: darkSafePrimary,
        textColor: '#ffffff',
        borderRadius: '9999px'
      },
      Slider: {
        color: primaryColor,
        colorHover: primaryColorHover,
        colorPressed: primaryColorPressed,
        railColor: isDark.value ? '#374151' : '#e5e7eb'
      },
      Divider: {
        color: isDark.value ? '#374151' : '#e5e7eb'
      },
      Dropdown: {
        menuBackgroundColor: isDark.value ? '#1f2937' : '#ffffff',
        menuBorderColor: isDark.value ? '#374151' : '#e5e7eb',
        optionTextColor: isDark.value ? '#e5e7eb' : '#1f2937',
        optionTextColorHover: '#ffffff',
        optionBackgroundColorHover: darkSafePrimary
      },
      Alert: {
        borderRadius,
        titleTextColor: isDark.value ? '#ffffff' : '#1f2937',
        contentTextColor: isDark.value ? '#d1d5db' : '#374151'
      },
      Timeline: {
        colorPrimary: primaryColor,
        lineColor: isDark.value ? '#374151' : '#e5e7eb',
        textColor: isDark.value ? '#d1d5db' : '#374151'
      }
    }
  })

  const applyTheme = (config: Partial<ThemeConfig>) => {
    Object.assign(themeConfig.value, config)

    // 持久化主题模式到 localStorage
    if (config.themeMode) {
      localStorage.setItem('theme-mode', config.themeMode)
    }

    const root = document.documentElement

    root.style.setProperty('--primary-color', themeConfig.value.primaryColor)
    root.style.setProperty('--primary-color-hover', themeConfig.value.primaryColorHover)
    root.style.setProperty('--primary-color-pressed', themeConfig.value.primaryColorPressed)
    root.style.setProperty('--accent-color', themeConfig.value.accentColor)
    root.style.setProperty('--success-color', themeConfig.value.successColor)
    root.style.setProperty('--success-color-hover', themeConfig.value.successColorHover)
    root.style.setProperty('--success-color-pressed', themeConfig.value.successColorPressed)
    root.style.setProperty('--warning-color', themeConfig.value.warningColor)
    root.style.setProperty('--warning-color-hover', themeConfig.value.warningColorHover)
    root.style.setProperty('--warning-color-pressed', themeConfig.value.warningColorPressed)
    root.style.setProperty('--error-color', themeConfig.value.errorColor)
    root.style.setProperty('--error-color-hover', themeConfig.value.errorColorHover)
    root.style.setProperty('--error-color-pressed', themeConfig.value.errorColorPressed)
    root.style.setProperty('--info-color', themeConfig.value.infoColor)
    root.style.setProperty('--info-color-hover', themeConfig.value.infoColorHover)
    root.style.setProperty('--info-color-pressed', themeConfig.value.infoColorPressed)
    root.style.setProperty('--font-family', themeConfig.value.fontFamily)
    root.style.setProperty('--font-size', getFontSize(themeConfig.value.fontSize))
    root.style.setProperty('--border-radius', borderRadiusValue.value)
    root.style.setProperty('--border-radius-small', borderRadiusValue.value === '4px' ? '2px' : borderRadiusValue.value === '8px' ? '4px' : '6px')

    root.classList.toggle('dark', isDark.value)
    root.classList.toggle('theme-system', themeConfig.value.themeMode === 'system')
    root.classList.toggle('layout-boxed', themeConfig.value.layoutMode === 'boxed')
    root.classList.toggle('layout-full-width', themeConfig.value.layoutMode === 'full-width')
    root.classList.toggle('animation-enabled', themeConfig.value.animationEnabled)
  }

  const resetTheme = () => {
    applyTheme(defaultTheme)
  }

  const applyPresetTheme = (preset: 'enterprise' | 'businessBlue' | 'medical' | 'deepPurple' | 'naturalGreen' | 'minimal') => {
    const presets: Record<string, Partial<ThemeConfig>> = {
      // 企业经典蓝：活力专业蓝 + 紫色点缀
      enterprise: {
        primaryColor: '#2563EB',
        primaryColorHover: '#3B82F6',
        primaryColorPressed: '#1D4ED8',
        accentColor: '#7C3AED',
        successColor: '#059669',
        successColorHover: '#10B981',
        successColorPressed: '#047857',
        warningColor: '#D97706',
        warningColorHover: '#F59E0B',
        warningColorPressed: '#B45309',
        errorColor: '#DC2626',
        errorColorHover: '#EF4444',
        errorColorPressed: '#B91C1C',
        infoColor: '#6366F1',
        infoColorHover: '#818CF8',
        infoColorPressed: '#4F46E5'
      },
      // 商务高级蓝：沉稳深海蓝 + 青蓝点缀
      businessBlue: {
        primaryColor: '#1E40AF',
        primaryColorHover: '#2563EB',
        primaryColorPressed: '#1E3A8A',
        accentColor: '#0891B2',
        successColor: '#059669',
        successColorHover: '#10B981',
        successColorPressed: '#047857',
        warningColor: '#D97706',
        warningColorHover: '#F59E0B',
        warningColorPressed: '#B45309',
        errorColor: '#DC2626',
        errorColorHover: '#EF4444',
        errorColorPressed: '#B91C1C',
        infoColor: '#3B82F6',
        infoColorHover: '#60A5FA',
        infoColorPressed: '#2563EB'
      },
      // 医疗合规绿：清新翡翠绿 + 青绿色点缀
      medical: {
        primaryColor: '#059669',
        primaryColorHover: '#10B981',
        primaryColorPressed: '#047857',
        accentColor: '#0D9488',
        successColor: '#34D399',
        successColorHover: '#6EE7B7',
        successColorPressed: '#10B981',
        warningColor: '#F59E0B',
        warningColorHover: '#FBBF24',
        warningColorPressed: '#D97706',
        errorColor: '#EF4444',
        errorColorHover: '#F87171',
        errorColorPressed: '#DC2626',
        infoColor: '#06B6D4',
        infoColorHover: '#22D3EE',
        infoColorPressed: '#0891B2'
      },
      // 深邃紫色：优雅紫罗兰 + 粉色点缀
      deepPurple: {
        primaryColor: '#7C3AED',
        primaryColorHover: '#8B5CF6',
        primaryColorPressed: '#6D28D9',
        accentColor: '#EC4899',
        successColor: '#059669',
        successColorHover: '#10B981',
        successColorPressed: '#047857',
        warningColor: '#D97706',
        warningColorHover: '#F59E0B',
        warningColorPressed: '#B45309',
        errorColor: '#DC2626',
        errorColorHover: '#EF4444',
        errorColorPressed: '#B91C1C',
        infoColor: '#A78BFA',
        infoColorHover: '#C4B5FD',
        infoColorPressed: '#8B5CF6'
      },
      // 自然草绿：森林绿 + 暖金色点缀，深色模式下可读
      naturalGreen: {
        primaryColor: '#4D7C0F',
        primaryColorHover: '#65A30D',
        primaryColorPressed: '#3F6212',
        accentColor: '#B45309',
        successColor: '#15803D',
        successColorHover: '#16A34A',
        successColorPressed: '#166534',
        warningColor: '#EA580C',
        warningColorHover: '#F97316',
        warningColorPressed: '#C2410C',
        errorColor: '#DC2626',
        errorColorHover: '#EF4444',
        errorColorPressed: '#B91C1C',
        infoColor: '#0E7490',
        infoColorHover: '#0891B2',
        infoColorPressed: '#155E75'
      },
      // 简约黑白（默认）：极简锌灰色系 + 低饱和功能色
      minimal: {
        primaryColor: '#27272A',
        primaryColorHover: '#3F3F46',
        primaryColorPressed: '#18181B',
        accentColor: '#52525B',
        successColor: '#059669',
        successColorHover: '#10B981',
        successColorPressed: '#047857',
        warningColor: '#B45309',
        warningColorHover: '#D97706',
        warningColorPressed: '#92400E',
        errorColor: '#B91C1C',
        errorColorHover: '#DC2626',
        errorColorPressed: '#991B1B',
        infoColor: '#3B82F6',
        infoColorHover: '#60A5FA',
        infoColorPressed: '#2563EB'
      }
    }
    applyTheme(presets[preset])
  }

  const loadThemeConfig = async (prefetchedData?: any) => {
    try {
      const response = prefetchedData || await getPublicSettings()
      const data = response.data || response
      if (data?.theme) {
        const themeSettings = data.theme

        // 检查是否有用户手动设置的主题模式
        const savedThemeMode = localStorage.getItem('theme-mode') as 'light' | 'dark' | 'system' | null

        applyTheme({
          // 如果用户手动设置过主题模式，优先使用用户的设置，否则使用后台配置
          themeMode: savedThemeMode || themeSettings.theme_mode || defaultTheme.themeMode,
          primaryColor: themeSettings.primary_color || defaultTheme.primaryColor,
          primaryColorHover: themeSettings.primary_color_hover || defaultTheme.primaryColorHover,
          primaryColorPressed: themeSettings.primary_color_pressed || defaultTheme.primaryColorPressed,
          accentColor: themeSettings.accent_color || defaultTheme.accentColor,
          successColor: themeSettings.success_color || defaultTheme.successColor,
          successColorHover: themeSettings.success_color_hover || defaultTheme.successColorHover,
          successColorPressed: themeSettings.success_color_pressed || defaultTheme.successColorPressed,
          warningColor: themeSettings.warning_color || defaultTheme.warningColor,
          warningColorHover: themeSettings.warning_color_hover || defaultTheme.warningColorHover,
          warningColorPressed: themeSettings.warning_color_pressed || defaultTheme.warningColorPressed,
          errorColor: themeSettings.error_color || defaultTheme.errorColor,
          errorColorHover: themeSettings.error_color_hover || defaultTheme.errorColorHover,
          errorColorPressed: themeSettings.error_color_pressed || defaultTheme.errorColorPressed,
          infoColor: themeSettings.info_color || defaultTheme.infoColor,
          infoColorHover: themeSettings.info_color_hover || defaultTheme.infoColorHover,
          infoColorPressed: themeSettings.info_color_pressed || defaultTheme.infoColorPressed,
          fontFamily: themeSettings.font_family || defaultTheme.fontFamily,
          fontSize: (themeSettings.font_size as 'sm' | 'md' | 'lg') || defaultTheme.fontSize,
          layoutMode: (themeSettings.layout_mode as 'boxed' | 'full-width') || defaultTheme.layoutMode,
          animationEnabled: themeSettings.animation_enabled !== undefined ? themeSettings.animation_enabled === 'true' : defaultTheme.animationEnabled,
          borderRadius: (themeSettings.border_radius as 'sm' | 'md' | 'lg') || defaultTheme.borderRadius
        })
      }
    } catch (error) {
      console.error('加载主题配置失败:', error)
    }
  }

  const initTheme = () => {
    // 从 localStorage 读取主题模式
    const savedThemeMode = localStorage.getItem('theme-mode') as 'light' | 'dark' | 'system' | null
    if (savedThemeMode && ['light', 'dark', 'system'].includes(savedThemeMode)) {
      themeConfig.value.themeMode = savedThemeMode
    }

    applyTheme(themeConfig.value)

    if (themeConfig.value.themeMode === 'system') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      const handleSystemThemeChange = () => {
        applyTheme(themeConfig.value)
      }
      mediaQuery.addEventListener('change', handleSystemThemeChange)
      return () => {
        mediaQuery.removeEventListener('change', handleSystemThemeChange)
      }
    }
    return () => {}
  }

  watch(isDark, (newVal) => {
    document.documentElement.classList.toggle('dark', newVal)
  })

  const toggleTheme = () => {
    const modes: ('light' | 'dark' | 'system')[] = ['light', 'dark', 'system']
    const currentIndex = modes.indexOf(themeConfig.value.themeMode)
    const nextIndex = (currentIndex + 1) % modes.length
    applyTheme({ themeMode: modes[nextIndex] })
  }

  const setThemeMode = (mode: 'light' | 'dark' | 'system') => {
    applyTheme({ themeMode: mode })
  }

  return {
    themeConfig,
    currentTheme,
    themeOverrides,
    isDark,
    applyTheme,
    resetTheme,
    applyPresetTheme,
    loadThemeConfig,
    initTheme,
    defaultTheme,
    toggleTheme,
    setThemeMode
  }
}

function getFontSize(size: 'sm' | 'md' | 'lg'): string {
  const fontSizeMap = { sm: '14px', md: '16px', lg: '18px' }
  return fontSizeMap[size]
}