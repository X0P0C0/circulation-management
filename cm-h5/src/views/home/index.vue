<template>
  <div class="home-page">
    <div class="home-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <h2>配件流转管理</h2>
        <p>欢迎回来，{{ userStore.realName || userStore.username }}</p>
      </div>
    </div>

    <div class="home-body">
      <div class="section-title">业务操作</div>
      <van-grid :column-num="2" :gutter="12" class="action-grid">
        <van-grid-item icon="scan" text="配件入库" class="action-item action-item--primary"
          @click="router.push('/scan/inbound')" />
        <van-grid-item icon="upgrade" text="配件领用" class="action-item action-item--success"
          @click="router.push('/scan/transfer-out')" />
        <van-grid-item icon="back-top" text="配件归还" class="action-item action-item--warning"
          @click="router.push('/scan/transfer-in')" />
        <van-grid-item icon="shopping-cart-o" text="配件售卖" class="action-item action-item--danger"
          @click="router.push('/scan/sell')" />
      </van-grid>

      <div class="section-title" style="margin-top: 20px">查询功能</div>
      <van-cell-group inset class="query-group">
        <van-cell title="条码追溯" label="查询配件全流程记录" is-link @click="router.push('/trace')">
          <template #icon><van-icon name="search" class="cell-icon cell-icon--primary" /></template>
        </van-cell>
        <van-cell title="总库存查询" label="查看全部配件库存" is-link @click="router.push('/inventory')">
          <template #icon><van-icon name="orders-o" class="cell-icon cell-icon--success" /></template>
        </van-cell>
        <van-cell title="操作记录" label="查看历史流转记录" is-link @click="router.push('/records')">
          <template #icon><van-icon name="notes-o" class="cell-icon cell-icon--warning" /></template>
        </van-cell>
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f1f5f9;
}

.home-header {
  position: relative;
  padding: 40px 20px 32px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  border-radius: 0 0 24px 24px;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-content h2 {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px;
}

.header-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.home-body {
  padding: 20px 16px;
  margin-top: -12px;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  padding-left: 4px;
}

.action-grid {
  margin-bottom: 4px;
}

.action-item :deep(.van-grid-item__content) {
  border-radius: 12px;
  padding: 20px 12px;
}

.query-group {
  border-radius: 12px;
  overflow: hidden;
}

.cell-icon {
  font-size: 20px;
  margin-right: 8px;
}

.cell-icon--primary { color: #4361ee; }
.cell-icon--success { color: #22c55e; }
.cell-icon--warning { color: #f59e0b; }
</style>