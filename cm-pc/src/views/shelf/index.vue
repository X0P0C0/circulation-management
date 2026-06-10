<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">货架管理</h2>
      <el-button type="primary" @click="openDialog()">新增货架</el-button>
    </div>
    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <el-input v-model="keyword" placeholder="搜索货架名称或位置" clearable style="width: 260px" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>
    <el-card>
      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="货架名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该货架？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑货架' : '新增货架'" width="450px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入货架名称" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="选填，如A区-3排-2层" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getShelves, createShelf, updateShelf, deleteShelf } from '@/api/shelf'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ name: '', location: '', remark: '' })
const formRules = { name: [{ required: true, message: '请输入货架名称', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getShelves({ keyword: keyword.value || undefined })
    list.value = data
  } catch (e) { ElMessage.error("操作失败: " + (e.response?.data?.message || e.message)) } finally { loading.value = false }
}

const openDialog = (row) => {
  editId.value = row?.id || null
  Object.assign(form, row || { name: '', location: '', remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) { await updateShelf(editId.value, form) }
    else { await createShelf(form) }
    ElMessage.success(editId.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error("操作失败: " + (e.response?.data?.message || e.message)) } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await deleteShelf(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { ElMessage.error("操作失败: " + (e.response?.data?.message || e.message)) }
}

onMounted(() => loadData())
</script>
