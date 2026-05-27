<template>
  <div>
    <van-nav-bar title="配件入库" left-arrow @click-left="router.back()" />
    <van-form @submit="handleSubmit">
      <van-cell-group inset title="条码">
        <van-field v-model="form.barcode" label="条码" placeholder="扫码或输入条码" required
          :rules="[{ required: true, message: '请输入条码' }]">
          <template #button>
            <van-button size="small" type="primary" @click="startScan">扫码</van-button>
          </template>
        </van-field>
      </van-cell-group>
      <van-cell-group inset title="配件信息" style="margin-top: 12px">
        <van-field v-model="form.name" label="名称" placeholder="配件名称" required
          :rules="[{ required: true, message: '请输入名称' }]" />
        <van-field v-model="form.spec" label="规格" placeholder="规格型号" />
        <van-field v-model="categoryName" label="分类" placeholder="选择分类" is-link readonly
          @click="showCategoryPicker = true" required />
        <van-field v-model="form.unit" label="单位" placeholder="个/件/套" />
        <van-field v-model="form.remark" label="备注" type="textarea" rows="2" />
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit" :loading="loading">确认入库</van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showCategoryPicker" position="bottom" round>
      <van-picker :columns="categoryColumns" @confirm="onCategoryConfirm" @cancel="showCategoryPicker = false" />
    </van-popup>

    <van-popup v-model:show="showScanner" position="bottom" style="height: 60%">
      <div id="qr-reader" style="width: 100%"></div>
      <div style="padding: 16px; text-align: center">
        <van-button @click="stopScan">关闭扫码</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { accessoryInbound } from '@/api/accessory'
import { getCategories } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const showCategoryPicker = ref(false)
const showScanner = ref(false)
const categoryName = ref('')
const categoryColumns = ref([])
let categoryList = []
let html5QrCode = null

const form = reactive({ barcode: '', name: '', spec: '', categoryId: null, unit: '个', remark: '' })

const onCategoryConfirm = ({ selectedOptions }) => {
  form.categoryId = selectedOptions[0]?.value
  categoryName.value = selectedOptions[0]?.text
  showCategoryPicker.value = false
}

const startScan = async () => {
  showScanner.value = true
  try {
    const { Html5Qrcode } = await import('html5-qrcode')
    html5QrCode = new Html5Qrcode('qr-reader')
    await html5QrCode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 250, height: 150 } },
      (decodedText) => {
        form.barcode = decodedText
        stopScan()
        showToast('扫码成功')
      }
    )
  } catch (e) {
    showToast('无法调用摄像头，请手动输入')
    showScanner.value = false
  }
}

const stopScan = async () => {
  if (html5QrCode) {
    try { await html5QrCode.stop() } catch (e) { /* ignore */ }
    html5QrCode = null
  }
  showScanner.value = false
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await accessoryInbound(form)
    showToast.success('入库成功')
    Object.assign(form, { barcode: '', name: '', spec: '', categoryId: null, unit: '个', remark: '' })
    categoryName.value = ''
  } catch (e) { /* handled */ } finally { loading.value = false }
}

onMounted(async () => {
  try {
    const { data } = await getCategories()
    categoryList = data
    categoryColumns.value = data.map(c => ({ text: c.name, value: c.id }))
  } catch (e) { /* ignore */ }
})

onUnmounted(() => { stopScan() })
</script>