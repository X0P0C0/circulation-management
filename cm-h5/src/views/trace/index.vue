<template>
  <div>
    <van-nav-bar title="条码追溯" left-arrow @click-left="router.back()" />
    <van-search v-model="barcode" placeholder="输入条码查询" show-action @search="handleTrace">
      <template #action>
        <span @click="handleTrace">查询</span>
      </template>
    </van-search>

    <div v-if="traceData" style="padding: 16px">
      <van-cell-group inset>
        <van-cell title="条码" :value="traceData.barcode" />
        <van-cell title="配件名称" :value="traceData.accessoryName" />
        <van-cell title="当前库存" :value="traceData.currentQty" />
        <van-cell title="当前状态" :value="traceData.currentHolder" />
      </van-cell-group>

      <div style="margin-top: 16px; font-size: 15px; font-weight: 600; color: #323233">流转记录</div>
      <van-steps direction="vertical" :active="0" style="margin-top: 12px">
        <van-step v-for="step in traceData.steps" :key="step.id">
          <h4>{{ step.flowTypeDesc }}</h4>
          <p v-if="step.workerName">师傅：{{ step.workerName }}</p>
          <p v-if="step.customerName">客户：{{ step.customerName }}</p>
          <p style="color: #969799">{{ step.createTime }}</p>
        </van-step>
      </van-steps>
      <van-empty v-if="!traceData.steps?.length" description="暂无流转记录" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { traceBarcode } from '@/api/flow'
import { showToast } from 'vant'

const router = useRouter()
const barcode = ref('')
const traceData = ref(null)

const handleTrace = async () => {
  if (!barcode.value) return
  try {
    const { data } = await traceBarcode(barcode.value)
    traceData.value = data
  } catch (e) { traceData.value = null }
}
</script>