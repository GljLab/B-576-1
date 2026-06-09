import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/Projects.vue'),
        meta: { title: '项目管理', icon: 'Folder' }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue'),
        meta: { title: '项目详情', hidden: true }
      },
      {
        path: 'candidates',
        name: 'Candidates',
        component: () => import('@/views/Candidates.vue'),
        meta: { title: '考生管理', icon: 'User' }
      },
      {
        path: 'examiners',
        name: 'Examiners',
        component: () => import('@/views/Examiners.vue'),
        meta: { title: '考官管理', icon: 'Avatar' }
      },
      {
        path: 'rooms',
        name: 'Rooms',
        component: () => import('@/views/Rooms.vue'),
        meta: { title: '考场管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'draw',
        name: 'Draw',
        component: () => import('@/views/Draw.vue'),
        meta: { title: '抽签管理', icon: 'Ticket' }
      },
      {
        path: 'scoring',
        name: 'Scoring',
        component: () => import('@/views/Scoring.vue'),
        meta: { title: '现场评分', icon: 'Edit' }
      },
      {
        path: 'results',
        name: 'Results',
        component: () => import('@/views/Results.vue'),
        meta: { title: '成绩查询', icon: 'Document' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧面试评分系统` : '智慧面试评分系统'
})

export default router
