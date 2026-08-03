<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title" style="cursor:pointer" @click="backToSummary">总库{{ (filters.exactBarcode || filters.barcode) ? " - " + (filters.exactBarcode || filters.barcode) : "（全部工件）" }}</h2>
      <div class="header-actions">
        <el-button v-if="!showGrouped" size="small" @click="backToSummary">&larr; 返回汇总</el-button>
        <el-radio-group v-model="showGrouped" size="small" @change="loadData">
          <el-radio-button :value="false">明细</el-radio-button>
          <el-radio-button :value="true">汇总</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" @click="loadData">查询</el-button>
        <el-button size="small" @click="resetFilters">重置</el-button>
        <el-button v-if="!showGrouped" size="small" type="danger" :plain="!deleteMode" @click="toggleDeleteMode">{{ deleteMode ? '取消删除' : '删除' }}</el-button>
        <el-button size="small" @click="handleExport">导出</el-button>
        <el-button size="small" type="primary" @click="showImportDialog = true">批量导入</el-button>
      </div>
    </div>
    <div v-if="deleteMode" class="delete-bar">
      <span>已进入删除模式，勾选要删除的工件</span>
      <el-button size="small" type="danger" :loading="deleteLoading" @click="handleBatchDelete">确认删除</el-button>
    </div>
    <el-card>
      <div class="search-area">
        <div class="search-row">
          <div class="search-item">
            <label class="search-label">条码</label>
            <el-input v-model="filters.barcode" clearable placeholder="请输入" size="small" @keyup.enter="loadData" />
          </div>
          <div class="search-item">
            <label class="search-label">工件编号</label>
            <el-input v-model="filters.itemCode" clearable placeholder="请输入" size="small" @keyup.enter="loadData" />
          </div>
          <div class="search-item">
            <label class="search-label">持有者</label>
            <el-select v-model="filters.owner" clearable placeholder="全部" size="small" @change="loadData" @focus="loadWorkers">
              <el-option label="全部" value="" />
              <el-option label="总部" value="hq" />
              <el-option v-for="w in workers" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </div>
          <div class="search-item">
            <label class="search-label">分类</label>
            <CategoryCascader v-model="filters.categoryId" placeholder="选择分类筛选" />
          </div>
          <div class="search-item">
            <label class="search-label">货架</label>
            <CustomSelect v-model="filters.shelfId" :options="shelfList" placeholder="全部" labelKey="name" extraKey="location" />
          </div>
          <div class="search-item">
            <label class="search-label">状态</label>
            <el-select v-model="filters.status" clearable placeholder="全部" size="small" @change="loadData">
              <el-option label="在库" :value="1" />
              <el-option label="已出库" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="寄回厂家" :value="4" />
              <el-option label="旧件待返厂" :value="5" />
              <el-option label="已售出" :value="6" />
            </el-select>
          </div>
          <div class="search-item">
            <label class="search-label">高价值</label>
            <el-select v-model="filters.highValue" clearable placeholder="全部" size="small" @change="loadData">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </div>
          <div class="search-item">
            <label class="search-label">备注</label>
            <el-input v-model="filters.remark" clearable placeholder="请输入" size="small" @keyup.enter="loadData" />
          </div>
          <div class="search-item search-item--date">
            <label class="search-label">入库时间</label>
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-"
              start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"
              @change="handleDateChange" clearable size="small" />
          </div>
        </div>
      </div>

      <el-table v-if="showGrouped" :data="groupList" stripe border v-loading="loading"
        @row-click="onGroupRowClick" highlight-current-row :row-style="{ cursor: 'pointer' }">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="barcode" label="条码" width="128" sortable />
        <el-table-column prop="categoryName" label="分类" width="160" sortable>
          <template #default="{ row }">
            {{ row.categoryName }}<span v-if="row.partNumber" style="color:#94a3b8;font-size:12px;margin-left:4px">({{ row.partNumber }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数" width="100" align="center" sortable />
        <el-table-column prop="availableCount" label="可用" width="100" align="center" sortable>
          <template #default="{ row }">
            <el-tag :type="row.availableCount > 0 ? 'success' : 'info'" size="small">{{ row.availableCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="filteredList" stripe border v-loading="loading" style="width:100%" @row-click="onItemClick" highlight-current-row :row-style="{ cursor: 'pointer' }" @selection-change="onSelectionChange">
        <el-table-column v-if="deleteMode" type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="itemCode" label="工件编号" width="124" sortable />
        <el-table-column prop="barcode" label="条码" width="128" sortable />
        <el-table-column prop="categoryName" label="分类" width="160" sortable>
          <template #default="{ row }">
            {{ row.categoryName }}<span v-if="row.partNumber" style="color:#94a3b8;font-size:12px;margin-left:4px">({{ row.partNumber }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="110" align="center" sortable>
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="highValue" label="高价值" width="88" align="center" sortable>
          <template #default="{ row }">
            <el-tag :type="row.highValue ? 'danger' : 'info'" size="small">{{ row.highValue ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerName" label="持有者" width="100" sortable>
          <template #default="{ row }">{{ row.workerName || '总部' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="shelfName" label="货架" width="140" sortable>
          <template #default="{ row }">{{ row.shelfName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="入库时间" width="170" sortable>
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <span class="page-label">每页</span>
        <el-input v-model="pageSizeInput" class="page-size-input" size="small" @change="handlePageSizeChange" />
        <span class="page-label">条，共 {{ total }} 条</span>
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
          layout="prev, pager, next" @current-change="loadData" size="small" />
      </div>
    </el-card>

    <el-dialog v-model="showImportDialog" title="批量导入工件" width="560px" :close-on-click-modal="true">
      <div style="margin-bottom: 16px;">
        <p style="font-size: 13px; color: #606266; margin-bottom: 8px;">支持 CSV 或 Excel 文件，格式：专用号, 名称, 数量, 货架, 备注</p>
        <el-table :data="exampleData" size="small" stripe style="margin-bottom: 12px;" :show-header="true" max-height="200">
          <el-table-column prop="barcode" label="专用号" width="160" />
          <el-table-column prop="qty" label="数量" width="60" align="center" />
          <el-table-column prop="category" label="分类名称" width="120" />
          <el-table-column prop="shelf" label="货架" width="100" />
          <el-table-column prop="remark" label="备注" min-width="100" />
        </el-table>
        <el-button size="small" @click="downloadTemplate">下载模板</el-button>
      </div>
      <el-form label-width="80px" size="small">
        <el-form-item label="选择文件">
          <input type="file" accept=".csv,.xlsx,.xls" @change="onFileChange" />
        </el-form-item>
      </el-form>
      <div v-if="importResult" style="margin-top: 12px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
        <p style="font-weight: 500;">导入结果：</p>
        <p>成功导入 <span style="color: #67c23a; font-weight: bold;">{{ importResult.success }}</span> 件</p>
        <p v-if="importResult.fail > 0">失败 <span style="color: #f56c6c; font-weight: bold;">{{ importResult.fail }}</span> 件</p>
      </div>
      <template #footer>
        <el-button size="small" @click="showImportDialog = false; importResult = null">取消</el-button>
        <el-button size="small" type="primary" :loading="importing" :disabled="!selectedFile" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑工件" width="600px" class="edit-dialog" :close-on-click-modal="true">
      <el-form label-width="80px" size="small">
        <el-form-item label="工件编号">
          <el-input v-model="editForm.itemCode" disabled />
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="editForm.barcode" />
        </el-form-item>
        <el-form-item label="分类">
          <CategoryCascader v-model="editForm.categoryId" placeholder="请选择分类" />
        </el-form-item>
        <el-form-item label="高价值">
          <el-radio-group v-model="editForm.isHighValue">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="货架">
          <CustomSelect v-model="editForm.shelfId" :options="shelfList" placeholder="选择货架" labelKey="name" extraKey="location" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="goToTrace(editForm.itemCode)">追溯</el-button>
        <el-button size="small" @click="showEditDialog = false">取消</el-button>
        <el-button size="small" type="primary" :loading="editing" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { exportCsv, formatDate } from '@/utils/export'
import { searchAccessories, getInventoryGroup, importAccessories, importExcel, deleteAccessory, updateAccessory } from '@/api/accessory'
import { getCategories } from '@/api/category'
import { getAllWorkers } from '@/api/worker'
import { getShelves } from '@/api/shelf'
import CategoryCascader from '@/components/CategoryCascader.vue'
import CustomSelect from '@/components/CustomSelect.vue'

const router = useRouter()
const showGrouped = ref(true)
const loading = ref(false)
const itemList = ref([])
const groupList = ref([])
const filteredList = computed(() => itemList.value)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
const categories = ref([])
const workers = ref([])
const shelfList = ref([])
const dateRange = ref(null)

const filters = reactive({
  barcode: '', exactBarcode: '', itemCode: '', owner: '', categoryId: null, shelfId: null,
  status: null, highValue: null, remark: '', operator: ''
})

const statusTag = (s) => ({ 1:'success', 2:'primary', 3:'info', 4:'warning', 5:'warning', 6:'danger' }[s] || 'info')

const resetFilters = () => {
  filters.barcode = ''; filters.itemCode = ''; filters.owner = ''
  filters.categoryId = null; filters.shelfId = null; filters.status = null
  filters.highValue = null; filters.remark = ''; filters.operator = ''
  dateRange.value = null; pageNum.value = 1; loadData()
}

const handleDateChange = () => { pageNum.value = 1; loadData() }
const handlePageSizeChange = () => { const v = Number(pageSizeInput.value); if (v && v > 0) { pageSize.value = v; pageNum.value = 1; loadData() } }

const backToSummary = () => { filters.barcode = ''; filters.exactBarcode = ''; filters.categoryId = null; filters.itemCode = ''; showGrouped.value = true; pageNum.value = 1; loadData() }

const onGroupRowClick = (row) => {
  filters.exactBarcode = row.barcode || ''
  filters.categoryId = row.categoryId
  showGrouped.value = false
  pageNum.value = 1
  loadData()
}

const loadWorkers = async () => { try { const { data } = await getAllWorkers(); workers.value = data } catch (e) { /* */ } }

const loadData = async () => {
  loading.value = true
  try {
    const baseParams = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.exactBarcode) { baseParams.exactBarcode = filters.exactBarcode } else if (filters.barcode) { baseParams.barcode = filters.barcode }
    if (filters.categoryId) baseParams.categoryId = filters.categoryId
    if (filters.remark) baseParams.remark = filters.remark
    if (filters.highValue !== null && filters.highValue !== '') baseParams.highValue = filters.highValue
    if (filters.shelfId) baseParams.shelfId = filters.shelfId
    if (dateRange.value && dateRange.value.length === 2) {
      baseParams.startDate = dateRange.value[0]
      baseParams.endDate = dateRange.value[1]
    }
    if (showGrouped.value) {
      if (filters.itemCode && !filters.barcode) baseParams.barcode = filters.itemCode
      if (filters.owner === 'hq') {
        baseParams.statusFilter = filters.status || 1
      } else if (filters.owner && typeof filters.owner === 'number') {
        baseParams.workerId = filters.owner
      } else if (filters.status) {
        baseParams.statusFilter = filters.status
      }
      const { data } = await getInventoryGroup(baseParams)
      groupList.value = data.records
      total.value = data.total
    } else {
      if (filters.itemCode) baseParams.keyword = filters.itemCode
      if (filters.owner === 'hq') {
        baseParams.status = filters.status || 1
      } else if (filters.owner && typeof filters.owner === 'number') {
        baseParams.workerId = filters.owner
        if (filters.status) baseParams.status = filters.status
      } else if (filters.status) {
        baseParams.status = filters.status
      }
      const { data } = await searchAccessories(baseParams)
      itemList.value = data.records
      total.value = data.total
    }
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

// Delete mode
const deleteMode = ref(false)
const deleteLoading = ref(false)
const selectedItems = ref([])
const onSelectionChange = (rows) => { selectedItems.value = rows }
const toggleDeleteMode = () => { deleteMode.value = !deleteMode.value; if (!deleteMode.value) selectedItems.value = [] }
const handleBatchDelete = async () => {
  if (selectedItems.value.length === 0) { ElMessage.warning('请选择要删除的工件'); return }
  await ElMessageBox.confirm('确定删除选中的 ' + selectedItems.value.length + ' 个工件？此操作不可恢复。', '确认删除', { type: 'warning' })
  deleteLoading.value = true
  try {
    for (const item of selectedItems.value) { await deleteAccessory(item.id) }
    ElMessage.success('批量删除成功'); deleteMode.value = false; loadData()
  } catch (e) { console.error(e) } finally { deleteLoading.value = false }
}

// Export
const handleExport = () => {
  const rows = showGrouped.value ? groupList.value : itemList.value
  if (showGrouped.value) {
    exportCsv(['条码', '分类', '总数', '可用'], rows.map(r => [r.barcode, r.categoryName, r.totalCount, r.availableCount]), '总库汇总')
  } else {
    exportCsv(['工件编号', '条码', '分类', '状态', '持有者', '货架', '入库时间'],
      rows.map(r => [r.itemCode, r.barcode, r.categoryName, r.statusDesc, r.workerName || '总部', r.shelfName || '-', formatDate(r.createTime)]), '总库明细')
  }
}

// Import
const showImportDialog = ref(false)
const selectedFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const exampleData = [{ barcode: '0011800284AJ', qty: 5, category: '内电脑板', shelf: '3-1', remark: '' }]
const onFileChange = (e) => { selectedFile.value = e.target.files[0] }
const downloadTemplate = () => {
  const csv = '专用号,数量,分类名称,货架,备注\n0011800284AJ,5,内电脑板,3-1,\n'
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = '导入模板.csv'; a.click()
}
const handleImport = async () => {
  if (!selectedFile.value) return
  importing.value = true; importResult.value = null
  try {
    const formData = new FormData(); formData.append('file', selectedFile.value)
    const fileName = selectedFile.value.name.toLowerCase()
    let res
    if (fileName.endsWith('.xlsx') || fileName.endsWith('.xls')) {
      res = await importExcel(formData)
    } else {
      res = await importAccessories(formData)
    }
    importResult.value = { success: res.data.success || res.data.length || 0, fail: res.data.fail || 0 }
    selectedFile.value = null; loadData()
  } catch (e) { importResult.value = { success: 0, fail: 1 }; console.error(e) } finally { importing.value = false }
}

// Edit
const showEditDialog = ref(false)
const editing = ref(false)
const editForm = reactive({ id: null, itemCode: '', barcode: '', categoryId: null, shelfId: null, remark: '', isHighValue: 0 })
const openEdit = (row) => {
  editForm.id = row.id; editForm.itemCode = row.itemCode; editForm.barcode = row.barcode
  editForm.categoryId = row.categoryId; editForm.shelfId = row.shelfId || null
  editForm.remark = row.remark || ''; editForm.isHighValue = row.isHighValue || 0
  showEditDialog.value = true
}
const handleEditSubmit = async () => {
  if (!editForm.barcode) { ElMessage.warning('请输入条码'); return }
  editing.value = true
  try {
    await updateAccessory({ id: editForm.id, barcode: editForm.barcode, categoryId: editForm.categoryId, shelfId: editForm.shelfId, remark: editForm.remark, isHighValue: editForm.isHighValue })
    ElMessage.success('修改成功'); showEditDialog.value = false; loadData()
  } catch (e) { console.error(e) } finally { editing.value = false }
}
const goToTrace = (itemCode) => { showEditDialog.value = false; router.push({ path: '/flow/trace', query: { itemCode } }) }
const onItemClick = (row) => { if (!deleteMode.value) openEdit(row) }

onMounted(async () => {
  loadData()
  try { const { data } = await getCategories(); categories.value = data } catch (e) { /* */ }
  loadWorkers()
  try { const { data } = await getShelves(); shelfList.value = data || [] } catch (e) { /* */ }
})
</script>

<style scoped>
.search-area { width: 100%; min-height: 70px; padding: 12px 20px; margin: 0 0 16px; box-sizing: border-box; font-size: 16px; border-radius: 6px; background: rgb(248, 249, 251); border: 1px solid #e4e7ed; }
.search-row { display: flex; flex-wrap: wrap; align-items: center; gap: 10px 14px; }
.search-item { display: inline-flex; align-items: center; gap: 6px; flex-shrink: 1; }
.search-label { font-size: 12px; font-weight: 500; color: #606266; white-space: nowrap; flex-shrink: 0; }
.search-item .el-input, .search-item .el-select, .search-item .category-cascader, .search-item .custom-select { width: 116px; }
.search-item--date .el-date-editor { width: 240px; }
.header-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.delete-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: #fef0f0; border: 1px solid #fde2e2; border-radius: 4px; margin-bottom: 12px; font-size: 13px; color: #f56c6c; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; height: 28px; }
.pagination-wrapper .page-label { font-size: 12px; color: var(--text-secondary); margin: 0 4px; line-height: 28px; }
.pagination-wrapper .page-size-input { width: 52px; }
.pagination-wrapper :deep(.el-input--small) { height: 24px; }
.pagination-wrapper :deep(.el-pagination) { margin-left: 12px; height: 28px; display: flex; align-items: center; }
.page-container :deep(.el-table__empty-block) { width: 100%; height: 350px; padding: 0; margin: 0; font-size: 14px; }
.page-container :deep(.el-card__body) { width: 100%; height: auto; min-height: 515px; padding: 20px; margin: 0; font-size: 16px; border-radius: 0; box-sizing: border-box; }

/* Edit dialog - wider selectors (scoped, does not affect other pages) */
</style>
<style>
/* Global styles for edit dialog (teleported outside scoped component) */
.edit-dialog .cascader-trigger { width: 440px !important; }
.edit-dialog .select-trigger { width: 442px !important; }

/* Edit dialog shelf dropdown - expand UPWARD */
.edit-dialog .custom-select .select-panel {
  top: auto !important;
  bottom: 100% !important;
  margin-top: 0 !important;
  margin-bottom: 4px !important;
}

/* Edit dialog cascader panel - ensure not clipped */
.edit-dialog .category-cascader .cascader-panel {
  z-index: 10000 !important;
}

/* Edit dialog shelf panel - high z-index */
.edit-dialog .custom-select .select-panel {
  z-index: 10000 !important;
  width: 442px !important;
}
</style>
