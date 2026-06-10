<template>
  <div class="dashboard">
    <div class="stat-grid">
      <div class="stat-card stat-card--primary">
        <div class="stat-card__icon"><el-icon :size="28"><Box /></el-icon></div>
        <div class="stat-card__info">
          <div class="stat-card__label">可支配库存</div>
          <div class="stat-card__value">{{ stats.inStockCount ?? '-' }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card__icon"><el-icon :size="28"><Upload /></el-icon></div>
        <div class="stat-card__info">
          <div class="stat-card__label">已出库</div>
          <div class="stat-card__value">{{ stats.outStockCount ?? '-' }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--warning">
        <div class="stat-card__icon"><el-icon :size="28"><User /></el-icon></div>
        <div class="stat-card__info">
          <div class="stat-card__label">师傅数量</div>
          <div class="stat-card__value">{{ stats.workerCount ?? '-' }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
      <div class="stat-card stat-card--danger">
        <div class="stat-card__icon"><el-icon :size="28"><Document /></el-icon></div>
        <div class="stat-card__info">
          <div class="stat-card__label">总工件数</div>
          <div class="stat-card__value">{{ stats.totalAccessories ?? '-' }}</div>
        </div>
        <div class="stat-card__deco"></div>
      </div>
    </div>

    <div class="middle-row">
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
          <el-button type="success" size="large" @click="$router.push('/flow/outbound')">
            <el-icon><Upload /></el-icon> 配件出库
          </el-button>
          <el-button type="warning" size="large" @click="$router.push('/flow/return')">
            <el-icon><RefreshLeft /></el-icon> 配件归还
          </el-button>
          <el-button type="danger" size="large" @click="$router.push('/flow/sell')">
            <el-icon><ShoppingCart /></el-icon> 配件售卖
          </el-button>
          <el-button size="large" @click="$router.push('/flow/transfer')">
            <el-icon><Sort /></el-icon> 库存转移
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
            <el-icon><Search /></el-icon> 工件追溯
          </el-button>
          <el-button size="large" @click="$router.push('/inventory/master')">
            <el-icon><Box /></el-icon> 总库
          </el-button>
          <el-button size="large" @click="$router.push('/inventory/available')">
            <el-icon><Goods /></el-icon> 可支配库
          </el-button>
          <el-button size="large" @click="$router.push('/worker')">
            <el-icon><User /></el-icon> 师傅管理
          </el-button>
        </div>
      </el-card>
    </div>

    <div class="bottom-row">
      <el-card class="recent-card">
        <template #header>
          <div class="action-card__header">
            <el-icon color="#f59e0b"><Clock /></el-icon>
            <span>最近流转</span>
          </div>
        </template>
        <el-table :data="stats.recentFlows || []" stripe size="small" style="width: 100%">
          <el-table-column prop="itemCode" label="工件编号" width="160" />
          <el-table-column label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="flowTagType(row.flowType)">{{ flowTypeMap[row.flowType] || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="流转方向">
            <template #default="{ row }">
              <span v-if="row.flowType === 7">{{ row.fromWorkerName }} → {{ row.toWorkerName }}</span>
              <span v-else-if="row.toWorkerName">{{ row.toWorkerName }}</span>
              <span v-else-if="row.customerName">客户: {{ row.customerName }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="90" align="center" />
          <el-table-column label="时间" width="160" align="center">
            <template #default="{ row }">{{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '' }}</template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-card class="dist-card">
        <template #header>
          <div class="action-card__header">
            <el-icon color="#22c55e"><PieChart /></el-icon>
            <span>分类分布</span>
          </div>
        </template>
        <div class="dist-list">
          <div v-for="item in (stats.categoryDistribution || [])" :key="item.name" class="dist-item">
            <div class="dist-item__label">
              <span class="dist-item__name">{{ item.name }}</span>
              <span class="dist-item__count">{{ item.count }}件</span>
            </div>
            <el-progress :percentage="maxDist > 0 ? Math.round(item.count / maxDist * 100) : 0"
              :stroke-width="10" :show-text="false" :color="distColor(item.name)" />
          </div>
          <el-empty v-if="!(stats.categoryDistribution || []).length" description="暂无数据" :image-size="60" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats } from '@/api/dashboard'

const stats = ref({})

const maxDist = computed(() => {
  const list = stats.value.categoryDistribution || []
  return list.length ? Math.max(...list.map(i => i.count)) : 0
})

const flowTypeMap = { 1: '入库', 2: '出库', 3: '归还', 4: '退回', 5: '返厂', 6: '售卖', 7: '转移' }
const flowTagType = (t) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning', 4: '', 5: '', 6: 'danger', 7: '' }
  return map[t] || ''
}

const distColors = ['#4361ee', '#22c55e', '#f59e0b', '#ef4444', '#6366f1', '#14b8a6', '#ec4899', '#8b5cf6']
const distColor = (name) => {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return distColors[Math.abs(hash) % distColors.length]
}

onMounted(async () => {
  try {
    const { data } = await getDashboardStats()
    stats.value = data
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
})
</script>

<style scoped>
.dashboard { padding: 24px; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card { background: #fff; border-radius: 16px; padding: 24px; display: flex; align-items: center; gap: 16px; position: relative; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); transition: all 0.3s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.stat-card__icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: #fff; z-index: 1; }
.stat-card--primary .stat-card__icon { background: linear-gradient(135deg, #4361ee, #6366f1); box-shadow: 0 4px 12px rgba(67,97,238,0.35); }
.stat-card--success .stat-card__icon { background: linear-gradient(135deg, #22c55e, #16a34a); box-shadow: 0 4px 12px rgba(34,197,94,0.35); }
.stat-card--warning .stat-card__icon { background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 4px 12px rgba(245,158,11,0.35); }
.stat-card--danger .stat-card__icon { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 4px 12px rgba(239,68,68,0.35); }
.stat-card__info { z-index: 1; }
.stat-card__label { font-size: 13px; color: #94a3b8; margin-bottom: 6px; }
.stat-card__value { font-size: 32px; font-weight: 800; color: #1e293b; letter-spacing: -1px; }
.stat-card__deco { position: absolute; right: -20px; bottom: -20px; width: 100px; height: 100px; border-radius: 50%; opacity: 0.06; }
.stat-card--primary .stat-card__deco { background: #4361ee; }
.stat-card--success .stat-card__deco { background: #22c55e; }
.stat-card--warning .stat-card__deco { background: #f59e0b; }
.stat-card--danger .stat-card__deco { background: #ef4444; }

.middle-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 24px; }
.action-card__header { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #1e293b; }
.action-btns { display: flex; gap: 12px; flex-wrap: wrap; }
.action-btns .el-button { flex: 1; min-width: 120px; border-radius: 10px !important; font-weight: 500; }

.bottom-row { display: grid; grid-template-columns: 3fr 2fr; gap: 20px; }
.recent-card { min-height: 320px; }

.dist-list { display: flex; flex-direction: column; gap: 14px; }
.dist-item__label { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.dist-item__name { font-size: 13px; color: #334155; font-weight: 500; }
.dist-item__count { font-size: 13px; color: #94a3b8; }

@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .middle-row { grid-template-columns: 1fr; }
  .bottom-row { grid-template-columns: 1fr; }
}
</style>