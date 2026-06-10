<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库存转移</h2>
    </div>

    <!-- 顶部：配置区 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="form-item">
          <span class="form-label">源师傅</span>
          <el-select v-model="fromWorkerId" placeholder="请选择" filterable clearable style="width: 140px"
            @change="onFromWorkerChange" @focus="loadWorkerData">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id"
              :style="w.id === toWorkerId ? 'display:none' : ''" />
          </el-select>
        </div>
        <el-button size="small" circle @click="swapWorkers" title="对调"><el-icon><Sort /></el-icon></el-button>
        <div class="form-item">
          <span class="form-label">目标师傅</span>
          <el-select v-model="toWorkerId" placeholder="请选择" filterable clearable style="width: 140px"
            @change="onToWorkerChange">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id"
              :style="w.id === fromWorkerId ? 'display:none' : ''" />
          </el-select>
        </div>
        <el-button size="small" @click="resetAll">清空重选</el-button>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="detail">明细</el-radio-button>
          <el-radio-button value="summary">汇总</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 中间：双栏对比 -->
    <el-card :body-style="{ padding: '0' }">
      <div class="inventory-compare">
        <!-- 源师傅面板 -->
        <div class="inventory-panel source-panel">
          <div class="panel-header source-header">
            <div class="header-left">
              {{ fromWorkerId ? getWorkerName(fromWorkerId) + ' 的库' : '源师傅库' }}
              <span v-if="fromWorkerId">（{{ fromWorkerItems.length }}件）</span>
              <span v-if="selectedItems.length" class="selected-count">已选 {{ selectedItems.length }} 件</span>
            </div>
          </div>
          <template v-if="fromWorkerId">
            <el-table v-if="viewMode === 'summary'" :data="fromWorkerGrouped" border size="small" empty-text="暂无库存"
              v-loading="loadingInventory"  :sort-multiple="true" @sort-change="handleFromSort"
              @selection-change="onGroupedSelectionChange" ref="fromGroupedTableRef"
              :row-key="row => row.barcode" :reserve-selection="true">
              <el-table-column type="selection" width="45" align="center" />
              <el-table-column type="index" label="序号" width="50" align="center" />
              <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="categoryName" label="分类" width="100" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="count" label="数量" width="70" align="center" sortable :sort-orders="['ascending', 'descending']">
                <template #default="{ row }">
                  <el-tag size="small" type="primary">{{ row.count }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="转移数量" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number :model-value="getQty(row.barcode)" :min="0" :max="row.count" size="small"
                    controls-position="right" style="width: 90px" @change="(v) => setQty(row, v)" :disabled="false" />
                </template>
              </el-table-column>
            </el-table>
            <el-table v-else :data="fromWorkerItems" border size="small" empty-text="暂无库存" v-loading="loadingInventory"
               @selection-change="onSelectionChange" :sort-multiple="true" @sort-change="handleFromSort">
              <el-table-column type="selection" width="45" align="center" />
              <el-table-column type="index" label="序号" width="50" align="center" />
              <el-table-column prop="itemCode" label="工件编号" width="150" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="categoryName" label="分类" width="100" sortable :sort-orders="['ascending', 'descending']" />
            </el-table>
          </template>
          <div v-if="!fromWorkerId" class="empty-placeholder">请选择源师傅</div>
        </div>

        <!-- 目标师傅面板 -->
        <div class="inventory-panel target-panel">
          <div class="panel-header target-header">
            <div class="header-left">
              {{ toWorkerId ? getWorkerName(toWorkerId) + ' 的库' : '目标师傅库存' }}
              <span v-if="toWorkerId">（{{ toWorkerItems.length }}件）</span>
            </div>
          </div>
          <template v-if="toWorkerId">
            <el-table v-if="viewMode === 'summary'" :data="toWorkerGrouped" border size="small" empty-text="暂无库存"
              v-loading="loadingInventory"  :sort-multiple="true" @sort-change="handleToSort"
              :row-class-name="getTargetRowClass">
              <el-table-column type="index" label="序号" width="50" align="center" />
              <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="categoryName" label="分类" width="100" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="count" label="数量" width="70" align="center" sortable :sort-orders="['ascending', 'descending']">
                <template #default="{ row }">
                  <el-tag size="small" type="primary">{{ row.count }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-table v-else :data="toWorkerItems" border size="small" empty-text="暂无库存" v-loading="loadingInventory"
               :sort-multiple="true" @sort-change="handleToSort" :row-class-name="getTargetRowClass">
              <el-table-column type="index" label="序号" width="50" align="center" />
              <el-table-column prop="itemCode" label="工件编号" width="150" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
              <el-table-column prop="categoryName" label="分类" width="100" sortable :sort-orders="['ascending', 'descending']" />
            </el-table>
          </template>
          <div v-else class="empty-placeholder">请选择目标师傅</div>
        </div>
      </div>
    </el-card>

    <!-- 底部：备注 + 提交 -->
    <div class="action-bar">
      <div class="action-left">
        <span class="action-label">备注</span>
        <el-input v-model="remark" placeholder="选填" clearable style="width: 360px" size="default" />
      </div>
      <el-button type="success" :loading="loading" :disabled="!fromWorkerId || !toWorkerId || !selectedItems.length"
        @click="handleSubmit" size="default">
        确认转移（{{ selectedItems.length }}件）
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllWorkers } from '@/api/worker'
import { searchAccessories } from '@/api/accessory'
import { flowTransfer } from '@/api/flow'

const fromWorkerId = ref(null)
const toWorkerId = ref(null)
const workerList = ref([])
const fromWorkerItems = ref([])
const toWorkerItems = ref([])
const selectedItems = ref([])
const qtyMap = ref({})
let syncingSelection = false
const transferredIds = ref(new Set())
const remark = ref('')
const loading = ref(false)
const loadingInventory = ref(false)
const viewMode = ref('detail')
const fromSortState = ref([])
const toSortState = ref([])
const fromGroupedTableRef = ref(null)

const handleFromSort = ({ prop, order }) => {
  if (!prop) { fromSortState.value = []; return }
  const idx = fromSortState.value.findIndex(s => s.prop === prop)
  if (idx >= 0) fromSortState.value.splice(idx, 1)
  if (order) fromSortState.value.unshift({ prop, order })
}
const handleToSort = ({ prop, order }) => {
  if (!prop) { toSortState.value = []; return }
  const idx = toSortState.value.findIndex(s => s.prop === prop)
  if (idx >= 0) toSortState.value.splice(idx, 1)
  if (order) toSortState.value.unshift({ prop, order })
}

const groupByBarcode = (items, sortState) => {
  const map = {}
  items.forEach(item => {
    if (!map[item.barcode]) {
      map[item.barcode] = { barcode: item.barcode, categoryName: item.categoryName, count: 0 }
    }
    map[item.barcode].count++
  })
  let result = Object.values(map)
  if (sortState && sortState.length > 0) {
    result.sort((a, b) => {
      for (const { prop, order } of sortState) {
        const asc = order === 'ascending'
        let va = a[prop], vb = b[prop]
        if (typeof va === 'number') {
          if (va !== vb) return asc ? va - vb : vb - va
        } else {
          if (String(va) !== String(vb)) return asc ? String(va).localeCompare(String(vb)) : String(vb).localeCompare(String(va))
        }
      }
      return 0
    })
  }
  return result
}

const fromWorkerGrouped = computed(() => groupByBarcode(fromWorkerItems.value, fromSortState.value))
const toWorkerGrouped = computed(() => groupByBarcode(toWorkerItems.value, toSortState.value))

const isGroupSelected = (barcode) => !!qtyMap.value[barcode]
const getQty = (barcode) => qtyMap.value[barcode] || 0

const setQty = (row, val) => {
  const v = Math.max(0, Math.min(val || 0, row.count))
  const newMap = { ...qtyMap.value }
  if (v > 0) newMap[row.barcode] = v
  else delete newMap[row.barcode]
  qtyMap.value = newMap
  syncGroupedSelection()
  // Auto-check the row if qty > 0
  const tableRef = fromGroupedTableRef.value
  if (tableRef) tableRef.toggleRowSelection(row, v > 0)
}

const syncGroupedSelection = () => {
  syncingSelection = true
  const items = []
  for (const [barcode, qty] of Object.entries(qtyMap.value)) {
    const group = fromWorkerGrouped.value.find(g => g.barcode === barcode)
    if (!group) continue
    const pool = fromWorkerItems.value.filter(i => i.barcode === barcode)
    const shuffled = [...pool].sort(() => Math.random() - 0.5)
    items.push(...shuffled.slice(0, Math.min(qty, pool.length)))
  }
  selectedItems.value = items
  const tableRef = fromGroupedTableRef.value
  if (tableRef) {
    tableRef.clearSelection()
    fromWorkerGrouped.value.forEach(row => {
      if (qtyMap.value[row.barcode]) tableRef.toggleRowSelection(row, true)
    })
  }
  syncingSelection = false
}

const onGroupedSelectionChange = (groupedRows) => {
  if (syncingSelection) return
  const newMap = {}
  groupedRows.forEach(r => {
    newMap[r.barcode] = qtyMap.value[r.barcode] || r.count
  })
  qtyMap.value = newMap
  syncGroupedSelection()
}

const onSelectionChange = (selection) => { selectedItems.value = selection }

const loadWorkerInventory = async (workerId, target) => {
  if (!workerId) { target.value = []; return }
  loadingInventory.value = true
  try {
    const { data } = await searchAccessories({ workerId, status: 2, pageNum: 1, pageSize: 100 })
    target.value = data.records
  } catch (e) { target.value = [] } finally { loadingInventory.value = false }
}

const onFromWorkerChange = () => {
  selectedItems.value = []
  transferredIds.value = new Set()
  qtyMap.value = {}
  fromSortState.value = []
  if (toWorkerId.value === fromWorkerId.value) toWorkerId.value = null
  loadWorkerInventory(fromWorkerId.value, fromWorkerItems)
  if (toWorkerId.value) loadWorkerInventory(toWorkerId.value, toWorkerItems)
}

const onToWorkerChange = () => {
  transferredIds.value = new Set()
  toSortState.value = []
  loadWorkerInventory(toWorkerId.value, toWorkerItems)
}

const getWorkerName = (id) => workerList.value.find(w => w.id === id)?.name || ''
const getTargetRowClass = ({ row }) => transferredIds.value.has(row.id) ? 'transferred-row' : 'target-row'

const swapWorkers = () => {
  const tmp = fromWorkerId.value
  fromWorkerId.value = toWorkerId.value
  toWorkerId.value = tmp
  selectedItems.value = []
  transferredIds.value = new Set()
  qtyMap.value = {}
  fromSortState.value = []
  toSortState.value = []
  if (fromWorkerId.value) loadWorkerInventory(fromWorkerId.value, fromWorkerItems)
  else fromWorkerItems.value = []
  if (toWorkerId.value) loadWorkerInventory(toWorkerId.value, toWorkerItems)
  else toWorkerItems.value = []
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await ElMessageBox.confirm('确认转移 ' + selectedItems.value.length + ' 件工件？', '确认', { type: 'warning' })
    await flowTransfer({
      fromWorkerId: fromWorkerId.value,
      toWorkerId: toWorkerId.value,
      accessoryIds: selectedItems.value.map(i => i.id),
      remark: remark.value
    })
    const justTransferred = new Set(selectedItems.value.map(i => i.id))
    viewMode.value = 'detail'
    ElMessage.success('转移成功')
    selectedItems.value = []
    qtyMap.value = {}
    if (fromWorkerId.value) loadWorkerInventory(fromWorkerId.value, fromWorkerItems)
    if (toWorkerId.value) await loadWorkerInventory(toWorkerId.value, toWorkerItems)
    transferredIds.value = justTransferred
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

const resetAll = () => {
  fromWorkerId.value = null
  toWorkerId.value = null
  fromWorkerItems.value = []
  toWorkerItems.value = []
  selectedItems.value = []
  transferredIds.value = new Set()
  remark.value = ''
  viewMode.value = 'detail'
  fromSortState.value = []
  toSortState.value = []
}

onMounted(async () => {
  try { const { data } = await getAllWorkers(); workerList.value = data } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 16px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.form-item { display: flex; align-items: center; gap: 6px; }
.form-label { font-size: 13px; color: #475569; white-space: nowrap; }
.arrow { color: #94a3b8; font-size: 16px; font-weight: 700; }

.inventory-compare { display: flex; }
.inventory-panel { flex: 1; min-width: 0; min-height: 200px; }
.inventory-panel:first-child { border-right: 1px solid #d9d9d9; }
.panel-header { font-size: 13px; font-weight: 600; padding: 10px 16px; display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 8px; }
.source-header { background: #409eff; color: #fff; }
.target-header { background: #67c23a; color: #fff; }
.selected-count { font-size: 12px; background: rgba(255,255,255,0.3); color: #fff; padding: 2px 8px; border-radius: 10px; }
.empty-placeholder { color: #94a3b8; font-size: 13px; padding: 40px 0; text-align: center; }
:deep(.el-table__empty-block) { min-height: 160px; }
:deep(.source-row td) { background-color: #ecf5ff !important; }
:deep(.source-row:hover td) { background-color: #d9ecff !important; }
:deep(.target-row td) { background-color: #f0f9eb !important; }
:deep(.target-row:hover td) { background-color: #e1f3d8 !important; }
:deep(.transferred-row td) { background-color: #95d475 !important; font-weight: 600; }
:deep(.transferred-row:hover td) { background-color: #7ccc63 !important; }

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 14px 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.action-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.action-label {
  font-size: 14px;
  color: #475569;
  white-space: nowrap;
  font-weight: 500;
}
</style>