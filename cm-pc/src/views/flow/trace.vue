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

      <div v-if="multiItems.length > 1" class="item-tabs-wrapper">
        <button class="tab-arrow" @click="scrollTabs(-1)">&lt;</button>
        <div class="item-tabs" ref="tabsContainer">
          <span v-for="(code, idx) in multiItems" :key="code"
            class="item-tab" :class="{ active: idx === activeIndex }"
            :style="tabStyle(code, idx)"
            @click="switchItem(idx)">{{ code }}</span>
        </div>
        <button class="tab-arrow" @click="scrollTabs(1)">&gt;</button>
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

        <el-alert v-if="traceData.deleted === 1" title="该工件已被删除（软删除），以下为历史流转记录" type="error" show-icon :closable="false" style="margin: 12px 0" />

        <h3 class="timeline-title">流转时间线</h3>
        <el-timeline>
          <el-timeline-item v-for="step in traceData.steps" :key="step.id"
            :timestamp="step.createTime" placement="top" :type="timelineType(step.flowType)">
            <el-card shadow="never">
              <span style="font-weight: 600">{{ step.flowTypeDesc }}</span>
              <span v-if="step.toWorkerName"> -> {{ step.toWorkerName }}</span>
              <span v-if="step.fromWorkerName"> <- {{ step.fromWorkerName }}</span>
              <span v-if="step.customerName"> . {{ step.customerName }}</span>
              <span v-if="step.operator" style="color: #909399; margin-left: 12px">操作人：{{ step.operator }}</span>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!traceData.steps?.length" description="暂无流转记录" />
      </div>
      <el-empty v-if="searched && !traceData && !loading" description="未找到相关工件" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onActivated, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { traceItem, traceByBarcode } from '@/api/flow'
import { getAccessoriesByItemCodes } from '@/api/accessory'

const route = useRoute()
const searchType = ref('itemCode')
const searchKeyword = ref('')
const traceData = ref(null)
const loading = ref(false)
const searched = ref(false)
const multiItems = ref([])
const activeIndex = ref(0)
const itemDetails = ref({})
const tabsContainer = ref(null)

const statusTag = (s) => ({ '-1':'danger', 1:'success', 2:'primary', 3:'info', 4:'warning', 5:'warning', 6:'danger' }[s] || 'info')
const timelineType = (t) => ({ 1:'success', 2:'primary', 3:'info', 4:'warning', 5:'warning', 6:'danger', 7:'', 8:'danger' }[t] || '')

const handleTrace = async () => {
  if (!searchKeyword.value.trim()) return
  loading.value = true
  try {
    if (searchType.value === 'barcode') {
      // 条码 = 分类专用号，可能对应多个工件，全部展示为可切换的 tab
      const { data } = await traceByBarcode(searchKeyword.value.trim())
      const codes = (data || []).map(i => i.itemCode).filter(Boolean)
      searched.value = true
      if (!codes.length) {
        multiItems.value = []
        traceData.value = null
        return
      }
      multiItems.value = codes
      activeIndex.value = 0
      itemDetails.value = {}
      await loadItemDetails()
      const { data: d } = await traceItem(codes[0])
      traceData.value = d
    } else {
      const { data } = await traceItem(searchKeyword.value.trim())
      searched.value = true
      traceData.value = data
    }
  } catch (e) { traceData.value = null } finally { loading.value = false }
}

const getStatusColor = (item) => {
  if (!item) return '#909399'
  if (item.deleted === 1) return '#f56c6c'
  const map = { 1: '#67c23a', 2: '#409eff', 3: '#909399', 4: '#e6a23c', 5: '#e6a23c', 6: '#f56c6c' }
  return map[item.status] || '#909399'
}

const tabStyle = (code, idx) => {
  const d = itemDetails.value[code]
  const color = getStatusColor(d)
  if (idx === activeIndex.value) return { background: color, color: '#fff', borderColor: color }
  return { borderColor: color, color: color }
}

const scrollTabs = (dir) => {
  if (!tabsContainer.value) return
  tabsContainer.value.scrollLeft += dir * 200
}

const loadFromQuery = () => {
  const q = route.query
  if (q.itemCodes) {
    multiItems.value = q.itemCodes.split(',')
    activeIndex.value = 0
    searchType.value = 'itemCode'
    searchKeyword.value = multiItems.value[0]
    handleTrace()
  } else if (q.itemCode) {
    multiItems.value = []
    searchType.value = 'itemCode'
    searchKeyword.value = q.itemCode
    handleTrace()
  }
}

const loadItemDetails = async () => {
  if (!multiItems.value.length) return
  try {
    const { data } = await getAccessoriesByItemCodes(multiItems.value)
    const map = {}
    data.forEach(a => { map[a.itemCode] = a })
    itemDetails.value = map
  } catch (e) { /* */ }
}

onActivated(() => { nextTick(() => { loadFromQuery(); loadItemDetails() }) })
loadFromQuery()
loadItemDetails()

const switchItem = (idx) => {
  activeIndex.value = idx
  searchType.value = 'itemCode'
  searchKeyword.value = multiItems.value[idx]
  handleTrace()
}
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
.item-tabs-wrapper { display: flex; align-items: center; gap: 4px; margin-bottom: 12px; }
.tab-arrow { flex-shrink: 0; width: 28px; height: 28px; border: 1px solid #dcdfe6; border-radius: 4px; background: #fff; cursor: pointer; font-size: 12px; color: #606266; display: flex; align-items: center; justify-content: center; }
.tab-arrow:hover { border-color: #409eff; color: #409eff; }
.item-tabs { display: flex; gap: 6px; overflow-x: hidden; padding: 8px 0; flex: 1; scroll-behavior: smooth; }
.item-tab { flex-shrink: 0; padding: 6px 14px; font-size: 13px; border-radius: 6px; cursor: pointer; background: #f5f7fa; color: #606266; border: 1px solid #e4e7ed; transition: all 0.2s; }
.item-tab:hover { opacity: 0.85; }
.item-tab.active { color: #fff; }
.timeline-title { margin: 20px 0 12px; font-size: 16px; color: #1e293b; }
</style>