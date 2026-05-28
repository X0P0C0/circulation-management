<template>
  <el-dialog v-model="visible" :title="title" width="900px" :close-on-click-modal="false" top="5vh">
    <!-- 搜索条件 -->
    <div class="picker-search">
      <el-input v-model="filters.barcode" placeholder="条码" clearable style="width: 160px" @keyup.enter="doSearch" />
      <el-input v-model="filters.itemCode" placeholder="工件编号" clearable style="width: 160px" @keyup.enter="doSearch" />
      <el-select v-model="filters.categoryId" placeholder="分类" clearable style="width: 140px" @change="doSearch">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-if="showStatus" v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="doSearch">
        <el-option label="在库" :value="1" />
        <el-option label="已出库" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="寄回厂家" :value="4" />
        <el-option label="旧件待返厂" :value="5" />
        <el-option label="已售卖" :value="6" />
      </el-select>
      <el-button type="primary" @click="doSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" stripe border v-loading="loading" @selection-change="onSelectionChange"
      style="margin-top: 12px" :max-height="400">
      <el-table-column v-if="multiple" type="selection" width="50" align="center"
        :selectable="(row) => !selectedIds.has(row.id)" />
      <el-table-column prop="itemCode" label="工件编号" width="160" />
      <el-table-column prop="barcode" label="条码" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="statusDesc" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workerName" label="持有师傅" width="100" />
      <el-table-column v-if="!multiple" label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="onSingleSelect(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="picker-pagination">
      <span>每页</span>
      <el-input v-model.number="pageSizeInput" size="small" style="width: 60px" @change="onPageSizeChange" />
      <span>条</span>
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next" @change="loadData" />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="multiple" type="primary" :disabled="!currentSelection.length" @click="onConfirm">
        确认选择（{{ currentSelection.length }}件）
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { searchAccessories } from '@/api/accessory'
import { getCategories } from '@/api/category'

const props = defineProps({
  title: { type: String, default: '选择工件' },
  multiple: { type: Boolean, default: true },
  showStatus: { type: Boolean, default: false },
  defaultStatus: { type: Number, default: null },
  excludeIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
const categories = ref([])
const currentSelection = ref([])
const selectedIds = computed(() => new Set(props.excludeIds))

const filters = reactive({
  barcode: '', itemCode: '', categoryId: null, status: props.defaultStatus
})

watch(() => props.defaultStatus, (val) => { filters.status = val })

const open = async () => {
  filters.barcode = ''
  filters.itemCode = ''
  filters.categoryId = null
  filters.status = props.defaultStatus
  pageNum.value = 1
  currentSelection.value = []
  visible.value = true
  await loadCategories()
  loadData()
}

const loadCategories = async () => {
  try {
    const { data } = await getCategories()
    categories.value = data
  } catch (e) { /* ignore */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...filters,
      keyword: filters.itemCode || undefined
    }
    // 清除空值
    Object.keys(params).forEach(k => { if (params[k] === '' || params[k] == null) delete params[k] })
    const { data } = await searchAccessories(params)
    list.value = data.records
    total.value = data.total
  } catch (e) { /* handled */ } finally {
    loading.value = false
  }
}

const doSearch = () => {
  pageNum.value = 1
  loadData()
}

const onPageSizeChange = (val) => {
  pageSize.value = val || 20
  pageNum.value = 1
  loadData()
}

const onSelectionChange = (selection) => {
  currentSelection.value = selection
}

const onSingleSelect = (row) => {
  visible.value = false
  emit('confirm', [row])
}

const onConfirm = () => {
  visible.value = false
  emit('confirm', currentSelection.value)
}

const statusTagType = (s) => ({
  1: 'success', 2: 'primary', 3: 'info', 4: 'warning', 5: 'warning', 6: 'danger'
}[s] || 'info')

defineExpose({ open })
</script>

<style scoped>
.picker-search {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.picker-pagination {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  justify-content: flex-end;
}

.picker-pagination span {
  font-size: 13px;
  color: #606266;
}
</style>