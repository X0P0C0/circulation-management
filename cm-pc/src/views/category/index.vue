<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">分类管理</h2>
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>
    <el-card>
      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新增分类'" width="450px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ name: '', sort: 0, remark: '' })
const formRules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getCategories()
    list.value = data
  } catch (e) { /* handled */ } finally { loading.value = false }
}

const openDialog = (row) => {
  editId.value = row?.id || null
  Object.assign(form, row || { name: '', sort: 0, remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editId.value) { await updateCategory(editId.value, form) }
    else { await createCategory(form) }
    ElMessage.success(editId.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除分类 "' + row.name + '" ？', '提示', { type: 'warning' })
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(() => loadData())
</script>