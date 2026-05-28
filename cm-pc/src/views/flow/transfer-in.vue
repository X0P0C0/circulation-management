<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配件归还</h2>
    </div>
    <el-card>
      <el-form label-width="110px" class="return-form">
        <el-form-item label="归还类型" required>
          <el-radio-group v-model="returnType">
            <el-radio :value="1">工单完成（换件）</el-radio>
            <el-radio :value="2">工单取消（退件）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="returnType === 2" label="工件价值">
          <el-radio-group v-model="highValue">
            <el-radio :value="false">低价值（≤200元，退回可支配库）</el-radio>
            <el-radio :value="true">高价值（&gt;200元，寄回厂家）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择工件">
          <el-button type="primary" @click="openPicker">添加工件</el-button>
        </el-form-item>
        <el-form-item v-if="selectedItems.length" label="归还清单">
          <el-table :data="selectedItems" stripe border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="itemCode" label="工件编号" width="180" />
            <el-table-column prop="barcode" label="条码" />
            <el-table-column prop="workerName" label="持有师傅" width="120" />
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
          <el-button type="primary" :loading="loading" :disabled="!selectedItems.length"
            @click="handleSubmit">
            确认归还（{{ selectedItems.length }}件）
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ItemPicker ref="pickerRef" title="选择要归还的工件" :multiple="true" :default-status="2"
      :show-status="false" :exclude-ids="selectedItems.map(i => i.id)" @confirm="onPickerConfirm" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { flowReturn } from '@/api/flow'
import ItemPicker from '@/components/ItemPicker.vue'

const returnType = ref(1)
const highValue = ref(false)
const selectedItems = ref([])
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
    await flowReturn({
      returnType: returnType.value,
      highValue: returnType.value === 2 ? highValue.value : null,
      accessoryIds: selectedItems.value.map(i => i.id),
      remark: remark.value
    })
    ElMessage.success('归还成功')
    selectedItems.value = []
    remark.value = ''
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.return-form { max-width: 900px; }
.return-form :deep(.el-form-item) { margin-bottom: 22px; }
</style>