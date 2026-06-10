<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">可支配库存</h2>
      <el-radio-group v-model="showGrouped" size="small" @change="loadData">
        <el-radio-button :value="false">明细</el-radio-button>
        <el-radio-button :value="true">汇总</el-radio-button>
      </el-radio-group>
    </div>
    <el-card>
      <div class="search-bar">
        <el-select v-model="ownerFilter" placeholder="持有者" clearable style="width: 160px" @change="handleOwnerChange" @focus="loadWorkers">
          <el-option label="全部" value="" />
          <el-option label="总部库存" value="hq" />
          <el-option v-for="w in workers" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索条码/工件编号" clearable style="width: 220px" @keyup.enter="loadData" />
        <CategoryCascader v-model="categoryId" placeholder="选择分类筛选" />
        <CustomSelect v-model="shelfId" :options="shelfList" placeholder="全部货架" labelKey="name" extraKey="location" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table v-if="showGrouped" :data="groupList" stripe border v-loading="loading" :sort-multiple="true" @sort-change="handleSortChange">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="categoryName" label="分类" width="120" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="availableCount" label="可用数量" width="120" align="center" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.availableCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="itemList" stripe border v-loading="loading" :sort-multiple="true" @sort-change="handleSortChange">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="itemCode" label="工件编号" width="180" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="barcode" label="条码" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="categoryName" label="分类" width="120" sortable :sort-orders="['ascending', 'descending']" />
        <el-table-column prop="workerName" label="持有师傅" width="120" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <span v-if="row.workerName">{{ row.workerName }}</span>
            <el-tag v-else type="info" size="small">总部</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shelfName" label="货架" width="140" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">{{ row.shelfName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="入库时间" width="170" sortable :sort-orders="['ascending', 'descending']"><template #default="{ row }">{{ row.createTime ? String(row.createTime).replace('T', ' ').substring(0, 19) : '' }}</template></el-table-column>
      </el-table>

      <div class="pagination-wrapper" style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMultiSort } from '@/components/multi-sort/useMultiSort'
import { searchAccessories, getInventoryGroup } from '@/api/accessory'
import { formatDate } from '@/utils/export'
import { getCategories } from '@/api/category'
import CategoryCascader from '@/components/CategoryCascader.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import { getShelves } from '@/api/shelf'
import { getAllWorkers } from '@/api/worker'

const showGrouped = ref(true)
const loading = ref(false)
const itemList = ref([])
const groupList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const categories = ref([])
const workers = ref([])
const keyword = ref('')
const categoryId = ref(null)
const ownerFilter = ref('')

const { sorts: sortsRef, applyLocalSort } = useMultiSort()

function applyMultiSortFromTable(sortsRef, { prop, order, shift }) {
  if (!prop || !order) { sortsRef.value = []; return }
  const idx = sortsRef.value.findIndex((s) => s.prop === prop)
  if (shift) {
    if (idx === -1) sortsRef.value.push({ prop, order })
    else sortsRef.value[idx].order = order
  } else {
    sortsRef.value = [{ prop, order }]
  }
}

const handleSortChange = ({ prop, order, $event }) => {
  applyMultiSortFromTable(sortsRef, { prop, order, shift: $event?.shiftKey })
  pageNum.value = 1
  loadData()
}

const handleOwnerChange = () => {
  pageNum.value = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    if (showGrouped.value) {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (ownerFilter.value === 'hq') {
        params.statusFilter = 1
      } else if (ownerFilter.value && typeof ownerFilter.value === 'number') {
        params.workerId = ownerFilter.value
        params.statusFilter = 2
      } else {
        params.statusFilter = 1
      }
      if (keyword.value) params.barcode = keyword.value
      if (categoryId.value) params.categoryId = categoryId.value
      if (shelfId.value) params.shelfId = shelfId.value
      params.statusFilter = params.statusFilter || 1
      const { data } = await getInventoryGroup(params)
      groupList.value = applyLocalSort(data.records)
      total.value = data.total
    } else {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (ownerFilter.value === 'hq') {
        params.status = 1
      } else if (ownerFilter.value && typeof ownerFilter.value === 'number') {
        params.workerId = ownerFilter.value
        params.status = 2
      } else {
        params.status = 1
      }
      if (keyword.value) params.keyword = keyword.value
      if (categoryId.value) params.categoryId = categoryId.value
      if (shelfId.value) params.shelfId = shelfId.value
      const { data } = await searchAccessories(params)
      itemList.value = applyLocalSort(data.records)
      total.value = data.total
    }
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message) } finally { loading.value = false }
}

onMounted(async () => {
  loadData()
  try {
    const { data } = await getShelves()
    shelfList.value = data || []
  } catch (e) { /* ignore */ }
  try {
    const { data } = await getCategories()
    categories.value = data
  } catch (e) { /* ignore */ }
  try {
    const { data } = await getAllWorkers()
    workers.value = data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; align-items: center; }
</style>
