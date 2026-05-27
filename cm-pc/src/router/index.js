import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

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
        path: 'flow/transfer-out',
        name: 'TransferOut',
        component: () => import('@/views/flow/transfer-out.vue'),
        meta: { title: '配件领用', icon: 'Upload' }
      },
      {
        path: 'flow/transfer-in',
        name: 'TransferIn',
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
        path: 'inventory/total',
        name: 'InventoryTotal',
        component: () => import('@/views/inventory/total.vue'),
        meta: { title: '总库存', icon: 'Box' }
      },
      {
        path: 'flow/trace',
        name: 'FlowTrace',
        component: () => import('@/views/flow/trace.vue'),
        meta: { title: '条码追溯', icon: 'Search' }
      },
      {
        path: 'worker',
        name: 'Worker',
        component: () => import('@/views/worker/index.vue'),
        meta: { title: '师傅管理', icon: 'User' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router