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
        </el-form-item>
      </el-form>
    </el-card>

    <ItemPicker ref="pickerRef" title="选择要售卖的工件" :multiple="true" :show-status="true"
      :exclude-ids="selectedItems.map(i => i.id)" @confirm="onPickerConfirm" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { flowSell } from '@/api/flow'
import ItemPicker from '@/components/ItemPicker.vue'

const selectedItems = ref([])
const customerName = ref('')
const customerPhone = ref('')
const remark = ref('')
const loading = ref(false)
const pickerRef = ref()

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
    await flowSell({
      accessoryIds: selectedItems.value.map(i => i.id),
      customerName: customerName.value,
      customerPhone: customerPhone.value,
      remark: remark.value
    })
    ElMessage.success('售卖成功')
    selectedItems.value = []
    customerName.value = ''
    customerPhone.value = ''
    remark.value = ''
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.sell-form { max-width: 900px; }
.sell-form :deep(.el-form-item) { margin-bottom: 22px; }
</style>