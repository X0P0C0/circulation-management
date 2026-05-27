import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue'), meta: { hidden: true } },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: 'Home' } },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/inventory/index.vue'), meta: { title: 'Inventory' } },
      { path: 'mine', name: 'Mine', component: () => import('@/views/mine/index.vue'), meta: { title: 'My' } }
    ]
  },
  { path: '/scan/inbound', name: 'ScanInbound', component: () => import('@/views/scan/inbound.vue') },
  { path: '/scan/transfer-out', name: 'ScanTransferOut', component: () => import('@/views/scan/transfer-out.vue') },
  { path: '/scan/transfer-in', name: 'ScanTransferIn', component: () => import('@/views/scan/transfer-in.vue') },
  { path: '/scan/sell', name: 'ScanSell', component: () => import('@/views/scan/sell.vue') },
  { path: '/trace', name: 'Trace', component: () => import('@/views/trace/index.vue') },
  { path: '/records', name: 'Records', component: () => import('@/views/records/index.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })

let hasFetchedUserInfo = false

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    if (getToken()) next('/')
    else next()
    return
  }
  if (!getToken()) { next('/login'); return }
  if (!hasFetchedUserInfo) {
    const userStore = useUserStore()
    await userStore.fetchUserInfo()
    hasFetchedUserInfo = true
  }
  next()
})

export default router