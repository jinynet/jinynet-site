<template>
  <div class="space-y-1">
    <div class="flex items-center justify-between text-sm">
      <span :class="themeMode === 'dark' ? 'text-gray-300' : 'text-gray-700'" class="font-medium">{{ skill.name }}</span>
      <span :class="themeMode === 'dark' ? 'text-gray-500' : 'text-gray-500'">{{ skill.level }}%</span>
    </div>
    <div class="h-2 rounded-full overflow-hidden" :class="themeMode === 'dark' ? 'bg-gray-700' : 'bg-gray-200'">
      <div
        class="h-full rounded-full transition-all duration-500"
        :style="{ width: Math.min(skill.level, 100) + '%', backgroundColor: themeConfig.primaryColor }"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { ref, onMounted } from 'vue'

const { themeConfig } = useTheme()
const themeMode = ref('light')

interface Skill {
  id: number
  name: string
  category: string
  level: number
}

defineProps<{
  skill: Skill
}>()

onMounted(() => {
  themeMode.value = themeConfig.value.themeMode
})
</script>
