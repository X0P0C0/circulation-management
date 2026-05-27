<template>
  <div class="barcode-scanner">
    <div class="scanner-row">
      <el-input
        v-model="inputValue"
        :placeholder="placeholder"
        clearable
        @keyup.enter="handleConfirm"
        @clear="handleClear"
      />
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        :on-change="handleFileChange"
      >
        <el-button :icon="Camera" :loading="scanning">识别条码</el-button>
      </el-upload>
      <el-button type="primary" @click="handleConfirm">确认</el-button>
    </div>
    <div v-if="scanResult" class="scan-tip">
      <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
      <span>{{ scanResult }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Camera, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Quagga from '@ericblade/quagga2'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '手动输入条码，或点击"识别条码"上传图片' }
})

const emit = defineEmits(['update:modelValue', 'scanned'])

const inputValue = ref(props.modelValue)
const scanning = ref(false)
const scanResult = ref('')
const uploadRef = ref()

watch(() => props.modelValue, (val) => { inputValue.value = val })
watch(inputValue, (val) => { emit('update:modelValue', val) })

const handleConfirm = () => {
  const code = inputValue.value.trim()
  if (code) emit('scanned', code)
}

const handleClear = () => {
  scanResult.value = ''
  emit('update:modelValue', '')
}

const handleFileChange = async (file) => {
  if (!file || !file.raw) return
  scanning.value = true
  scanResult.value = ''
  try {
    const result = await recognizeBarcode(file.raw)
    if (result) {
      inputValue.value = result
      scanResult.value = '识别成功：' + result
      emit('scanned', result)
    } else {
      ElMessage.warning('未能识别到条码，请尝试更清晰的图片或手动输入')
    }
  } catch (e) {
    ElMessage.warning('条码识别失败，请手动输入')
  } finally {
    scanning.value = false
    if (uploadRef.value) uploadRef.value.clearFiles()
  }
}

function recognizeBarcode(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      Quagga.decodeSingle({
        src: e.target.result,
        numOfWorkers: 0,
        inputStream: { size: 800 },
        decoder: {
          readers: [
            'code_128_reader', 'ean_reader', 'ean_8_reader',
            'code_39_reader', 'code_93_reader', 'upc_reader',
            'upc_e_reader', 'codabar_reader', 'i2of5_reader'
          ]
        },
        locate: true,
        locator: { halfSample: true, patchSize: 'medium' }
      }, (result) => {
        resolve(result?.codeResult?.code || null)
      })
    }
    reader.onerror = () => resolve(null)
    reader.readAsDataURL(file)
  })
}
</script>

<style scoped>
.barcode-scanner {
  width: 100%;
}

.scanner-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.scanner-row .el-input {
  flex: 1;
}

.scan-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>