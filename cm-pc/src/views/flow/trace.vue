<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">工件追溯</h2>
    </div>
    <el-card>
      <el-input v-model="itemCode" placeholder="输入工件编号查询流转记录" clearable style="max-width: 400px"
        @keyup.enter="handleTrace">
        <template #append><el-button @click="handleTrace" :loading="loading">查询</el-button></template>
      </el-input>

      <div v-if="traceData" style="margin-top: 20px">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="工件编号">{{ traceData.itemCode }}</el-descriptions-item>
          <el-descriptions-item label="条码">{{ traceData.barcode }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ traceData.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusTag(traceData.currentStatus)" size="small">{{ traceData.currentStatusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前持有">{{ traceData.currentHolder }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin: 20px 0 12px; font-size: 16px; color: #1e293b">流转时间线</h3>
        <el-timeline>
          <el-timeline-item v-for="step in traceData.steps" :key="step.id"
            :timestamp="step.createTime" placement="top" :type="timelineType(step.flowType)">
            <el-card shadow="never">
              <span style="font-weight: 600">{{ step.flowTypeDesc }}</span>
              <span v-if="step.toWorkerName"> → {{ step.toWorkerName }}</span>
              <span v-if="step.fromWorkerName"> ← {{ step.fromWorkerName }}</span>
              <span v-if="step.customerName"> · 客户：{{ step.customerName }}</span>
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
import { traceItem } from '@/api/flow'

const itemCode = ref('')
const traceData = ref(null)
const loading = ref(false)

const statusTag = (s) => ({ 1: 'success', 2: 'primary', 3: 'info', 4: 'warning', 5: 'warning', 6: 'danger' }[s] || 'info')
const timelineType = (t) => ({ 1: 'success', 2: 'primary', 3: 'info', 4: 'warning', 5: 'warning', 6: 'danger', 7: '' }[t] || '')

const handleTrace = async () => {
  if (!itemCode.value.trim()) return
  loading.value = true
  try {
    const { data } = await traceItem(itemCode.value.trim())
    traceData.value = data
  } catch (e) { traceData.value = null } finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
</style>