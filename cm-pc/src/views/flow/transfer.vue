<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库存转移</h2>
    </div>
    <el-card>
      <el-form label-width="100px" class="transfer-form">
        <el-form-item label="源师傅" required>
          <el-select v-model="fromWorkerId" placeholder="选择源师傅" filterable style="width: 100%"
            @change="onFromWorkerChange">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标师傅" required>
          <el-select v-model="toWorkerId" placeholder="选择目标师傅" filterable style="width: 100%">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id"
              :disabled="w.id === fromWorkerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择工件">
          <el-button type="primary" :disabled="!fromWorkerId" @click="openPicker">添加工件</el-button>
          <span v-if="!fromWorkerId" style="margin-left: 8px; color: #94a3b8; font-size: 13px">请先选择源师傅</span>
        </el-form-item>
        <el-form-item v-if="selectedItems.length" label="转移清单">
          <el-table :data="selectedItems" stripe border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="itemCode" label="工件编号" width="180" />
            <el-table-column prop="barcode" label="条码" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="selectedItems.splice($index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="remark" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" :disabled="!fromWorkerId || !toWorkerId || !selectedItems.length"
            @click="handleSubmit">
            确认转移（{{ selectedItems.length }}件）
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ItemPicker ref="pickerRef" title="选择要转移的工件" :multiple="true" :default-status="2"
      :show-status="false" :worker-id="fromWorkerId" :exclude-ids="selectedItems.map(i => i.id)"
      @confirm="onPickerConfirm" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllWorkers } from '@/api/worker'
import { flowTransfer } from '@/api/flow'
import ItemPicker from '@/components/ItemPicker.vue'

const fromWorkerId = ref(null)
const toWorkerId = ref(null)
const workerList = ref([])
const selectedItems = ref([])
const remark = ref('')
const loading = ref(false)
const pickerRef = ref()

const onFromWorkerChange = () => {
  selectedItems.value = []
  if (toWorkerId.value === fromWorkerId.value) toWorkerId.value = null
}

const openPicker = () => { pickerRef.value.open() }

const onPickerConfirm = (items) => {
  items.forEach(item => {
    if (!selectedItems.value.find(s => s.id === item.id)) {
      selectedItems.value.push(item)
    }
  })
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await flowTransfer({
      fromWorkerId: fromWorkerId.value,
      toWorkerId: toWorkerId.value,
      accessoryIds: selectedItems.value.map(i => i.id),
      remark: remark.value
    })
    ElMessage.success('转移成功')
    selectedItems.value = []
    remark.value = ''
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(async () => {
  try {
    const { data } = await getAllWorkers()
    workerList.value = data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.transfer-form { max-width: 900px; }
.transfer-form :deep(.el-form-item) { margin-bottom: 22px; }
</style>