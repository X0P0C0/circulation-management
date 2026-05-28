<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">可支配库存</h2>
      <el-switch v-model="showGrouped" active-text="按条码分组" inactive-text="逐条显示" @change="loadData" />
    </div>
    <el-card>
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索条码/工件编号" clearable style="width: 220px" @keyup.enter="loadData" />
        <el-select v-model="categoryId" placeholder="分类" clearable style="width: 140px" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table v-if="showGrouped" :data="groupList" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="barcode" label="条码" sortable />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="availableCount" label="可用数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.availableCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="itemList" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="itemCode" label="工件编号" width="180" sortable />
        <el-table-column prop="barcode" label="条码" sortable />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="createTime" label="入库时间" width="170" sortable />
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
import { searchAccessories, getInventoryGroup } from '@/api/accessory'
import { getCategories } from '@/api/category'

const showGrouped = ref(true)
const loading = ref(false)
const itemList = ref([])
const groupList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    if (showGrouped.value) {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value, statusFilter: 1 }
      if (keyword.value) params.barcode = keyword.value
      if (categoryId.value) params.categoryId = categoryId.value
      const { data } = await getInventoryGroup(params)
      groupList.value = data.records
      total.value = data.total
    } else {
      const params = { pageNum: pageNum.value, pageSize: pageSize.value, status: 1 }
      if (keyword.value) params.keyword = keyword.value
      if (categoryId.value) params.categoryId = categoryId.value
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
.search-bar { display: flex; gap: 8px; margin-bottom: 16px; align-items: center; }
</style>