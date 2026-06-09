<template>
  <div class="min-h-screen" :class="isDark ? 'bg-gray-900' : 'bg-gradient-to-br from-gray-50 via-white to-gray-50'">
    <Header />
    
    <main class="pt-24 pb-16">
      <div class="max-w-6xl mx-auto px-4">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div class="lg:col-span-2">
            <n-card title="个人简介" class="mb-8">
              <div class="prose prose-gray max-w-none">
                <p class="leading-relaxed" :class="isDark ? 'text-gray-300' : 'text-gray-600'" v-html="formatText(profileInfo.introduction)"></p>
              </div>
            </n-card>

            <n-card title="工作经历" class="mb-8">
              <div class="space-y-6">
                <div v-for="work in workExperience" :key="work.id" class="p-4 rounded-lg transition-colors work-experience-item" :class="isDark ? 'bg-gray-800 hover:bg-gray-700' : 'bg-gray-50 hover:bg-gray-100'">
                  <div class="flex items-start justify-between mb-2">
                    <div>
                      <h4 class="font-semibold" :class="isDark ? 'text-white' : 'text-gray-800'">{{ work.companyNameShow }}</h4>
                      <p class="text-sm font-medium" :style="{ color: themeConfig.primaryColor }">{{ work.position }}</p>
                    </div>
                    <span class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">{{ work.startDate }} - {{ work.endDate }}</span>
                  </div>
                  <p class="text-sm mb-3" :class="isDark ? 'text-gray-400' : 'text-gray-500'">{{ work.description }}</p>
                  <div class="flex flex-wrap gap-1.5 max-w-full">
                    <n-tag 
                      v-for="(tag, index) in splitTechTags(work.techs)" 
                      :key="index" 
                      size="small" 
                      type="info"
                      class="break-words max-w-full"
                    >
                      {{ tag }}
                    </n-tag>
                  </div>
                </div>
              </div>
            </n-card>

          </div>

          <div>
            <n-card title="专业技能" class="mb-8">
              <div class="flex flex-wrap gap-2">
                <n-tag 
                  v-for="skill in skills" 
                  :key="skill.name" 
                  type="info" 
                  class="px-3 py-1"
                >
                  {{ skill.name }}
                </n-tag>
              </div>
            </n-card>

            <n-card title="联系方式">
              <div class="space-y-4">
                <div v-if="contactInfo" class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor + '20' }">
                    <Mail class="w-5 h-5" :style="{ color: themeConfig.primaryColor }" />
                  </div>
                  <div>
                    <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">邮箱</p>
                    <p class="font-medium" :class="isDark ? 'text-white' : 'text-gray-900'">{{ contactInfo.email }}</p>
                  </div>
                </div>
                <div v-if="contactInfo" class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor + '20' }">
                    <Phone class="w-5 h-5" :style="{ color: themeConfig.primaryColor }" />
                  </div>
                  <div>
                    <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">电话</p>
                    <p class="font-medium" :class="isDark ? 'text-white' : 'text-gray-900'">{{ contactInfo.phone }}</p>
                  </div>
                </div>
                <div v-if="contactInfo" class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg flex items-center justify-center" :style="{ backgroundColor: themeConfig.primaryColor + '20' }">
                    <MapPin class="w-5 h-5" :style="{ color: themeConfig.primaryColor }" />
                  </div>
                  <div>
                    <p class="text-sm" :class="isDark ? 'text-gray-400' : 'text-gray-500'">位置</p>
                    <p class="font-medium" :class="isDark ? 'text-white' : 'text-gray-900'">{{ contactInfo.location }}</p>
                  </div>
                </div>
              </div>
            </n-card>
          </div>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Mail, Phone, MapPin } from '@/icons'
import { NCard, NTag } from 'naive-ui'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import { useTheme } from '@/composables/useTheme'
import { useUserStore } from '@/stores/user'
import { 
  getWorkExperience, getSkills,
  type WorkExperience as WorkExperienceType,
  type SkillItem as SkillItemType
} from '@/api/about'

const { themeConfig, isDark } = useTheme()
const userStore = useUserStore()

const profileInfo = computed(() => userStore.getProfileInfo())
const contactInfo = computed(() => userStore.getContactInfo())

const workExperience = ref<WorkExperienceType[]>([])
const skills = ref<SkillItemType[]>([])

const formatText = (text: string) => {
  return text.replace(/\n/g, '<br/>')
}

const splitTechTags = (techs: string[]): string[] => {
  const result: string[] = []
  techs.forEach(tech => {
    const parts = tech.split(/(?=\d、)/)
    parts.forEach(part => {
      if (part.trim()) {
        result.push(part.trim())
      }
    })
  })
  return result
}

const loadData = async () => {
  try {
    await Promise.all([
      userStore.fetchUserInfo(),
      userStore.fetchUserContacts()
    ])
    
    const [workRes, skillsRes] = await Promise.all([
      getWorkExperience(),
      getSkills()
    ])

    workExperience.value = workRes.data as any
    skills.value = skillsRes.data as any
  } catch (error) {
    console.error('Failed to load about data:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.work-experience-item {
  overflow: hidden;
}

@media (max-width: 640px) {
  .work-experience-item :deep(.n-tag) {
    font-size: 12px;
    padding: 2px 6px;
  }
}
</style>
