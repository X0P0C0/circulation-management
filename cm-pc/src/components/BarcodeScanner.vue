<template>
  <div class="barcode-scanner">
    <el-input
      v-model="inputValue"
      :placeholder="placeholder"
      clearable
      @keyup.enter="handleConfirm"
      @clear="handleClear"
    >
      <template #prepend>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          :on-change="handleFileChange"
        >
          <el-button :loading="scanning" :icon="Camera">识别条码</el-button>
        </el-upload>
      </template>
      <template #append>
        <el-button @click="handleConfirm">确认</el-button>
      </template>
    </el-input>
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
  placeholder: { type: String, default: '手动输入条码或点击"识别条码"上传图片' }
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
  if (code) {
    emit('scanned', code)
  }
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
    // 清除upload组件状态，允许重复上传同一文件
    if (uploadRef.value) uploadRef.value.clearFiles()
  }
}

/** 使用Quagga2从图片识别一维条码 */
function recognizeBarcode(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const imgUrl = e.target.result
      Quagga.decodeSingle({
        src: imgUrl,
        numOfWorkers: 0,
        inputStream: {
          size: 800
        },
        decoder: {
          readers: [
            'code_128_reader',
            'ean_reader',
            'ean_8_reader',
            'code_39_reader',
            'code_93_reader',
            'upc_reader',
            'upc_e_reader',
            'codabar_reader',
            'i2of5_reader'
          ]
        },
        locate: true,
        locator: {
          halfSample: true,
          patchSize: 'medium'
        }
      }, (result) => {
        if (result && result.codeResult && result.codeResult.code) {
          resolve(result.codeResult.code)
        } else {
          resolve(null)
        }
      })
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}
</script>

<style scoped>
.barcode-scanner {
  width: 100%;
}

.barcode-scanner :deep(.el-input-group__prepend) {
  padding: 0;
  background: transparent;
}

.barcode-scanner :deep(.el-upload) {
  display: inline-block;
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