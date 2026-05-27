<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件领用</h2>
    </div>
    <el-card>
      <el-form label-width="100px" style="max-width: 700px">
        <el-form-item label="选择师傅" required>
          <el-select v-model="workerId" placeholder="请选择领用师傅" filterable style="width: 100%">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="条码录入">
          <el-input v-model="barcodeInput" placeholder="输入条码后回车添加" @keyup.enter="addBarcode">
            <template #append>
              <el-button @click="addBarcode">添加</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="待领用列表">
          <el-table :data="barcodeList" stripe border style="width: 100%" empty-text="暂无配件，请扫码或输入条码添加">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="barcode" label="条码" />
            <el-table-column prop="name" label="配件名称" />
            <el-table-column prop="availableQty" label="可用库存" width="100" />
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button link type="danger" @click="barcodeList.splice($index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="remark" placeholder="备注信息" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" :disabled="!workerId || barcodeList.length === 0" @click="handleSubmit">
            确认领用 ({{ barcodeList.length }}件)
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllWorkers } from '@/api/worker'
import { getAccessoryByBarcode } from '@/api/accessory'
import { transferOut } from '@/api/flow'

const workerId = ref(null)
const workerList = ref([])
const barcodeInput = ref('')
const barcodeList = ref([])
const remark = ref('')
const loading = ref(false)

const addBarcode = async () => {
  const barcode = barcodeInput.value.trim()
  if (!barcode) return
  if (barcodeList.value.find(b => b.barcode === barcode)) {
    ElMessage.warning('该条码已添加')
    barcodeInput.value = ''
    return
  }
  try {
    const { data } = await getAccessoryByBarcode(barcode)
    barcodeList.value.push(data)
    barcodeInput.value = ''
  } catch (e) { /* handled */ }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await transferOut({
      workerId: workerId.value,
      barcodes: barcodeList.value.map(b => b.barcode),
      remark: remark.value
    })
    ElMessage.success('领用成功')
    barcodeList.value = []
    remark.value = ''
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await getAllWorkers()
    workerList.value = data
  } catch (e) { /* ignore */ }
})
</script>