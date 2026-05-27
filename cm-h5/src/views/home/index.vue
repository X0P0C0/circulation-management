<template>
  <div>
    <van-nav-bar title="CM System" />
    <div style="padding: 16px">
      <van-grid :column-num="2" :gutter="12">
        <van-grid-item icon="scan" text="Inbound" @click="router.push('/scan/inbound')" />
        <van-grid-item icon="upgrade" text="Transfer Out" @click="router.push('/scan/transfer-out')" />
        <van-grid-item icon="back-top" text="Return" @click="router.push('/scan/transfer-in')" />
        <van-grid-item icon="shopping-cart-o" text="Sell" @click="router.push('/scan/sell')" />
      </van-grid>

      <van-cell-group inset style="margin-top: 16px" title="Query">
        <van-cell title="Barcode Trace" is-link @click="router.push('/trace')" />
        <van-cell title="Inventory" is-link @click="router.push('/inventory')" />
        <van-cell title="Records" is-link @click="router.push('/records')" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>