<template>
  <div class="space-y-6">
    <n-tabs v-model:value="activeTab" type="line">
      <n-tab-pane name="files" tab="文件管理">
        <FileList />
      </n-tab-pane>

      <n-tab-pane name="categories" tab="分类管理">
        <CategoryList />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane } from 'naive-ui'
import FileList from './components/FileList.vue'
import CategoryList from './components/CategoryList.vue'

const route = useRoute()
const router = useRouter()

const validTabs = ['files', 'categories']
const defaultTab = 'files'

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