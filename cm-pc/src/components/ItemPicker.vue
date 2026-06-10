<template>
  <el-dialog v-model="visible" :title="title" width="960px" :close-on-click-modal="false" top="5vh">
    <div class="picker-search-form">
      <div class="picker-search-row">
        <div class="search-item">
          <span class="search-label">条码</span>
          <el-input v-model="filters.barcode" clearable placeholder="请输入" style="width: 150px" @keyup.enter="doSearch" />
        </div>
        <div class="search-item">
          <span class="search-label">工件编号</span>
          <el-input v-model="filters.itemCode" clearable placeholder="请输入" style="width: 150px" @keyup.enter="doSearch" />
        </div>
        <div class="search-item">
          <span class="search-label">分类</span>
          <CategoryCascader v-model="filters.categoryId" placeholder="全部" @update:modelValue="doSearch" />
        </div>
        <div v-if="showStatus" class="search-item">
          <span class="search-label">状态</span>
          <el-select v-model="filters.status" clearable placeholder="全部" style="width: 130px" @change="doSearch">
            <el-option label="在库" :value="1" />
            <el-option label="已出库" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="寄回厂家" :value="4" />
            <el-option label="旧件待返厂" :value="5" />
          </el-select>
        </div>
        <div v-if="showWorkerFilter" class="search-item">
          <span class="search-label">持有者</span>
          <el-select v-model="filters.owner" :clearable="!workerId && !fixedOwner" :disabled="!!workerId || !!fixedOwner" placeholder="全部" style="width: 140px" @change="doSearch">
            <el-option label="总部" value="hq" />
            <el-option v-for="w in workers" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </div>
        <div class="picker-search-actions">
          <el-radio-group v-model="viewMode" size="small" style="margin-right:8px">
            <el-radio-button value="detail">明细</el-radio-button>
            <el-radio-button value="summary">汇总</el-radio-button>
          </el-radio-group>
          <el-button type="primary" @click="doSearch">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 明细视图 -->
    <el-table v-if="viewMode==='detail'" :data="filteredList" stripe border v-loading="loading" @selection-change="onSelectionChange"
      style="margin-top: 12px" :max-height="400">
      <el-table-column v-if="multiple" type="selection" width="50" align="center" />
      <el-table-column prop="itemCode" label="工件编号" width="160" />
      <el-table-column prop="barcode" label="条码" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column v-if="showStatus" prop="statusDesc" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="showWorkerColumn" prop="workerName" label="持有师傅" width="100" />
      <el-table-column v-if="!multiple" label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="onSingleSelect(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 汇总视图 -->
    <el-table v-else :data="summaryList" stripe border v-loading="loading" style="margin-top: 12px" :max-height="400"
      ref="summaryTableRef" @selection-change="onSummarySelectionChange" :row-key="r => r.barcode">
      <el-table-column v-if="multiple" type="selection" width="50" align="center" reserve-selection />
      <el-table-column prop="barcode" label="条码" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="totalCount" label="可选数量" width="100" align="center" />
      <el-table-column label="选择数量" width="180" align="center">
        <template #default="{ row }">
          <el-input-number :model-value="getQty(row.barcode)" @update:model-value="setQty(row.barcode, $event)"
            :min="0" :max="row.totalCount" size="small" style="width: 130px" />
        </template>
      </el-table-column>
    </el-table>

    <div class="picker-pagination">
      <span class="page-label">每页</span>
      <el-input v-model.number="pageSizeInput" size="small" class="page-size-input"
        @change="onPageSizeChange" />
      <span class="page-label">条</span>
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50, 100]"
        layout="total, prev, pager, next" @change="loadData" background />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="multiple" type="primary" :disabled="confirmCount === 0" @click="handleConfirm">
        确认选择（{{ confirmCount }}件）
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { getCategories } from '@/api/category'
import CategoryCascader from '@/components/CategoryCascader.vue'
import { getAllWorkers } from '@/api/worker'
import { searchAccessories } from '@/api/accessory'

