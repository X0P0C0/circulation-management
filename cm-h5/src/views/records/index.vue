<template>
  <div>
    <van-nav-bar title="Records" left-arrow @click-left="router.back()" />
    <van-tabs v-model:active="activeTab" @change="loadData">
      <van-tab title="All" name="all" />
      <van-tab title="Inbound" name="1" />
      <van-tab title="Transfer Out" name="2" />
      <van-tab title="Return" name="3" />
      <van-tab title="Sell" name="4" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="listLoading" :finished="finished" finished-text="No more" @load="loadData">
        <van-cell v-for="item in list" :key="item.id"
          :title="item.accessoryName"
          :label="'Barcode: ' + item.barcode + (item.workerName ? ' | Worker: ' + item.workerName : '')"
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

const formatType = (t) => ({ 1: 'Inbound', 2: 'Out', 3: 'Return', 4: 'Sell' }[t] || '-')

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
  loadData()
}
</script>