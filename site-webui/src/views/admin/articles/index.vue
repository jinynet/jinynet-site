<template>
  <div class="space-y-6">
    <n-tabs v-model:value="activeTab" type="line">
      <n-tab-pane name="articles" tab="文章管理">
        <ArticleList />
      </n-tab-pane>

      <n-tab-pane name="categories" tab="分类管理">
        <CategoryList />
      </n-tab-pane>

      <n-tab-pane name="tags" tab="标签管理">
        <TagList />
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane } from 'naive-ui'
import ArticleList from './components/ArticleList.vue'
import CategoryList from './components/CategoryList.vue'
import TagList from './components/TagList.vue'

const route = useRoute()
const router = useRouter()

const validTabs = ['articles', 'categories', 'tags']
const defaultTab = 'articles'

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
