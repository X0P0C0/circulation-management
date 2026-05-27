<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card stat-card--primary">
        <div class="stat-card__icon">
          <el-icon :size="28"><Box /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__label">配件种类</div>
          <div class="stat-card__value">{{ stats.totalAccessories || 0 }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card__icon">
          <el-icon :size="28"><Menu /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__label">配件分类</div>
          <div class="stat-card__value">{{ stats.totalCategories || 0 }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--warning">
        <div class="stat-card__icon">
          <el-icon :size="28"><Checked /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__label">在库数量</div>
          <div class="stat-card__value">{{ stats.availableQty || 0 }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--danger">
        <div class="stat-card__icon">
          <el-icon :size="28"><Upload /></el-icon>
        </div>
        <div class="stat-card__info">
          <div class="stat-card__label">在外数量</div>
          <div class="stat-card__value">{{ stats.outQty || 0 }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
    </div>

    <!-- 功能区 -->
    <div class="action-grid">
      <el-card class="action-card">
        <template #header>
          <div class="action-card__header">
            <el-icon color="#4361ee"><Operation /></el-icon>
            <span>快捷操作</span>
          </div>
        </template>
        <div class="action-btns">
          <el-button type="primary" size="large" @click="$router.push('/accessory/inbound')">
            <el-icon><Download /></el-icon> 配件入库
          </el-button>
          <el-button type="success" size="large" @click="$router.push('/flow/transfer-out')">
            <el-icon><Upload /></el-icon> 配件领用
          </el-button>
          <el-button type="warning" size="large" @click="$router.push('/flow/transfer-in')">
            <el-icon><RefreshLeft /></el-icon> 配件归还
          </el-button>
          <el-button type="danger" size="large" @click="$router.push('/flow/sell')">
            <el-icon><ShoppingCart /></el-icon> 配件售卖
          </el-button>
        </div>
      </el-card>

      <el-card class="action-card">
        <template #header>
          <div class="action-card__header">
            <el-icon color="#6366f1"><Search /></el-icon>
            <span>快捷查询</span>
          </div>
        </template>
        <div class="action-btns">
          <el-button size="large" @click="$router.push('/flow/trace')">
            <el-icon><Search /></el-icon> 条码追溯
          </el-button>
          <el-button size="large" @click="$router.push('/inventory/total')">
            <el-icon><Box /></el-icon> 总库存
          </el-button>
          <el-button size="large" @click="$router.push('/worker')">
            <el-icon><User /></el-icon> 师傅管理
          </el-button>
          <el-button size="large" @click="$router.push('/log')">
            <el-icon><Document /></el-icon> 操作日志
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInventoryStats } from '@/api/inventory'

const stats = ref({})

onMounted(async () => {
  try {
    const { data } = await getInventoryStats()
    stats.value = data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
}

/* ---- 统计卡片 ---- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}

.stat-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  z-index: 1;
}

.stat-card--primary .stat-card__icon { background: linear-gradient(135deg, #4361ee 0%, #6366f1 100%); box-shadow: 0 4px 12px rgba(67, 97, 238, 0.35); }
.stat-card--success .stat-card__icon { background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%); box-shadow: 0 4px 12px rgba(34, 197, 94, 0.35); }
.stat-card--warning .stat-card__icon { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35); }
.stat-card--danger  .stat-card__icon { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35); }

.stat-card__info {
  z-index: 1;
}

.stat-card__label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-card__value {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -1px;
  line-height: 1;
}

.stat-card__deco {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.06;
}

.stat-card--primary .stat-card__deco { background: #4361ee; }
.stat-card--success .stat-card__deco { background: #22c55e; }
.stat-card--warning .stat-card__deco { background: #f59e0b; }
.stat-card--danger  .stat-card__deco { background: #ef4444; }

/* ---- 功能区 ---- */
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.action-card :deep(.el-card__header) {
  padding: 16px 20px;
}

.action-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.action-btns {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btns .el-button {
  flex: 1;
  min-width: 120px;
  border-radius: 10px !important;
  font-weight: 500;
}
</style>