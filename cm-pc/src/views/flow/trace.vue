<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">工件追溯</h2>
    </div>
    <el-card>
      <div class="search-form">
        <div class="search-row">
          <div class="search-item">
            <el-select v-model="searchType" style="width: 100px">
              <el-option label="工件编号" value="itemCode" />
              <el-option label="条码" value="barcode" />
            </el-select>
            <el-input v-model="searchKeyword" clearable :placeholder="searchType === 'itemCode' ? '请输入工件编号' : '请输入条码'" style="width: 260px"
              @keyup.enter="handleTrace" />
          </div>
          <div class="search-actions">
            <el-button type="primary" @click="handleTrace" :loading="loading">查询</el-button>
          </div>
        </div>
      </div>

      <div v-if="traceData">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="工件编号">{{ traceData.itemCode }}</el-descriptions-item>
          <el-descriptions-item label="条码">{{ traceData.barcode }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ traceData.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusTag(traceData.currentStatus)" size="small">{{ traceData.currentStatusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前持有">{{ traceData.currentHolder }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="timeline-title">流转时间线</h3>
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
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { traceItem, traceByBarcode } from '@/api/flow'

const route = useRoute()
const searchType = ref('itemCode')
const searchKeyword = ref('')
const traceData = ref(null)
const loading = ref(false)

const statusTag = (s) => ({ 1:'success', 2:'primary', 3:'info', 4:'warning', 5:'warning', 6:'danger' }[s] || 'info')
const timelineType = (t) => ({ 1:'success', 2:'primary', 3:'info', 4:'warning', 5:'warning', 6:'danger', 7:'' }[t] || '')

const handleTrace = async () => {
  if (!searchKeyword.value.trim()) return
  loading.value = true
  try {
    const fn = searchType.value === 'barcode' ? traceByBarcode : traceItem
    const { data } = await fn(searchKeyword.value.trim())
    traceData.value = data
  } catch (e) { traceData.value = null } finally { loading.value = false }
}
watch(() => route.query.itemCode, (val) => {
  if (val) {
    searchType.value = 'itemCode'
    searchKeyword.value = val
    handleTrace()
  }
}, { immediate: true })
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.search-form {
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px 12px;
  margin-bottom: 16px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  min-width: 56px;
  text-align: right;
}
.search-actions {
  display: flex;
  gap: 8px;
}
.timeline-title {
  margin: 20px 0 12px;
  font-size: 16px;
  color: #1e293b;
}
</style>