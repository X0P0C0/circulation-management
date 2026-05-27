<template>
  <div>
    <van-nav-bar title="操作记录" left-arrow @click-left="router.back()" />
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部" name="all" />
      <van-tab title="入库" name="1" />
      <van-tab title="领用" name="2" />
      <van-tab title="归还" name="3" />
      <van-tab title="售卖" name="4" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="listLoading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <van-cell v-for="item in list" :key="item.id"
          :title="item.accessoryName"
          :label="'条码: ' + item.barcode + (item.workerName ? ' | 师傅: ' + item.workerName : '')"
          :value="formatType(item.flowType)" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFlowRecords } from '@/api/flow'

const router = useRouter()
const activeTab = ref('all')
const list = ref([])
const listLoading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)

const formatType = (t) => ({ 1: '入库', 2: '领用', 3: '归还', 4: '售卖' }[t] || '-')

const onTabChange = () => {
  pageNum.value = 1
  list.value = []
  finished.value = false
}

const loadData = async () => {
  const params = { pageNum: pageNum.value, pageSize: 20 }
  if (activeTab.value !== 'all') params.flowType = parseInt(activeTab.value)
  try {
    const { data } = await getFlowRecords(params)
    if (refreshing.value) { list.value = []; refreshing.value = false }
    list.value.push(...data.records)
    listLoading.value = false
    if (list.value.length >= data.total) finished.value = true
    else pageNum.value++
  } catch (e) { listLoading.value = false }
}

const onRefresh = () => {
  pageNum.value = 1
  finished.value = false
  list.value = []
  loadData()
}
</script>