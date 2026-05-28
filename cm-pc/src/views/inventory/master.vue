<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">总库（全部工件）</h2>
      <el-switch v-model="showGrouped" active-text="按条码分组" inactive-text="逐条显示" @change="loadData" />
    </div>
    <el-card>
      <!-- 搜索条件 -->
      <div class="search-bar">
        <el-input v-model="filters.barcode" placeholder="条码" clearable style="width: 160px" @keyup.enter="loadData" />
        <el-input v-model="filters.itemCode" placeholder="工件编号" clearable style="width: 160px" @keyup.enter="loadData" />
        <el-select v-model="filters.categoryId" placeholder="分类" clearable style="width: 140px" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="loadData">
          <el-option label="在库" :value="1" /><el-option label="已出库" :value="2" />
          <el-option label="已完成" :value="3" /><el-option label="寄回厂家" :value="4" />
          <el-option label="旧件待返厂" :value="5" /><el-option label="已售卖" :value="6" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <!-- 分组视图 -->
      <el-table v-if="showGrouped" :data="groupList" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="barcode" label="条码" sortable />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="totalCount" label="总数" width="100" align="center" sortable />
        <el-table-column prop="availableCount" label="可用" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableCount > 0 ? 'success' : 'info'" size="small">{{ row.availableCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 逐条视图 -->
      <el-table v-else :data="itemList" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="itemCode" label="工件编号" width="170" sortable />
        <el-table-column prop="barcode" label="条码" sortable />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="statusDesc" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerName" label="持有师傅" width="100" />
        <el-table-column prop="createTime" label="入库时间" width="170" sortable />
      </el-table>

      <div class="pagination-wrapper" style="margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; gap: 8px">
        <span style="font-size: 13px; color: #606266">每页</span>
        <el-input v-model.number="pageSizeInput" size="small" style="width: 60px"
          @change="pageSize = pageSizeInput || 20; pageNum = 1; loadData()" />
        <span style="font-size: 13px; color: #606266">条</span>
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { searchAccessories, getInventoryGroup } from '@/api/accessory'
import { getCategories } from '@/api/category'

const showGrouped = ref(true)
const loading = ref(false)
const itemList = ref([])
const groupList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pageSizeInput = ref(20)
const categories = ref([])
const filters = reactive({ barcode: '', itemCode: '', categoryId: null, status: null })

const statusTag = (s) => ({ 1: 'success', 2: 'primary', 3: 'info', 4: 'warning', 5: 'warning', 6: 'danger' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    if (showGrouped.value) {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (filters.barcode) params.barcode = filters.barcode
      if (filters.categoryId) params.categoryId = filters.categoryId
      if (filters.status) params.statusFilter = filters.status
      const { data } = await getInventoryGroup(params)
      groupList.value = data.records
      total.value = data.total
    } else {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value }
      if (filters.barcode) params.barcode = filters.barcode
      if (filters.itemCode) params.keyword = filters.itemCode
      if (filters.categoryId) params.categoryId = filters.categoryId
      if (filters.status) params.status = filters.status
      const { data } = await searchAccessories(params)
      itemList.value = data.records
      total.value = data.total
    }
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(async () => {
  loadData()
  try {
    const { data } = await getCategories()
    categories.value = data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 20px; font-weight: 700; color: #1e293b; }
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
</style>