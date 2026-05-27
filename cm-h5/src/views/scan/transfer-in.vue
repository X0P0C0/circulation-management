<template>
  <div>
    <van-nav-bar title="配件归还" left-arrow @click-left="router.back()" />
    <van-cell-group inset title="选择师傅" style="margin-top: 12px">
      <van-field v-model="workerName" label="师傅" placeholder="选择归还师傅" is-link readonly
        @click="showWorkerPicker = true" required />
    </van-cell-group>
    <van-cell-group inset title="添加配件" style="margin-top: 12px">
      <van-field v-model="barcodeInput" label="条码" placeholder="输入条码后添加">
        <template #button>
          <van-button size="small" type="primary" @click="startScan">扫码</van-button>
          <van-button size="small" style="margin-left: 4px" @click="addBarcode">添加</van-button>
        </template>
      </van-field>
    </van-cell-group>
    <van-cell-group v-if="barcodeList.length" inset title="待归还列表" style="margin-top: 12px">
      <van-swipe-cell v-for="(item, index) in barcodeList" :key="index">
        <van-cell :title="item.name" :label="item.barcode" />
        <template #right>
          <van-button square type="danger" text="删除" @click="barcodeList.splice(index, 1)" />
        </template>
      </van-swipe-cell>
    </van-cell-group>
    <div style="margin: 16px">
      <van-button round block type="primary" :loading="loading" :disabled="!form.workerId || !barcodeList.length"
        @click="handleSubmit">确认归还 ({{ barcodeList.length }}件)</van-button>
    </div>
    <van-popup v-model:show="showWorkerPicker" position="bottom" round>
      <van-picker :columns="workerColumns" @confirm="onWorkerConfirm" @cancel="showWorkerPicker = false" />
    </van-popup>
    <van-popup v-model:show="showScanner" position="bottom" style="height: 60%">
      <div id="qr-reader" style="width: 100%"></div>
      <div style="padding: 16px; text-align: center"><van-button @click="stopScan">关闭扫码</van-button></div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAllWorkers } from '@/api/worker'
import { getAccessoryByBarcode } from '@/api/accessory'
import { transferIn } from '@/api/flow'

const router = useRouter()
const loading = ref(false)
const showWorkerPicker = ref(false)
const showScanner = ref(false)
const workerName = ref('')
const barcodeInput = ref('')
const barcodeList = ref([])
const workerColumns = ref([])
const form = reactive({ workerId: null, barcodes: [] })
let html5QrCode = null

const onWorkerConfirm = ({ selectedOptions }) => {
  form.workerId = selectedOptions[0]?.value
  workerName.value = selectedOptions[0]?.text
  showWorkerPicker.value = false
}

const addBarcode = async () => {
  const barcode = barcodeInput.value.trim()
  if (!barcode) return
  if (barcodeList.value.find(b => b.barcode === barcode)) { showToast('已添加'); barcodeInput.value = ''; return }
  try {
    const { data } = await getAccessoryByBarcode(barcode)
    barcodeList.value.push(data)
    barcodeInput.value = ''
  } catch (e) { /* handled */ }
}

const startScan = async () => {
  showScanner.value = true
  try {
    const { Html5Qrcode } = await import('html5-qrcode')
    html5QrCode = new Html5Qrcode('qr-reader')
    await html5QrCode.start({ facingMode: 'environment' }, { fps: 10, qrbox: { width: 250, height: 150 } },
      (text) => { barcodeInput.value = text; stopScan(); addBarcode() })
  } catch (e) { showToast('无法调用摄像头'); showScanner.value = false }
}

const stopScan = async () => {
  if (html5QrCode) { try { await html5QrCode.stop() } catch (e) {} html5QrCode = null }
  showScanner.value = false
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await transferIn({ workerId: form.workerId, barcodes: barcodeList.value.map(b => b.barcode) })
    showToast.success('归还成功')
    barcodeList.value = []
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(async () => {
  try { const { data } = await getAllWorkers(); workerColumns.value = data.map(w => ({ text: w.name, value: w.id })) } catch (e) {}
})
onUnmounted(() => { stopScan() })
</script>