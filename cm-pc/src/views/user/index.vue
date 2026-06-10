<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="openDialog()">新增用户</el-button>
    </div>

    <el-card>
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center;">
        <el-input v-model="keyword" placeholder="搜索用户名/姓名" clearable style="width: 250px" @keyup.enter="fetchUsers" />
        <el-button type="primary" @click="pageNum = 1; fetchUsers()">查询</el-button>
        <el-button @click="keyword = ''; pageNum = 1; fetchUsers()">重置</el-button>
      </div>
        <el-table :data="userList" stripe border style="width: 100%" :sort-multiple="true" @sort-change="handleSortChange">
        <el-table-column prop="id" label="编号" width="80" align="center" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="username" label="用户名" width="150" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="realName" label="姓名" width="120" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="roleName" label="角色" width="100" align="center" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
              {{ row.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)"
              :disabled="row.username === 'admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <span class="page-label">每页</span>
        <el-input v-model.number="pageSizeInput" size="small" class="page-size-input" @change="onPageSizeChange" />
        <span class="page-label">条</span>
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50,100]"
          layout="total, prev, pager, next, jumper" @change="fetchUsers" background />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="450px"
      :close-on-click-modal="false" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password
            :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :value="1">管理员</el-radio>
            <el-radio :value="2">操作员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMultiSort } from '@/components/multi-sort/useMultiSort'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userList = ref([])
const keyword = ref('')
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
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
  fetchUsers()
}
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 2
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

const fetchUsers = async () => {
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, ...buildSortParams() }
    if (keyword.value) params.keyword = keyword.value
    const { data } = await request.get('/api/user/page', { params })
    userList.value = applyLocalSort(data.records)
    total.value = data.total
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

const onPageSizeChange = () => {
  const v = Number(pageSizeInput.value)
  if (v && v > 0) { pageSize.value = v; pageNum.value = 1; fetchUsers() }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    Object.assign(form, {
      username: row.username,
      password: '',
      realName: row.realName,
      role: row.role
    })
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, { username: '', password: '', realName: '', role: 2 })
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      const payload = { realName: form.realName, role: form.role }
      if (form.password) payload.password = form.password
      await request.put(`/api/user/${editId.value}`, payload)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/user', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally {
    submitLoading.value = false
  }
}

const handleToggle = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户"${row.username}"？`, '提示', { type: 'warning' })
  try {
    await request.put(`/api/user/${row.id}/toggle`)
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除用户"${row.username}"？此操作不可恢复。`, '警告', { type: 'error' })
  try {
    await request.delete(`/api/user/${row.id}`)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) }
}

onMounted(() => { fetchUsers() })
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 18px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 28px;
}
.pagination-wrapper .page-label {
  font-size: 12px;
  color: #909399;
  margin: 0 4px;
  line-height: 28px;
}
.pagination-wrapper .page-size-input {
  width: 52px;
}
.pagination-wrapper :deep(.el-pagination) {
  margin-left: 12px;
  height: 28px;
  display: flex;
  align-items: center;
}
</style>
