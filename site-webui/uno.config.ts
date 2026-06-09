import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons()
  ],
  shortcuts: {
    'bg-primary': 'bg-gray-900',
    'text-primary': 'text-gray-900',
    'text-dark': 'text-gray-800',
    'hover:underline': 'hover:decoration-gray-900 hover:underline-offset-4'
  }
})
