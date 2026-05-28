import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页概览', icon: 'HomeFilled' }
      },
      {
        path: 'accessory/inbound',
        name: 'AccessoryInbound',
        component: () => import('@/views/accessory/inbound.vue'),
        meta: { title: '配件入库', icon: 'Download' }
      },
      {
        path: 'flow/outbound',
        name: 'FlowOutbound',
        component: () => import('@/views/flow/transfer-out.vue'),
        meta: { title: '配件出库', icon: 'Upload' }
      },
      {
        path: 'flow/return',
        name: 'FlowReturn',
        component: () => import('@/views/flow/transfer-in.vue'),
        meta: { title: '配件归还', icon: 'RefreshLeft' }
      },
      {
        path: 'flow/sell',
        name: 'FlowSell',
        component: () => import('@/views/flow/sell.vue'),
        meta: { title: '配件售卖', icon: 'ShoppingCart' }
      },
      {
        path: 'flow/transfer',
        name: 'FlowTransfer',
        component: () => import('@/views/flow/transfer.vue'),
        meta: { title: '库存转移', icon: 'Sort' }
      },
      {
        path: 'inventory/master',
        name: 'InventoryMaster',
        component: () => import('@/views/inventory/master.vue'),
        meta: { title: '总库', icon: 'Box' }
      },
      {
        path: 'inventory/available',
        name: 'InventoryAvailable',
        component: () => import('@/views/inventory/available.vue'),
        meta: { title: '可支配库存', icon: 'Goods' }
      },
      {
        path: 'flow/trace',
        name: 'FlowTrace',
        component: () => import('@/views/flow/trace.vue'),
        meta: { title: '工件追溯', icon: 'Search' }
      },
      {
        path: 'worker',
        name: 'Worker',
        component: () => import('@/views/worker/index.vue'),
        meta: { title: '师傅管理', icon: 'User' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', adminOnly: true }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Menu', adminOnly: true }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document', adminOnly: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let hasFetchedUserInfo = false

router.beforeEach(async (to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    if (token) next('/')
    else next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  if (!hasFetchedUserInfo) {
    const userStore = useUserStore()
    await userStore.fetchUserInfo()
    hasFetchedUserInfo = true
  }
  if (to.meta?.adminOnly) {
    const userStore = useUserStore()
    if (userStore.role !== 1) {
      next('/dashboard')
      return
    }
  }
  next()
})

export default router