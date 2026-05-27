<template>
  <div>
    <van-nav-bar title="配件售卖" left-arrow @click-left="router.back()" />
    <van-form @submit="handleSubmit">
      <van-cell-group inset title="条码" style="margin-top: 12px">
        <van-field v-model="form.barcode" label="条码" placeholder="扫码或输入" required
          :rules="[{ required: true, message: '请输入条码' }]" @blur="loadAccessory">
          <template #button>
            <van-button size="small" type="primary" @click="startScan">扫码</van-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAccessoryByBarcode } from '@/api/accessory'
import { sellAccessory } from '@/api/flow'

const router = useRouter()
const loading = ref(false)
const showScanner = ref(false)
const accessoryInfo = ref(null)
const form = reactive({ barcode: '', customerName: '', customerPhone: '', remark: '' })
let html5QrCode = null

const loadAccessory = async () => {
  if (!form.barcode) return
  try { const { data } = await getAccessoryByBarcode(form.barcode); accessoryInfo.value = data } catch (e) { accessoryInfo.value = null }
}

const startScan = async () => {
  showScanner.value = true
  try {
    const { Html5Qrcode } = await import('html5-qrcode')
    html5QrCode = new Html5Qrcode('qr-reader')
    await html5QrCode.start({ facingMode: 'environment' }, { fps: 10, qrbox: { width: 250, height: 150 } },
      (text) => { form.barcode = text; stopScan(); loadAccessory() })
  } catch (e) { showToast('无法调用摄像头'); showScanner.value = false }
}

const stopScan = async () => {
  if (html5QrCode) { try { await html5QrCode.stop() } catch (e) {} html5QrCode = null }
  showScanner.value = false
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

onUnmounted(() => { stopScan() })
</script>