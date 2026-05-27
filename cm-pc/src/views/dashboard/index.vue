<template>
  <div class="page-container">
    <h2 class="page-title">首页概览</h2>
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff15; color: #409eff">
            <el-icon :size="28"><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">配件种类</div>
            <div class="stat-value">{{ stats.totalAccessories || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a15; color: #67c23a">
            <el-icon :size="28"><Menu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">配件分类</div>
            <div class="stat-value">{{ stats.totalCategories || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c15; color: #e6a23c">
            <el-icon :size="28"><Checked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在库数量</div>
            <div class="stat-value">{{ stats.availableQty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c15; color: #f56c6c">
            <el-icon :size="28"><Upload /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在外数量</div>
            <div class="stat-value">{{ stats.outQty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>快捷操作</span></template>
          <div class="quick-actions">
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
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>快捷查询</span></template>
          <div class="quick-actions">
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
      </el-col>
    </el-row>
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
.page-container { padding: 20px; }
.page-title { margin: 0 0 16px; font-size: 18px; }
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>