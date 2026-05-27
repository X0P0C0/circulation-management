<template>
  <div>
    <van-nav-bar title="配件入库" left-arrow @click-left="router.back()" />
    <van-form @submit="handleSubmit">
      <van-cell-group inset title="条码" style="margin-top: 12px">
        <van-field v-model="form.barcode" label="条码" placeholder="扫码或输入条码" required
          :rules="[{ required: true, message: '请输入条码' }]">
          <template #button>
            <van-button size="small" type="primary" @click="startCameraScan">拍照扫码</van-button>
            <van-button size="small" style="margin-left: 4px" @click="triggerFileInput">相册识别</van-button>
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

    <!-- 摄像头扫码弹窗 -->
    <van-popup v-model:show="showScanner" position="bottom" style="height: 60%">
      <div id="qr-reader" style="width: 100%"></div>
      <div style="padding: 16px; text-align: center">
        <van-button @click="stopScan">关闭扫码</van-button>
      </div>
    </van-popup>

    <!-- 隐藏的文件选择 -->
    <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
    <!-- 隐藏的临时元素用于文件识别 -->
    <div id="qr-temp" style="display: none"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { accessoryInbound } from '@/api/accessory'
import { getCategories } from '@/api/category'
import { useBarcodeScan } from '@/composables/useBarcodeScan'

const router = useRouter()
const { showScanner, scanning, startCameraScan, scanFromFile, stopScan } = useBarcodeScan()

const loading = ref(false)
const showCategoryPicker = ref(false)
const categoryName = ref('')
const categoryColumns = ref([])
const fileInput = ref(null)
let categoryList = []

const form = reactive({ barcode: '', name: '', spec: '', categoryId: null, unit: '个', remark: '' })

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const result = await scanFromFile(file)
  if (result) {
    form.barcode = result
    showToast('识别成功：' + result)
  }
  // 清除input值，允许重复选择同一文件
  e.target.value = ''
}

const onCategoryConfirm = ({ selectedOptions }) => {
  form.categoryId = selectedOptions[0]?.value
  categoryName.value = selectedOptions[0]?.text
  showCategoryPicker.value = false
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
</script>