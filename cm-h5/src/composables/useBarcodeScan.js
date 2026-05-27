import { ref, onUnmounted } from 'vue'
import { showToast } from 'vant'
import request from '@/utils/request'

/**
 * 条码扫描组合式函数
 * 支持：摄像头实时扫描（本地html5-qrcode）、相册选图识别（后端ZXing）
 */
export function useBarcodeScan() {
  const showScanner = ref(false)
  const scanning = ref(false)
  let html5QrCode = null

  /** 启动摄像头实时扫描 */
  const startCameraScan = async (elementId = 'qr-reader') => {
    showScanner.value = true
    try {
      const { Html5Qrcode } = await import('html5-qrcode')
      html5QrCode = new Html5Qrcode(elementId)
      await html5QrCode.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 250, height: 150 } },
        (decodedText) => {
          stopScan()
          return decodedText
        }
      )
    } catch (e) {
      showToast('无法调用摄像头，请使用相册识别或手动输入')
      showScanner.value = false
    }
  }

  /** 从图片文件识别条码（调用后端ZXing接口） */
  const scanFromFile = async (file) => {
    scanning.value = true
    try {
      const formData = new FormData()
      formData.append('file', file)

      const { data } = await request.post('/api/barcode/recognize', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      return data.barcode
    } catch (e) {
      showToast('未能识别到条码，请尝试更清晰的图片')
      return null
    } finally {
      scanning.value = false
    }
  }

  /** 停止扫描 */
  const stopScan = async () => {
    if (html5QrCode) {
      try { await html5QrCode.stop() } catch (e) { /* ignore */ }
      html5QrCode = null
    }
    showScanner.value = false
  }

  onUnmounted(() => { stopScan() })

  return {
    showScanner,
    scanning,
    startCameraScan,
    scanFromFile,
    stopScan
  }
}