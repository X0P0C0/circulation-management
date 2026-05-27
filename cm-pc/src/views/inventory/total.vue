<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">总库存</h2>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon> 导出CSV
      </el-button>
    </div>
    <el-card>
      <div class="search-bar" style="display: flex; gap: 12px; margin-bottom: 16px">
        <el-input v-model="keyword" placeholder="搜索名称/条码" clearable style="width: 250px"
          @keyup.enter="loadData" />
        <el-select v-model="categoryId" placeholder="选择分类" clearable style="width: 180px" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="barcode" label="条码" width="180" />
        <el-table-column prop="accessoryName" label="配件名称" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="totalQty" label="总入库" width="100" align="center" />
        <el-table-column prop="availableQty" label="可用库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableQty > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableQty }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInventories, exportInventoryCsv } from '@/api/inventory'
import { getCategories } from '@/api/category'

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const categoryId = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categories = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getInventories({
      keyword: keyword.value, categoryId: categoryId.value,
      pageNum: pageNum.value, pageSize: pageSize.value
    })
    list.value = data.records
    total.value = data.total
  } catch (e) { /* handled */ } finally { loading.value = false }
}

const handleExport = () => {
  exportInventoryCsv({ keyword: keyword.value, categoryId: categoryId.value })
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
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 18px; }
</style>