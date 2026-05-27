<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件售卖</h2>
    </div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" style="max-width: 650px">
        <el-form-item label="条码" prop="barcode">
          <BarcodeScanner v-model="form.barcode" placeholder="输入或识别条码" @scanned="onBarcodeScanned" />
        </el-form-item>
        <el-form-item v-if="accessoryInfo" label="配件信息">
          <el-descriptions :column="2" border size="small" style="width: 100%">
            <el-descriptions-item label="名称">{{ accessoryInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ accessoryInfo.spec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ accessoryInfo.categoryName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可用库存">
              <el-tag :type="accessoryInfo.availableQty > 0 ? 'success' : 'danger'" size="small">
                {{ accessoryInfo.availableQty }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="form.customerName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="客户电话">
          <el-input v-model="form.customerPhone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
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
import BarcodeScanner from '@/components/BarcodeScanner.vue'

const formRef = ref()
const loading = ref(false)
const accessoryInfo = ref(null)
const form = reactive({ barcode: '', customerName: '', customerPhone: '', remark: '' })
const rules = { barcode: [{ required: true, message: '请输入条码', trigger: 'blur' }] }

const onBarcodeScanned = async (code) => {
  form.barcode = code
  await loadAccessory()
}

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
    ElMessage.warning('该配件库存不足，无法售卖')
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

<style scoped>
.page-container { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; }
</style>