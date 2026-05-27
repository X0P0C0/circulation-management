import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { hidden: true } },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页' } },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/inventory/index.vue'), meta: { title: '库存' } },
      { path: 'mine', name: 'Mine', component: () => import('@/views/mine/index.vue'), meta: { title: '我的' } }
    ]
  },
  { path: '/scan/inbound', name: 'ScanInbound', component: () => import('@/views/scan/inbound.vue') },
  { path: '/scan/transfer-out', name: 'ScanTransferOut', component: () => import('@/views/scan/transfer-out.vue') },
  { path: '/scan/transfer-in', name: 'ScanTransferIn', component: () => import('@/views/scan/transfer-in.vue') },
  { path: '/scan/sell', name: 'ScanSell', component: () => import('@/views/scan/sell.vue') },
  { path: '/trace', name: 'Trace', component: () => import('@/views/trace/index.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.path === '/login') return next()
  if (!getToken()) return next('/login')
  next()
})

export default router