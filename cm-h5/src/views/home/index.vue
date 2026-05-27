<template>
  <div>
    <van-nav-bar title="配件流转管理" />
    <div style="padding: 16px">
      <van-grid :column-num="2" :gutter="12">
        <van-grid-item icon="scan" text="配件入库" @click="router.push('/scan/inbound')" />
        <van-grid-item icon="upgrade" text="配件领用" @click="router.push('/scan/transfer-out')" />
        <van-grid-item icon="back-top" text="配件归还" @click="router.push('/scan/transfer-in')" />
        <van-grid-item icon="shopping-cart-o" text="配件售卖" @click="router.push('/scan/sell')" />
      </van-grid>

      <van-cell-group inset style="margin-top: 16px" title="快捷查询">
        <van-cell title="条码追溯" is-link @click="router.push('/trace')" />
        <van-cell title="总库存查询" is-link @click="router.push('/inventory')" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>