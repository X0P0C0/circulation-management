<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件入库</h2>
    </div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="扫码或手动输入条码">
            <template #append>
              <el-button @click="handleScan">扫码</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="配件名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配件名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.spec" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="个/件/套" style="width: 120px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认入库</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { accessoryInbound } from '@/api/accessory'
import { getCategories } from '@/api/category'

const formRef = ref()
const loading = ref(false)
const categoryList = ref([])
const form = reactive({
  barcode: '', name: '', spec: '', categoryId: null, unit: '个', remark: ''
})
const rules = {
  barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入配件名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const handleScan = () => {
  ElMessage.info('请使用H5端扫码，或手动输入条码')
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await accessoryInbound(form)
    ElMessage.success('入库成功')
    resetForm()
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

onMounted(async () => {
  try {
    const { data } = await getCategories()
    categoryList.value = data
  } catch (e) { /* ignore */ }
})
</script>