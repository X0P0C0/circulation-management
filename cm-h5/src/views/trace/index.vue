<template>
  <div>
    <van-nav-bar title="条码追溯" left-arrow @click-left="router.back()" />
    <van-search v-model="barcode" placeholder="输入条码查询" show-action @search="handleTrace">
      <template #action>
        <span @click="handleTrace">查询</span>
      </template>
    </van-search>

    <div v-if="multiItems.length > 1" style="padding: 8px 16px 0">
      <van-tabs v-model:active="activeIndex" swipeable shrink @change="switchItem">
        <van-tab v-for="code in multiItems" :key="code" :title="code" />
      </van-tabs>
    </div>

    <div v-if="traceData" style="padding: 16px">
      <van-cell-group inset>
        <van-cell title="工件编号" :value="traceData.itemCode" />
        <van-cell title="条码" :value="traceData.barcode" />
        <van-cell title="配件名称" :value="traceData.categoryName || '-'" />
        <van-cell title="当前状态" :value="traceData.currentStatusDesc" />
        <van-cell title="当前持有" :value="traceData.currentHolder" />
      </van-cell-group>

      <div style="margin-top: 16px; font-size: 15px; font-weight: 600; color: #323233">流转记录</div>
      <van-steps direction="vertical" :active="0" style="margin-top: 12px">
        <van-step v-for="step in traceData.steps" :key="step.id">
          <h4>{{ step.flowTypeDesc }}</h4>
          <p v-if="step.toWorkerName">师傅：{{ step.toWorkerName }}</p>
          <p v-if="step.fromWorkerName">来源：{{ step.fromWorkerName }}</p>
          <p v-if="step.customerName">客户：{{ step.customerName }}</p>
          <p v-if="step.operator" style="color: #969799">操作人：{{ step.operator }}</p>
          <p style="color: #969799">{{ step.createTime }}</p>
        </van-step>
      </van-steps>
      <van-empty v-if="!traceData.steps?.length" description="暂无流转记录" />
    </div>

    <van-empty v-if="searched && !traceData" description="未找到相关工件" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { traceBarcode, traceItem } from '@/api/flow'

const router = useRouter()
const barcode = ref('')
const traceData = ref(null)
const multiItems = ref([])
const activeIndex = ref(0)
const searched = ref(false)

const handleTrace = async () => {
  if (!barcode.value) return
  try {
    const { data } = await traceBarcode(barcode.value)
    const codes = (data || []).map(i => i.itemCode).filter(Boolean)
    searched.value = true
    if (!codes.length) {
      multiItems.value = []
      traceData.value = null
      return
    }
    multiItems.value = codes
    activeIndex.value = 0
    const { data: d } = await traceItem(codes[0])
    traceData.value = d
  } catch (e) {
    traceData.value = null
  }
}

const switchItem = async (idx) => {
  activeIndex.value = idx
  const { data: d } = await traceItem(multiItems.value[idx])
  traceData.value = d
}
</script>