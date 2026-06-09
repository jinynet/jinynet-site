<template>
  <div class="space-y-6">
    <n-tabs v-model:value="activeTab" type="line" >
      <n-tab-pane name="info" tab="基本信息">
        <InfoForm />
      </n-tab-pane>

      <n-tab-pane name="skills" tab="技能管理">
        <SkillList />
      </n-tab-pane>

      <n-tab-pane name="contacts" tab="联系方式">
        <ContactList />
      </n-tab-pane>

      <n-tab-pane name="education" tab="教育经历">
        <EducationList />
      </n-tab-pane>

      <n-tab-pane name="work" tab="工作经验">
        <WorkList />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane } from 'naive-ui' 
import InfoForm from './components/InfoForm.vue'
import SkillList from './components/SkillList.vue'
import ContactList from './components/ContactList.vue'
import EducationList from './components/EducationList.vue'
import WorkList from './components/WorkList.vue'

const route = useRoute()
const router = useRouter()

const validTabs = ['info', 'skills', 'contacts', 'education', 'work']
const defaultTab = 'info'

const activeTab = ref(
  validTabs.includes((route.query.tab as string) || '') 
    ? (route.query.tab as string) 
    : defaultTab
)

watch(activeTab, (newTab) => {
  if (newTab === defaultTab) {
    router.push({ query: {} })
  } else {
    router.push({ query: { tab: newTab } })
  }
})
</script>
