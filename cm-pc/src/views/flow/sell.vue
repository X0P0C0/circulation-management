<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">配件售卖</h2></div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="输入条码" @blur="loadAccessory">
            <template #append><el-button @click="loadAccessory">查询</el-button></template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="accessoryInfo" label="配件信息">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="名称">{{ accessoryInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ accessoryInfo.spec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ accessoryInfo.categoryName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可用库存">{{ accessoryInfo.availableQty }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="form.customerName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="客户电话">
          <el-input v-model="form.customerPhone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认售卖</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccessoryByBarcode } from '@/api/accessory'
import { sellAccessory } from '@/api/flow'

const formRef = ref()
const loading = ref(false)
const accessoryInfo = ref(null)
const form = reactive({ barcode: '', customerName: '', customerPhone: '', remark: '' })
const rules = { barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }] }

const loadAccessory = async () => {
  if (!form.barcode) return
  try {
    const { data } = await getAccessoryByBarcode(form.barcode)
    accessoryInfo.value = data
  } catch (e) { accessoryInfo.value = null }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (accessoryInfo.value && accessoryInfo.value.availableQty < 1) {
    ElMessage.warning('库存不足')
    return
  }
  loading.value = true
  try {
    await sellAccessory(form)
    ElMessage.success('售卖成功')
    resetForm()
  } catch (e) { /* handled */ } finally { loading.value = false }
}

const resetForm = () => {
  formRef.value?.resetFields()
  accessoryInfo.value = null
}
</script>