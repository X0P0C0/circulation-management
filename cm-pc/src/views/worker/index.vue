<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">师傅管理</h2>
      <el-button type="primary" @click="openDialog()">新增师傅</el-button>
    </div>
    <el-card>
      <div class="search-bar" style="margin-bottom: 16px">
        <el-input v-model="keyword" placeholder="搜索姓名/工号/电话" clearable style="width: 250px"
          @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData" style="margin-left: 12px">查询</el-button>
      </div>
      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="jobNo" label="工号" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link type="info" @click="viewRecords(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
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
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    <el-drawer v-model="drawerVisible" title="师傅领用记录" size="600px">
      <el-table :data="records" stripe border>
        <el-table-column prop="barcode" label="条码" width="180" />
        <el-table-column prop="accessoryName" label="配件名称" />
        <el-table-column prop="flowType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.flowType===2?'primary':row.flowType===3?'warning':'info'" size="small">
              {{ {2:'领用',3:'归还'}[row.flowType] || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkers, createWorker, updateWorker, deleteWorker } from '@/api/worker'
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
const formRules = { name: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }
const drawerVisible = ref(false)
const records = ref([])

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
    ElMessage.success(editId.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除师傅 "' + row.name + '" ？', '提示', { type: 'warning' })
  try {
    await deleteWorker(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

const viewRecords = async (row) => {
  try {
    const { data } = await getWorkerRecords(row.id)
    records.value = data
    drawerVisible.value = true
  } catch (e) { /* handled */ }
}

onMounted(() => loadData())
</script>