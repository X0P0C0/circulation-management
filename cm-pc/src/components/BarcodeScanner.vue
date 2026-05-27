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
import { Html5Qrcode } from 'html5-qrcode'

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
    // 创建隐藏的html元素供html5-qrcode使用
    let tempDiv = document.getElementById('qr-scanner-temp')
    if (!tempDiv) {
      tempDiv = document.createElement('div')
      tempDiv.id = 'qr-scanner-temp'
      tempDiv.style.display = 'none'
      document.body.appendChild(tempDiv)
    }

    const qr = new Html5Qrcode('qr-scanner-temp')
    const result = await qr.scanFile(file.raw, true)

    if (result) {
      inputValue.value = result
      scanResult.value = '识别成功：' + result
      emit('scanned', result)
    } else {
      ElMessage.warning('未能识别到条码，请尝试更清晰的图片或手动输入')
    }
  } catch (e) {
    console.error('条码识别失败:', e)
    ElMessage.warning('未能识别到条码，请尝试更清晰的图片或手动输入')
  } finally {
    scanning.value = false
    if (uploadRef.value) uploadRef.value.clearFiles()
  }
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