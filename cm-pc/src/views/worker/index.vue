<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">Workers</h2>
      <el-button type="primary" @click="openDialog()">Add Worker</el-button>
    </div>
    <el-card>
      <div class="search-bar" style="margin-bottom: 16px">
        <el-input v-model="keyword" placeholder="Search name/phone" clearable style="width: 250px"
          @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData" style="margin-left: 12px">Search</el-button>
      </div>
      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="Name" />
        <el-table-column prop="jobNo" label="Job No" />
        <el-table-column prop="phone" label="Phone" />
        <el-table-column prop="remark" label="Remark" show-overflow-tooltip />
        <el-table-column label="Actions" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">Edit</el-button>
            <el-button link type="danger" @click="handleDelete(row)">Delete</el-button>
            <el-button link type="success" @click="viewInventory(row)">Stock</el-button>
            <el-button link type="info" @click="viewRecords(row)">Records</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? 'Edit Worker' : 'Add Worker'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="Worker name" />
        </el-form-item>
        <el-form-item label="Job No">
          <el-input v-model="form.jobNo" placeholder="Optional" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="form.phone" placeholder="Optional" />
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">OK</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="600px">
      <el-tabs v-model="drawerTab">
        <el-tab-pane label="Inventory" name="inventory">
          <el-table :data="workerInventory" stripe border empty-text="No items">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="barcode" label="Barcode" width="180" />
            <el-table-column prop="accessoryName" label="Name" />
            <el-table-column prop="spec" label="Spec" width="120" />
            <el-table-column prop="availableQty" label="Qty" width="80" align="center" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="Records" name="records">
          <el-table :data="records" stripe border empty-text="No records">
            <el-table-column prop="barcode" label="Barcode" width="180" />
            <el-table-column prop="accessoryName" label="Name" />
            <el-table-column prop="flowType" label="Type" width="80">
              <template #default="{ row }">
                <el-tag :type="row.flowType===2?'primary':row.flowType===3?'warning':'info'" size="small">
                  {{ {1:'Inbound',2:'Out',3:'Return',4:'Sell'}[row.flowType] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="Time" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkers, createWorker, updateWorker, deleteWorker, getWorkerInventory } from '@/api/worker'
import { getWorkerRecords } from '@/api/flow'

const list = ref([])
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
const formRules = { name: [{ required: true, message: 'Required', trigger: 'blur' }] }
const drawerVisible = ref(false)
const drawerTitle = ref('')
const drawerTab = ref('inventory')
const records = ref([])
const workerInventory = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getWorkers({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = data.records
    total.value = data.total
  } catch (e) { /* handled */ } finally { loading.value = false }
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
    ElMessage.success(editId.value ? 'Updated' : 'Created')
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('Delete worker "' + row.name + '"?', 'Confirm', { type: 'warning' })
  try {
    await deleteWorker(row.id)
    ElMessage.success('Deleted')
    loadData()
  } catch (e) { /* handled */ }
}

const viewInventory = async (row) => {
  drawerTitle.value = row.name + ' - Inventory'
  drawerTab.value = 'inventory'
  drawerVisible.value = true
  try {
    const { data } = await getWorkerInventory(row.id)
    workerInventory.value = data
  } catch (e) { /* handled */ }
}

const viewRecords = async (row) => {
  drawerTitle.value = row.name + ' - Records'
  drawerTab.value = 'records'
  drawerVisible.value = true
  try {
    const { data } = await getWorkerRecords(row.id)
    records.value = data
  } catch (e) { /* handled */ }
}

onMounted(() => loadData())
</script>