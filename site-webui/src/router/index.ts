import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/frontend/Home.vue')
  },
  {
    path: '/articles',
    name: 'ArticleList',
    component: () => import('@/views/frontend/ArticleList.vue')
  },
  {
    path: '/articles/:slug',
    name: 'ArticleDetail',
    component: () => import('@/views/frontend/ArticleDetail.vue')
  },
  {
    path: '/articles/id/:id',
    name: 'ArticleDetailById',
    component: () => import('@/views/frontend/ArticleDetail.vue')
  },
  {
    path: '/videos',
    name: 'VideoList',
    component: () => import('@/views/frontend/VideoList.vue')
  },
  {
    path: '/videos/:id',
    name: 'VideoDetail',
    component: () => import('@/views/frontend/VideoDetail.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/frontend/Search.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/frontend/About.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin/articles/preview/:id',
    name: 'AdminArticlePreview',
    component: () => import('@/views/frontend/ArticleDetail.vue'),
    meta: { requiresAuth: true, adminPreview: true }
  },
  {
    path: '/admin',
    component: () => import('@/components/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'articles',
        name: 'AdminArticleList',
        component: () => import('@/views/admin/articles/index.vue')
      },
      {
        path: 'articles/add',
        name: 'AdminArticleAdd',
        component: () => import('@/views/admin/articles/components/ArticleForm.vue')
      },
      {
        path: 'articles/edit/:id',
        name: 'AdminArticleEdit',
        component: () => import('@/views/admin/articles/components/ArticleForm.vue')
      },
      {
        path: 'files',
        name: 'AdminFileList',
        component: () => import('@/views/admin/files/index.vue')
      },
      {
        path: 'projects',
        name: 'AdminProjectList',
        component: () => import('@/views/admin/projects/index.vue')
      },
      {
        path: 'projects/add',
        name: 'AdminProjectAdd',
        component: () => import('@/views/admin/projects/components/ProjectForm.vue')
      },
      {
        path: 'projects/edit/:id(\\d+)',
        name: 'AdminProjectEdit',
        component: () => import('@/views/admin/projects/components/ProjectForm.vue'),
        props: (route) => ({ id: route.params.id as string })
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/profile/index.vue')
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue')
      }
    ]
  },
  // 404 页面 - 必须放在最后
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/frontend/NotFound.vue')
  }]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from) => {
  const authStore = useAuthStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (requiresAuth && !authStore.isAuthenticated()) {
    return {
      path: '/admin/login',
      query: { redirect: to.fullPath }
    }
  }
  
  if (to.path === '/admin/login' && authStore.isAuthenticated()) {
    return { path: '/admin' }
  }
  
  return true
})

export default router
