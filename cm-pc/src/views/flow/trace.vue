<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">条码追溯</h2></div>
    <el-card>
      <el-input v-model="barcode" placeholder="输入条码查询流转记录" clearable style="max-width: 400px"
        @keyup.enter="handleTrace">
        <template #append><el-button @click="handleTrace" :loading="loading">查询</el-button></template>
      </el-input>

      <div v-if="traceData" style="margin-top: 20px">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="条码">{{ traceData.barcode }}</el-descriptions-item>
          <el-descriptions-item label="配件名称">{{ traceData.accessoryName }}</el-descriptions-item>
          <el-descriptions-item label="当前库存">{{ traceData.currentQty }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">{{ traceData.currentHolder }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin: 20px 0 12px; font-size: 16px; color: #303133">流转时间线</h3>
        <el-timeline>
          <el-timeline-item v-for="step in traceData.steps" :key="step.id"
            :timestamp="step.createTime" placement="top"
            :type="getTimelineType(step.flowType)">
            <el-card shadow="never">
              <span style="font-weight: 600">{{ step.flowTypeDesc }}</span>
              <span v-if="step.workerName"> — {{ step.workerName }}</span>
              <span v-if="step.customerName"> — 客户：{{ step.customerName }}</span>
              <span v-if="step.operator" style="color: #909399; margin-left: 12px">操作人：{{ step.operator }}</span>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!traceData.steps?.length" description="暂无流转记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { traceBarcode } from '@/api/flow'

const barcode = ref('')
const traceData = ref(null)
const loading = ref(false)

const handleTrace = async () => {
  if (!barcode.value) return
  loading.value = true
  try {
    const { data } = await traceBarcode(barcode.value)
    traceData.value = data
  } catch (e) { traceData.value = null } finally { loading.value = false }
}

const getTimelineType = (flowType) => {
  const map = { 1: 'success', 2: 'primary', 3: 'warning', 4: 'danger' }
  return map[flowType] || ''
}
</script>