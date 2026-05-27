<template>
  <div>
    <van-nav-bar title="库存查询" />
    <van-search v-model="keyword" placeholder="搜索配件名称/条码" @search="loadData" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="listLoading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <van-cell v-for="item in list" :key="item.accessoryId" :title="item.accessoryName"
          :label="'条码: ' + item.barcode + ' | 分类: ' + (item.categoryName || '-')"
          :value="'库存: ' + item.availableQty" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getInventories } from '@/api/inventory'

const keyword = ref('')
const list = ref([])
const listLoading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)

const loadData = async () => {
  try {
    const { data } = await getInventories({ keyword: keyword.value, pageNum: pageNum.value, pageSize: 20 })
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