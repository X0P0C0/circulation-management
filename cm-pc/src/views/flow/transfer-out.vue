<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件出库</h2>
    </div>
    <el-card>
      <el-form label-width="100px" class="outbound-form">
        <el-form-item label="选择师傅" required>
          <el-select v-model="workerId" placeholder="请选择出库师傅" filterable style="width: 100%" @focus="loadWorkerData">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择工件">
          <el-button type="primary" @click="openPicker">添加工件</el-button>
        </el-form-item>
        <el-form-item v-if="selectedItems.length" label="出库清单">
          <div style="margin-bottom: 10px" v-if="tableSelections.length">
            <el-button type="danger" size="small" @click="batchRemove">批量移除（{{ tableSelections.length }}）</el-button>
          </div>
          <el-table ref="tableRef" :data="selectedItems" stripe border style="width: 100%"
            @selection-change="onTableSelectionChange">
            <el-table-column type="selection" width="45" align="center" />
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
          <el-button type="primary" :loading="loading" :disabled="!workerId || !selectedItems.length"
            @click="handleSubmit">
            确认出库（{{ selectedItems.length }}件）
          </el-button>
          <el-button v-if="lastOutbound" @click="handlePrint">打印出库单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ItemPicker ref="pickerRef" title="选择要出库的工件" :multiple="true" :default-status="1"
      :show-status="false" :show-worker-filter="true" fixed-owner="hq" :exclude-ids="selectedItems.map(i => i.id)" :exclude-barcodes="selectedItems.map(i => i.barcode)"
      @confirm="onPickerConfirm" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllWorkers } from '@/api/worker'
import { flowOutbound } from '@/api/flow'
import { printSlip } from '@/utils/print'
import ItemPicker from '@/components/ItemPicker.vue'

const workerId = ref(null)
const workerList = ref([])
const selectedItems = ref([])
const remark = ref('')
const loading = ref(false)
const pickerRef = ref()
const lastOutbound = ref(null)
const tableRef = ref()
const tableSelections = ref([])

const openPicker = () => { pickerRef.value.open() }

const onTableSelectionChange = (rows) => { tableSelections.value = rows }

const batchRemove = () => {
  const ids = new Set(tableSelections.value.map(r => r.id))
  selectedItems.value = selectedItems.value.filter(i => !ids.has(i.id))
  tableSelections.value = []
}

const onPickerConfirm = (items) => {
  items.forEach(item => {
    if (!selectedItems.value.find(s => s.id === item.id)) {
      selectedItems.value.push(item)
    }
  })
}

const handleSubmit = async () => {
  await ElMessageBox.confirm('确认出库 ' + selectedItems.value.length + ' 件工件？', '确认', { type: 'warning' })
  loading.value = true
  try {
    await flowOutbound({
      workerId: workerId.value,
      accessoryIds: selectedItems.value.map(i => i.id),
      remark: remark.value
    })
    lastOutbound.value = {
      workerName: workerList.value.find(w => w.id === workerId.value)?.name || '',
      items: [...selectedItems.value],
      remark: remark.value,
      time: new Date().toLocaleString('zh-CN')
    }
    ElMessage.success('出库成功')
    selectedItems.value = []
    remark.value = ''
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

const handlePrint = () => {
  const d = lastOutbound.value
  const rows = d.items.map((item, i) =>
    `<tr><td>${i + 1}</td><td>${item.itemCode || ''}</td><td>${item.barcode || ''}</td><td>${item.categoryName || ''}</td></tr>`
  ).join('')
  const html = `
    <div class="slip-header"><h1>配件出库单</h1><div class="sub">打印时间：${d.time}</div></div>
    <div class="info-section">
      <div class="left"><span>接收师傅：${d.workerName}</span><span>工件数量：${d.items.length} 件</span></div>
      <div class="right"><span>备注：${d.remark || '无'}</span></div>
    </div>
    <table><thead><tr><th>序号</th><th>工件编号</th><th>条码</th><th>分类</th></tr></thead><tbody>${rows}</tbody></table>
    <div class="footer"><span>经办人签字：<span class="sign"></span></span><span>接收人签字：<span class="sign"></span></span></div>`
  printSlip('配件出库单', html)
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
.outbound-form { max-width: 900px; }
.outbound-form :deep(.el-form-item) { margin-bottom: 22px; }
</style>