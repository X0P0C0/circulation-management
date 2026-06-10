<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">师傅管理</h2>
      <el-button type="primary" @click="openDialog()">新增师傅</el-button>
    </div>
    <el-card>
      <div class="search-bar" style="margin-bottom: 16px; display: flex; gap: 12px">
        <el-input v-model="keyword" placeholder="搜索姓名/电话" clearable style="width: 250px"
          @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="list" stripe border v-loading="loading" :sort-multiple="true" @sort-change="handleSortChange">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="姓名" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="jobNo" label="工号" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="phone" label="电话" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link type="success" @click="viewInventory(row)">库存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper" style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑师傅' : '新增师傅'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入师傅姓名" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.jobNo" placeholder="选填" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="750px">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <span style="font-size:16px;font-weight:600">{{ drawerTitle }}</span>
          <el-radio-group v-model="inventoryView" size="small">
            <el-radio-button value="detail">明细</el-radio-button>
            <el-radio-button value="summary">汇总</el-radio-button>
          </el-radio-group>
          <el-button v-if="inventoryFilter" size="small" @click="inventoryFilter = null">返回汇总</el-button>
        </div>
      </template>
      <el-table v-if="inventoryView === 'summary' && !inventoryFilter" :data="inventoryGrouped" stripe border empty-text="暂无配件"
        highlight-current-row :row-style="{ cursor: 'pointer' }" @row-click="onInventoryGroupClick">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="barcode" label="条码" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="count" label="数量" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-table v-else :data="inventoryFilteredItems" stripe border empty-text="暂无配件">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="itemCode" label="工件编号" width="180" />
        <el-table-column prop="barcode" label="条码" />
        <el-table-column prop="categoryName" label="分类" width="120" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMultiSort } from '@/components/multi-sort/useMultiSort'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkers, createWorker, updateWorker, deleteWorker } from '@/api/worker'
import { searchAccessories } from '@/api/accessory'

const list = ref([])
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
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ name: '', jobNo: '', phone: '', remark: '' })
const formRules = { name: [{ required: true, message: '请输入师傅姓名', trigger: 'blur' }] }
const drawerVisible = ref(false)
const drawerTitle = ref('')
const workerItems = ref([])
const inventoryView = ref('detail')
const inventoryFilter = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getWorkers({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value, ...buildSortParams() })
    list.value = applyLocalSort(data.records)
    total.value = data.total
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

const openDialog = (row) => {
  editId.value = row?.id || null
  Object.assign(form, row || { name: '', jobNo: '', phone: '', remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) { await updateWorker(editId.value, form) }
    else { await createWorker(form) }
    ElMessage.success(editId.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除师傅"' + row.name + '"？', '提示', { type: 'warning' })
  try {
    await deleteWorker(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

const viewInventory = async (row) => {
  drawerTitle.value = row.name + ' - 持有工件'
  inventoryView.value = 'summary'
  inventoryFilter.value = null
  drawerVisible.value = true
  try {
    const { data } = await searchAccessories({ workerId: row.id, status: 2, pageNum: 1, pageSize: 100 })
    workerItems.value = data.records
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

const inventoryGrouped = computed(() => {
  const map = {}
  workerItems.value.forEach(item => {
    if (!map[item.barcode]) map[item.barcode] = { barcode: item.barcode, categoryName: item.categoryName, count: 0 }
    map[item.barcode].count++
  })
  return Object.values(map)
})

const inventoryFilteredItems = computed(() => {
  if (!inventoryFilter.value) return workerItems.value
  return workerItems.value.filter(item => item.barcode === inventoryFilter.value)
})

const onInventoryGroupClick = (row) => {
  inventoryFilter.value = row.barcode
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
</style>
