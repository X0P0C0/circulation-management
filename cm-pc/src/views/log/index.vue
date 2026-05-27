<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">操作日志</h2></div>
    <el-card>
      <div class="search-bar" style="display: flex; gap: 12px; margin-bottom: 16px">
        <el-select v-model="actionType" placeholder="操作类型" clearable style="width: 180px">
          <el-option label="登录" value="LOGIN" />
          <el-option label="入库" value="INBOUND" />
          <el-option label="领用" value="TRANSFER_OUT" />
          <el-option label="归还" value="TRANSFER_IN" />
          <el-option label="售卖" value="SELL" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="actionType" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ actionMap[row.actionType] || row.actionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="操作内容" show-overflow-tooltip />
        <el-table-column prop="relatedBarcode" label="关联条码" width="180" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLogs } from '@/api/log'

const list = ref([])
const loading = ref(false)
const actionType = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const actionMap = {
  LOGIN: '登录', INBOUND: '入库', TRANSFER_OUT: '领用',
  TRANSFER_IN: '归还', SELL: '售卖'
}

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await getLogs({
      actionType: actionType.value, pageNum: pageNum.value, pageSize: pageSize.value
    })
    list.value = data.records
    total.value = data.total
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(() => loadData())
</script>