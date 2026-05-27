import { ref, onUnmounted } from 'vue'
import { showToast } from 'vant'

/**
 * 条码扫描组合式函数
 * 支持：摄像头实时扫描、相册选图识别
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

  /** 从图片文件识别条码 */
  const scanFromFile = (file) => {
    return new Promise((resolve, reject) => {
      scanning.value = true
      const reader = new FileReader()
      reader.onload = async (e) => {
        try {
          const { Html5Qrcode } = await import('html5-qrcode')
          const qr = new Html5Qrcode('qr-temp')
          try {
            const result = await qr.scanFile(file, true)
            scanning.value = false
            resolve(result)
          } catch (err) {
            scanning.value = false
            showToast('未能识别到条码，请尝试更清晰的图片')
            resolve(null)
          }
        } catch (e) {
          scanning.value = false
          showToast('条码识别组件加载失败')
          resolve(null)
        }
      }
      reader.onerror = () => {
        scanning.value = false
        reject(new Error('文件读取失败'))
      }
      reader.readAsDataURL(file)
    })
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