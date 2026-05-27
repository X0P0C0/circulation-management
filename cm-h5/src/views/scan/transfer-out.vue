<template>
  <div>
    <van-nav-bar title="配件领用" left-arrow @click-left="router.back()" />
    <van-cell-group inset title="选择师傅" style="margin-top: 12px">
      <van-field v-model="workerName" label="师傅" placeholder="选择领用师傅" is-link readonly
        @click="showWorkerPicker = true" required />
    </van-cell-group>
    <van-cell-group inset title="添加配件" style="margin-top: 12px">
      <van-field v-model="barcodeInput" label="条码" placeholder="输入条码后添加">
        <template #button>
          <van-button size="small" type="primary" @click="startCameraScan">拍照扫码</van-button>
          <van-button size="small" style="margin-left: 4px" @click="triggerFileInput">相册识别</van-button>
          <van-button size="small" style="margin-left: 4px" @click="addBarcode">添加</van-button>
        </template>
      </van-field>
    </van-cell-group>

    <van-cell-group v-if="barcodeList.length" inset title="待领用列表" style="margin-top: 12px">
      <van-swipe-cell v-for="(item, index) in barcodeList" :key="index">
        <van-cell :title="item.name" :label="item.barcode" />
        <template #right>
          <van-button square type="danger" text="删除" @click="barcodeList.splice(index, 1)" />
        </template>
      </van-swipe-cell>
    </van-cell-group>

    <div style="margin: 16px">
      <van-button round block type="primary" :loading="loading" :disabled="!form.workerId || !barcodeList.length"
        @click="handleSubmit">
        确认领用（{{ barcodeList.length }}件）
      </van-button>
    </div>

    <van-popup v-model:show="showWorkerPicker" position="bottom" round>
      <van-picker :columns="workerColumns" @confirm="onWorkerConfirm" @cancel="showWorkerPicker = false" />
    </van-popup>

    <van-popup v-model:show="showScanner" position="bottom" style="height: 60%">
      <div id="qr-reader" style="width: 100%"></div>
      <div style="padding: 16px; text-align: center">
        <van-button @click="stopScan">关闭扫码</van-button>
      </div>
    </van-popup>

    <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
    <div id="qr-temp" style="display: none"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAllWorkers } from '@/api/worker'
import { getAccessoryByBarcode } from '@/api/accessory'
import { transferOut } from '@/api/flow'
import { useBarcodeScan } from '@/composables/useBarcodeScan'

const router = useRouter()
const { showScanner, scanning, startCameraScan, scanFromFile, stopScan } = useBarcodeScan()

const loading = ref(false)
const showWorkerPicker = ref(false)
const workerName = ref('')
const barcodeInput = ref('')
const barcodeList = ref([])
const workerColumns = ref([])
const fileInput = ref(null)
const form = reactive({ workerId: null, barcodes: [], remark: '' })

const triggerFileInput = () => { fileInput.value?.click() }

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const result = await scanFromFile(file)
  if (result) {
    barcodeInput.value = result
    await addBarcode()
  }
  e.target.value = ''
}

const onWorkerConfirm = ({ selectedOptions }) => {
  form.workerId = selectedOptions[0]?.value
  workerName.value = selectedOptions[0]?.text
  showWorkerPicker.value = false
}

const addBarcode = async () => {
  const barcode = barcodeInput.value.trim()
  if (!barcode) return
  if (barcodeList.value.find(b => b.barcode === barcode)) { showToast('该条码已添加'); barcodeInput.value = ''; return }
  try {
    const { data } = await getAccessoryByBarcode(barcode)
    barcodeList.value.push(data)
    barcodeInput.value = ''
    showToast.success('已添加：' + data.name)
  } catch (e) { /* handled */ }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await transferOut({ workerId: form.workerId, barcodes: barcodeList.value.map(b => b.barcode) })
    showToast.success('领用成功')
    barcodeList.value = []
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(async () => {
  try {
    const { data } = await getAllWorkers()
    workerColumns.value = data.map(w => ({ text: w.name, value: w.id }))
  } catch (e) { /* ignore */ }
})
</script>