const props = defineProps({
  title: { type: String, default: '选择工件' },
  multiple: { type: Boolean, default: true },
  defaultStatus: { type: Number, default: null },
  showStatus: { type: Boolean, default: true },
  showWorkerFilter: { type: Boolean, default: false },
  workerId: { type: [Number, String, null], default: null },
  fixedOwner: { type: String, default: null },
  excludeIds: { type: Array, default: () => [] },
  excludeBarcodes: { type: Array, default: () => [] }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
const categories = ref([])
const currentSelection = ref([])
const selectedIds = computed(() => new Set(props.excludeIds))
const excludeBarcodeSet = computed(() => new Set(props.excludeBarcodes))

const filters = reactive({
  barcode: '', itemCode: '', categoryId: null, status: props.defaultStatus, owner: props.workerId || null
})

const viewMode = ref('detail')
const workers = ref([])
const showWorkerColumn = computed(() => props.showStatus || props.showWorkerFilter)

// ============ 汇总视图 ============
// summaryList: 纯展示数据，不含 qty
const filteredList = computed(() => list.value.filter(item => !selectedIds.value.has(item.id)))

const summaryList = computed(() => {
  const map = {}
  list.value.forEach(item => {
    if (excludeBarcodeSet.value.has(item.barcode)) return
    if (!map[item.barcode]) {
      map[item.barcode] = { barcode: item.barcode, categoryName: item.categoryName, totalCount: 0 }
    }
    map[item.barcode].totalCount++
  })
  return Object.values(map)
})

// qtyMap: 独立的 ref，用整个对象替换来触发响应式
const qtyMap = ref({})
const summaryTableRef = ref(null)

// 每次 summaryList 变化，初始化新条码数量为 0
watch(summaryList, (newList) => {
  const old = qtyMap.value
  const next = {}
  newList.forEach(r => {
    next[r.barcode] = old[r.barcode] ?? 0
  })
  qtyMap.value = next
}, { immediate: true })

const getQty = (barcode) => qtyMap.value[barcode] ?? 0

const setQty = (barcode, val) => {
  qtyMap.value = { ...qtyMap.value, [barcode]: val }
  const row = summaryList.value.find(r => r.barcode === barcode)
  if (row && summaryTableRef.value) {
    summaryTableRef.value.toggleRowSelection(row, val > 0)
  }
}

// 汇总总件数 — 显式依赖 qtyMap.value
const summaryTotal = computed(() => {
  return Object.values(qtyMap.value).reduce((sum, n) => sum + (Number(n) || 0), 0)
})

// 勾选行 → 填满数量
const onSummarySelectionChange = (rows) => {
  const checked = new Set(rows.map(r => r.barcode))
  const next = { ...qtyMap.value }
  summaryList.value.forEach(r => {
    if (checked.has(r.barcode)) {
      if (!next[r.barcode]) next[r.barcode] = r.totalCount
    } else {
      next[r.barcode] = 0
    }
  })
  qtyMap.value = next
}

// ============ 明细视图 ============
const onSelectionChange = (selection) => {
  currentSelection.value = selection
}

const onSingleSelect = (row) => {
  visible.value = false
  emit('confirm', [row])
}

const onConfirm = () => {
  visible.value = false
  emit('confirm', currentSelection.value)
}

const onConfirmSummary = () => {
  const selected = []
  const q = qtyMap.value
  const barcodeNeed = {}
  summaryList.value.forEach(r => {
    barcodeNeed[r.barcode] = { need: q[r.barcode] || 0, picked: 0 }
  })
  const sorted = [...list.value].sort((a, b) => (a.itemCode || '').localeCompare(b.itemCode || ''))
  sorted.forEach(item => {
    const bucket = barcodeNeed[item.barcode]
    if (!bucket) return
    if (selectedIds.value.has(item.id)) return
    if (bucket.picked < bucket.need) {
      selected.push(item)
      bucket.picked++
    }
  })
  visible.value = false
  emit('confirm', selected)
}

// ============ 通用 ============
watch(() => props.defaultStatus, (val) => { filters.status = val })

const open = async () => {
  filters.barcode = ''
  filters.itemCode = ''
  filters.categoryId = null
  filters.status = props.defaultStatus
  filters.owner = props.fixedOwner || props.workerId || null
  viewMode.value = 'detail'
  qtyMap.value = {}
  currentSelection.value = []
  pageNum.value = 1
  visible.value = true
  await Promise.all([loadCategories(), loadWorkers()])
  loadData()
}

const loadCategories = async () => {
  try {
    const { data } = await getCategories()
    categories.value = data
  } catch (e) { /* ignore */ }
}

const loadWorkers = async () => {
  if (!props.showWorkerFilter) { workers.value = []; return }
  try {
    const { data } = await getAllWorkers()
    workers.value = data
  } catch (e) { workers.value = [] }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (filters.barcode) params.barcode = filters.barcode
    if (filters.itemCode) params.keyword = filters.itemCode
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.status) params.status = filters.status
    params.statusNot = 6
    if (filters.owner === 'hq') {
      params.status = 1
    } else if (filters.owner) {
      params.workerId = filters.owner
    } else if (props.workerId) {
      params.workerId = props.workerId
    }
    const { data } = await searchAccessories(params)
    list.value = data.records
    total.value = data.total
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

const doSearch = () => {
  pageNum.value = 1
  loadData()
}

const onPageSizeChange = (val) => {
  pageSize.value = val || 20
  pageNum.value = 1
  loadData()
}

const confirmCount = computed(() => {
  if (viewMode.value === 'summary') return summaryTotal.value
  return currentSelection.value.length
})

const handleConfirm = () => {
  if (viewMode.value === 'summary') {
    onConfirmSummary()
  } else {
    onConfirm()
  }
}

const statusTagType = (s) => ({
  1: 'success', 2: 'primary', 3: 'info', 4: 'warning', 5: 'warning', 6: 'danger'
}[s] || 'info')

defineExpose({ open })
</script>

<style scoped>
.picker-search-form {
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 16px;
}
.picker-search-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
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
.picker-search-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.picker-pagination {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  justify-content: flex-end;
}
.picker-pagination .page-label {
  font-size: 13px;
  color: #606266;
  margin: 0 4px;
}
.picker-pagination .page-size-input { width: 52px; }
.picker-pagination :deep(.el-pagination) { margin-left: 12px; }
</style>