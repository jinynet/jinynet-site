import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/frontend/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/articles',
    name: 'ArticleList',
    component: () => import('@/views/frontend/ArticleList.vue'),
    meta: { title: '文章列表' }
  },
  {
    path: '/articles/:slug',
    name: 'ArticleDetail',
    component: () => import('@/views/frontend/ArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/articles/id/:id',
    name: 'ArticleDetailById',
    component: () => import('@/views/frontend/ArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/videos',
    name: 'VideoList',
    component: () => import('@/views/frontend/VideoList.vue'),
    meta: { title: '视频列表' }
  },
  {
    path: '/videos/:id',
    name: 'VideoDetail',
    component: () => import('@/views/frontend/VideoDetail.vue'),
    meta: { title: '视频详情' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/frontend/Search.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/frontend/About.vue'),
    meta: { title: '关于我' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/admin/articles/preview/:id',
    name: 'AdminArticlePreview',
    component: () => import('@/views/frontend/ArticleDetail.vue'),
    meta: { requiresAuth: true, adminPreview: true, title: '文章预览' }
  },
  {
    path: '/admin',
    component: () => import('@/components/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, title: '管理后台' },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'articles',
        name: 'AdminArticleList',
        component: () => import('@/views/admin/articles/index.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'articles/add',
        name: 'AdminArticleAdd',
        component: () => import('@/views/admin/articles/components/ArticleForm.vue'),
        meta: { title: '添加文章' }
      },
      {
        path: 'articles/edit/:id',
        name: 'AdminArticleEdit',
        component: () => import('@/views/admin/articles/components/ArticleForm.vue'),
        meta: { title: '编辑文章' }
      },
      {
        path: 'files',
        name: 'AdminFileList',
        component: () => import('@/views/admin/files/index.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: 'projects',
        name: 'AdminProjectList',
        component: () => import('@/views/admin/projects/index.vue'),
        meta: { title: '项目管理' }
      },
      {
        path: 'projects/add',
        name: 'AdminProjectAdd',
        component: () => import('@/views/admin/projects/components/ProjectForm.vue'),
        meta: { title: '添加项目' }
      },
      {
        path: 'projects/edit/:id(\\d+)',
        name: 'AdminProjectEdit',
        component: () => import('@/views/admin/projects/components/ProjectForm.vue'),
        props: (route) => ({ id: route.params.id as string }),
        meta: { title: '编辑项目' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/profile/index.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  // 404 页面 - 必须放在最后
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/frontend/NotFound.vue'),
    meta: { title: '页面未找到' }
  }]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 浏览器后退/前进时恢复原位置
    if (savedPosition) {
      return savedPosition
    }
    // 带锚点跳转
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    // 不同路由切换时回到顶部；相同路由（仅 query 变化）保持原位
    if (to.path !== from.path) {
      return { top: 0 }
    }
    return false
  }
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

// 统一通过路由 meta.title 设置页面标题
router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) || ''
  // 延迟到 siteConfig 加载后再由 App.vue 的 watch 修正完整标题
  document.title = title || document.title
})

export default router
