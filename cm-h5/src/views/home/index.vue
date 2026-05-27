<template>
  <div class="home-page">
    <!-- 顶部区域 -->
    <div class="home-header">
      <div class="header-content">
        <h2>配件流转管理</h2>
        <p>欢迎回来，{{ userStore.realName || userStore.username }}</p>
      </div>
    </div>

    <div class="home-body">
      <!-- 业务操作 -->
      <div class="section">
        <div class="section-title">业务操作</div>
        <div class="grid-2">
          <div class="menu-card menu-card--primary" @click="router.push('/scan/inbound')">
            <van-icon name="scan" size="28" />
            <span>配件入库</span>
          </div>
          <div class="menu-card menu-card--success" @click="router.push('/scan/transfer-out')">
            <van-icon name="upgrade" size="28" />
            <span>配件领用</span>
          </div>
          <div class="menu-card menu-card--warning" @click="router.push('/scan/transfer-in')">
            <van-icon name="back-top" size="28" />
            <span>配件归还</span>
          </div>
          <div class="menu-card menu-card--danger" @click="router.push('/scan/sell')">
            <van-icon name="shopping-cart-o" size="28" />
            <span>配件售卖</span>
          </div>
        </div>
      </div>

      <!-- 查询功能 -->
      <div class="section">
        <div class="section-title">查询功能</div>
        <div class="query-list">
          <div class="query-item" @click="router.push('/trace')">
            <van-icon name="search" size="20" class="qi-icon qi-icon--primary" />
            <div class="qi-text">
              <div class="qi-title">条码追溯</div>
              <div class="qi-desc">查询配件全流程记录</div>
            </div>
            <van-icon name="arrow" size="16" color="#c8c9cc" />
          </div>
          <div class="query-item" @click="router.push('/inventory')">
            <van-icon name="orders-o" size="20" class="qi-icon qi-icon--success" />
            <div class="qi-text">
              <div class="qi-title">总库存查询</div>
              <div class="qi-desc">查看全部配件库存</div>
            </div>
            <van-icon name="arrow" size="16" color="#c8c9cc" />
          </div>
          <div class="query-item" @click="router.push('/records')">
            <van-icon name="notes-o" size="20" class="qi-icon qi-icon--warning" />
            <div class="qi-text">
              <div class="qi-title">操作记录</div>
              <div class="qi-desc">查看历史流转记录</div>
            </div>
            <van-icon name="arrow" size="16" color="#c8c9cc" />
          </div>
        </div>
      </div>
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
  background: #f5f7fa;
}

/* ---- 顶部 ---- */
.home-header {
  background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%);
  padding: 36px 20px 44px;
  border-radius: 0 0 24px 24px;
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

/* ---- 内容区 ---- */
.home-body {
  padding: 0 16px 24px;
  margin-top: -20px;
  position: relative;
}

.section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  padding-left: 2px;
}

/* ---- 2列网格 ---- */
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.menu-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
  cursor: pointer;

  &:active {
    transform: scale(0.97);
  }
}

.menu-card--primary { color: #4361ee; }
.menu-card--success { color: #22c55e; }
.menu-card--warning { color: #f59e0b; }
.menu-card--danger  { color: #ef4444; }

/* ---- 查询列表 ---- */
.query-list {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.query-item {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
  cursor: pointer;
  transition: background 0.15s;

  &:active {
    background: #f8fafc;
  }

  & + .query-item {
    border-top: 1px solid #f1f5f9;
  }
}

.qi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.qi-icon--primary { background: #eef1ff; color: #4361ee; }
.qi-icon--success { background: #ecfdf5; color: #22c55e; }
.qi-icon--warning { background: #fffbeb; color: #f59e0b; }

.qi-text {
  flex: 1;
}

.qi-title {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 2px;
}

.qi-desc {
  font-size: 12px;
  color: #94a3b8;
}
</style>