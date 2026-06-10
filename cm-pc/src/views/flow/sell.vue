<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件售卖</h2>
    </div>
    <el-card>
      <el-form label-width="100px" class="sell-form">
        <el-form-item label="选择工件">
          <el-button type="primary" @click="openPicker">添加工件</el-button>
        </el-form-item>
        <el-form-item v-if="selectedItems.length" label="售卖清单">
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
            <el-table-column label="单价（元）" width="180" align="center">
              <template #default="{ row }">
                <el-input-number v-model="row.price" :min="0" :precision="2" :step="10" size="small" controls-position="right" style="width: 140px" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="selectedItems.splice($index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="customerName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="客户电话">
          <el-input v-model="customerPhone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="loading" :disabled="!selectedItems.length"
            @click="handleSubmit">
            确认售卖（{{ selectedItems.length }}件）
          </el-button>
          <el-button v-if="lastSell" @click="handlePrint">打印售卖单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ItemPicker ref="pickerRef" title="选择要售卖的工件" :multiple="true" :show-status="true" :show-worker-filter="true"
      :exclude-ids="selectedItems.map(i => i.id)" :exclude-barcodes="selectedItems.map(i => i.barcode)"
      @confirm="onPickerConfirm" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { flowSell } from '@/api/flow'
import { printSlip } from '@/utils/print'
import ItemPicker from '@/components/ItemPicker.vue'

const selectedItems = ref([])
const customerName = ref('')
const customerPhone = ref('')
const remark = ref('')
const loading = ref(false)
const pickerRef = ref()
const lastSell = ref(null)
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
      selectedItems.value.push({ ...item, price: 0 })
    }
  })
}

const handlePrint = () => {
  const d = lastSell.value
  const total = d.items.reduce((s, i) => s + (i.price || 0), 0)
  const rows = d.items.map((item, i) =>
    `<tr><td>${i + 1}</td><td>${item.itemCode || ''}</td><td>${item.barcode || ''}</td><td>${item.categoryName || ''}</td><td>${(item.price || 0).toFixed(2)}</td></tr>`
  ).join('')
  const html = `
    <div class="slip-header"><h1>配件售卖单</h1><div class="sub">打印时间：${d.time}</div></div>
    <div class="info-section">
      <div class="left"><span>客户：${d.customerName || '散客'}</span><span>电话：${d.customerPhone || '-'}</span></div>
      <div class="right"><span>总金额：¥${total.toFixed(2)}</span><span>备注：${d.remark || '无'}</span></div>
    </div>
    <table><thead><tr><th>序号</th><th>工件编号</th><th>条码</th><th>分类</th><th>单价(元)</th></tr></thead><tbody>${rows}</tbody></table>
    <div class="footer"><span>经办人签字：<span class="sign"></span></span><span>客户签字：<span class="sign"></span></span></div>`
  printSlip('配件售卖单', html)
}

const handleSubmit = async () => {
  await ElMessageBox.confirm('确认售卖 ' + selectedItems.value.length + ' 件工件？', '确认', { type: 'warning' })
  loading.value = true
  try {
    await flowSell({
      items: selectedItems.value.map(i => ({ accessoryId: i.id, price: i.price || 0 })),
      customerName: customerName.value,
      customerPhone: customerPhone.value,
      remark: remark.value
    })
    lastSell.value = {
      customerName: customerName.value,
      customerPhone: customerPhone.value,
      items: [...selectedItems.value],
      remark: remark.value,
      time: new Date().toLocaleString('zh-CN')
    }
    ElMessage.success('售卖成功')
    selectedItems.value = []
    customerName.value = ''
    customerPhone.value = ''
    remark.value = ''
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.sell-form { max-width: 900px; }
.sell-form :deep(.el-form-item) { margin-bottom: 22px; }
</style>