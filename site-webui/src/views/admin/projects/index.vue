<template>
  <div class="space-y-6">
    <n-tabs v-model:value="activeTab" type="line" animated @update:value="handleTabChange">
      <n-tab-pane name="projects" tab="项目管理">
        <ProjectList />
      </n-tab-pane>

      <n-tab-pane name="stacks" tab="技术栈管理">
        <StackList />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane } from 'naive-ui' 
import ProjectList from './components/ProjectList.vue'
import StackList from './components/StackList.vue'

const route = useRoute()
const router = useRouter()

const activeTab = ref('projects')

const handleTabChange = (value: string) => {
  router.push({ query: { tab: value } })
}

onMounted(() => {
  const tab = route.query.tab as string
  if (tab && (tab === 'projects' || tab === 'stacks')) {
    activeTab.value = tab
  }
})

watch(() => route.query.tab, (newTab) => {
  if (newTab && (newTab === 'projects' || newTab === 'stacks')) {
    activeTab.value = newTab as string
  }
})
</script>
