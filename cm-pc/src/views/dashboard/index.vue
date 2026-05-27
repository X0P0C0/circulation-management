<template>
  <div class="page-container">
    <h2 class="page-title">首页概览</h2>
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>配件总数</span></template>
          <div class="stat-value">{{ stats.totalAccessories || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>配件分类</span></template>
          <div class="stat-value">{{ stats.totalCategories || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>在库数量</span></template>
          <div class="stat-value primary">{{ stats.availableQty || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header><span>领用在途</span></template>
          <div class="stat-value warning">{{ stats.outQty || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>快捷操作</span></template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/accessory/inbound')">配件入库</el-button>
            <el-button type="success" @click="$router.push('/flow/transfer-out')">配件领用</el-button>
            <el-button type="warning" @click="$router.push('/flow/transfer-in')">配件归还</el-button>
            <el-button type="danger" @click="$router.push('/flow/sell')">配件售卖</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>系统信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">配件流转管理系统</el-descriptions-item>
            <el-descriptions-item label="版本">V1.0.0</el-descriptions-item>
            <el-descriptions-item label="技术栈">Spring Boot + Vue3 + Element Plus</el-descriptions-item>
          </el-descriptions>
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
.stat-value {
  font-size: 32px;
  font-weight: 600;
  text-align: center;
  color: #303133;
}
.stat-value.primary { color: #409eff; }
.stat-value.warning { color: #e6a23c; }
.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>