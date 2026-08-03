<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
    </div>
    <el-card>
      <div class="search-form">
        <div class="search-row">
          <div class="search-item">
            <span class="search-label">操作类型</span>
            <el-select v-model="actionType" clearable placeholder="全部" style="width: 160px">
              <el-option label="登录" value="LOGIN" />
              <el-option label="入库" value="INBOUND" />
              <el-option label="出库" value="OUTBOUND" />
              <el-option label="归还" value="RETURN" />
              <el-option label="售卖" value="SELL" />
              <el-option label="转移" value="TRANSFER" />
            </el-select>
          </div>
          <div class="search-item">
            <span class="search-label">操作人</span>
            <el-select v-model="operatorFilter" clearable placeholder="全部" filterable style="width: 160px" @focus="loadUsers">
              <el-option v-for="u in userList" :key="u.username" :label="u.realName || u.username" :value="u.username" />
            </el-select>
          </div>
          <div class="search-item">
            <span class="search-label">关联条码</span>
            <el-input v-model="barcodeFilter" clearable placeholder="请输入条码" style="width: 160px" @keyup.enter="loadData" />
          </div>
          <div class="search-item">
            <span class="search-label">操作时间</span>
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
              start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
              style="width: 300px" clearable />
          </div>
          <div class="search-actions">
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
            <el-button size="small" @click="handleExport">导出</el-button>
          </div>
        </div>
      </div>

      <el-table :data="list" stripe border v-loading="loading" :sort-multiple="true" @sort-change="handleSortChange" @row-click="onRowClick">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="actionType" label="操作类型" width="120" align="center" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <el-tag size="small">{{ actionMap[row.actionType] || row.actionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" show-overflow-tooltip />
        <el-table-column label="工件信息" min-width="260">
          <template #default="{ row }">
            <div v-if="row._items && row._items.length" class="batch-cell">
              <div v-for="item in row._items" :key="item.id" class="batch-card" @click.stop="goToTrace(item.itemCode)">
                <span class="link-text" style="font-weight:600;font-size:12px">{{ item.itemCode }}</span>
                <span style="font-size:11px;color:#909399">{{ item.parentCategoryName ? item.parentCategoryName + " / " : "" }}{{ item.categoryName || "-" }}<span v-if="item.partNumber"> ({{ item.partNumber }})</span></span>
              </div>
            </div>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" align="center" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="createTime" label="操作时间" width="180" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">{{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '' }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <span class="page-label">每页</span>
        <el-input v-model.number="pageSizeInput" size="small" class="page-size-input"
          @change="pageSize = pageSizeInput || 20; pageNum = 1; loadData()" />
        <span class="page-label">条</span>
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[20, 50, 100]"
          layout="total, prev, pager, next, jumper" @change="loadData" background />
      </div>
    </el-card>

    <el-dialog v-model="batchDialogVisible" title="工件列表" width="520px" close-on-click-modal>
      <el-table :data="batchItems" stripe size="small" max-height="400">
        <el-table-column label="工件编号" width="160">
          <template #default="{ row }"><span class="link-text" @click="goToTrace(row.itemCode); batchDialogVisible = false">{{ row.itemCode }}</span></template>
        </el-table-column>
        <el-table-column label="分类">
          <template #default="{ row }">{{ row.parentCategoryName ? row.parentCategoryName + " / " : "" }}{{ row.categoryName || "-" }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag size="small" :type="logStatusTag(row)">{{ row.statusDesc || "-" }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="barcode" label="条码" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="traceAll">全部追溯</el-button>
        <el-button @click="batchDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useMultiSort } from '@/components/multi-sort/useMultiSort'
import { getLogs } from '@/api/log'
import { getAccessoriesByIds } from '@/api/accessory'
import { getAllUsers } from '@/api/user'
import { exportCsv } from '@/utils/export'

const router = useRouter()
const batchDialogVisible = ref(false)
const batchItems = ref([])
const list = ref([])
const userList = ref([])
const { sorts: sortsRef, buildSortParams, applyLocalSort } = useMultiSort()

function applyMultiSortFromTable(sortsRef, { prop, order, shift }) {
  if (!prop || !order) { sortsRef.value = []; return }
  const idx = sortsRef.value.findIndex((s) => s.prop === prop)
  if (shift) {
    if (idx === -1) sortsRef.value.push({ prop, order })
    else sortsRef.value[idx].order = order
  } else {
    sortsRef.value = [{ prop, order }]
  }
}

const handleSortChange = ({ prop, order, $event }) => {
  applyMultiSortFromTable(sortsRef, { prop, order, shift: $event?.shiftKey })
  loadData()
}
const loading = ref(false)
const actionType = ref('')
const operatorFilter = ref('')
const barcodeFilter = ref('')
const dateRange = ref(null)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
const total = ref(0)

const actionMap = {
  LOGIN: '登录', INBOUND: '入库', OUTBOUND: '出库',
  RETURN: '归还', SELL: '售卖', TRANSFER: '转移'
}

const goToTrace = (itemCode) => { router.push({ path: '/flow/trace', query: { itemCode } }) }
const logStatusTag = (item) => { if (item.deleted === 1) return "danger"; return ({ 1:"success", 2:"primary", 3:"info", 4:"warning", 5:"warning", 6:"danger" }[item.status] || "info") }
const openBatchDialog = (items) => { batchItems.value = items; batchDialogVisible.value = true }
const traceAll = () => {
  const codes = batchItems.value.map(i => i.itemCode).join(',')
  batchDialogVisible.value = false
  router.push({ path: '/flow/trace', query: { itemCodes: codes } })
}
const onRowClick = async (row, column, event) => {
  if (event.target.closest('.link-text')) return
  if (row._items && row._items.length) { openBatchDialog(row._items); return }
  if (row.accessoryId) {
    try { const { data } = await getAccessoriesByIds([row.accessoryId]); openBatchDialog(data || []) } catch (e) { /* */ }
  }
}

const loadBatchItems = async (records) => {
  if (!records || !records.length) return
  const allIds = []
  records.forEach(r => {
    if (r.accessoryIds) {
      try { JSON.parse(r.accessoryIds).forEach(id => allIds.push(id)) } catch (e) { /* skip */ }
    } else if (r.accessoryId) {
      allIds.push(r.accessoryId)
    }
  })
  if (!allIds.length) return
  try {
    const { data } = await getAccessoriesByIds(allIds)
    const map = {}
    data.forEach(a => { map[a.id] = a })
    records.forEach(r => {
      if (r.accessoryIds) {
        try {
          const aids = JSON.parse(r.accessoryIds)
          r._items = aids.map(id => map[id]).filter(Boolean)
        } catch (e) { /* skip */ }
      } else if (r.accessoryId && map[r.accessoryId]) {
        r._items = [map[r.accessoryId]]
      }
    })
  } catch (e) { console.error('loadBatchItems error:', e) }
}

const resetFilters = () => {
  actionType.value = ''
  operatorFilter.value = ''
  barcodeFilter.value = ''
  dateRange.value = null
  pageNum.value = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (actionType.value) params.actionType = actionType.value
    if (operatorFilter.value) params.operator = operatorFilter.value
    if (barcodeFilter.value) params.barcode = barcodeFilter.value
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const { data } = await getLogs({ ...params, ...buildSortParams() })
    const sorted = applyLocalSort(data.records)
    await loadBatchItems(sorted)
    list.value = sorted
    total.value = data.total
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

const handleExport = () => {
  const headers = ['操作类型', '操作内容', '关联条码', '操作人', '操作时间']
  const rows = (list.value || []).map(r => [
    actionMap[r.actionType] || r.actionType,
    r.content || '',
    r.relatedBarcode || '',
    r.operator || '',
    r.createTime ? r.createTime.replace('T', ' ').substring(0, 19) : ''
  ])
  exportCsv('操作日志', headers, rows)
}

onMounted(() => { loadData(); loadUsers() })
onActivated(() => { loadData() })

const loadUsers = async () => { try { const { data } = await getAllUsers(); userList.value = data } catch (e) { /* ignore */ } }
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
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
.search-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.pagination-wrapper {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 28px;
}
.pagination-wrapper .page-label {
  font-size: 13px;
  color: #606266;
  margin: 0 4px;
  line-height: 28px;
}
.pagination-wrapper .page-size-input { width: 52px; }
.pagination-wrapper :deep(.el-input--small) { height: 24px; }
.pagination-wrapper :deep(.el-pagination) {
  margin-left: 12px;
  height: 28px;
  display: flex;
  align-items: center;
}
.link-text { color: #409eff; cursor: pointer; }
.batch-cell { display: flex; gap: 6px; overflow-x: auto; max-width: 100%; padding: 2px 0; }
.batch-card { flex-shrink: 0; display: flex; flex-direction: column; gap: 1px; padding: 3px 8px; background: #f5f7fa; border-radius: 4px; cursor: pointer; border: 1px solid #e4e7ed; }
.batch-card:hover { border-color: #409eff; background: #ecf5ff; }

.link-text:hover { text-decoration: underline; }
</style>