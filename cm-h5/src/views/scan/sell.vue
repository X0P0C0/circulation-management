<template>
  <div>
    <van-nav-bar title="配件售卖" left-arrow @click-left="router.back()" />
    <van-form @submit="handleSubmit">
      <van-cell-group inset title="条码" style="margin-top: 12px">
        <van-field v-model="form.barcode" label="条码" placeholder="扫码或输入" required
          :rules="[{ required: true, message: '请输入条码' }]" @blur="loadAccessory">
          <template #button>
            <van-button size="small" type="primary" @click="startCameraScan">拍照扫码</van-button>
            <van-button size="small" style="margin-left: 4px" @click="triggerFileInput">相册识别</van-button>
          </template>
        </van-field>
      </van-cell-group>
      <van-cell-group v-if="accessoryInfo" inset title="配件信息" style="margin-top: 12px">
        <van-cell title="名称" :value="accessoryInfo.name" />
        <van-cell title="规格" :value="accessoryInfo.spec || '-'" />
        <van-cell title="可用库存" :value="accessoryInfo.availableQty" />
      </van-cell-group>
      <van-cell-group inset title="客户信息（选填）" style="margin-top: 12px">
        <van-field v-model="form.customerName" label="客户名" placeholder="选填" />
        <van-field v-model="form.customerPhone" label="电话" placeholder="选填" />
        <van-field v-model="form.remark" label="备注" type="textarea" rows="2" />
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="danger" native-type="submit" :loading="loading">确认售卖</van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showScanner" position="bottom" style="height: 60%">
      <div id="qr-reader" style="width: 100%"></div>
      <div style="padding: 16px; text-align: center"><van-button @click="stopScan">关闭扫码</van-button></div>
    </van-popup>

    <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
    <div id="qr-temp" style="display: none"></div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAccessoryByBarcode } from '@/api/accessory'
import { sellAccessory } from '@/api/flow'
import { useBarcodeScan } from '@/composables/useBarcodeScan'

const router = useRouter()
const { showScanner, scanning, startCameraScan, scanFromFile, stopScan } = useBarcodeScan()

const loading = ref(false)
const accessoryInfo = ref(null)
const fileInput = ref(null)
const form = reactive({ barcode: '', customerName: '', customerPhone: '', remark: '' })

const triggerFileInput = () => { fileInput.value?.click() }

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const result = await scanFromFile(file)
  if (result) {
    form.barcode = result
    await loadAccessory()
    showToast('识别成功：' + result)
  }
  e.target.value = ''
}

const loadAccessory = async () => {
  if (!form.barcode) return
  try { const { data } = await getAccessoryByBarcode(form.barcode); accessoryInfo.value = data } catch (e) { accessoryInfo.value = null }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await sellAccessory(form)
    showToast.success('售卖成功')
    Object.assign(form, { barcode: '', customerName: '', customerPhone: '', remark: '' })
    accessoryInfo.value = null
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>