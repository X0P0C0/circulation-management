import axios from 'axios'
import { getToken, removeToken } from './auth'
import { showToast } from 'vant'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = Bearer 
  return config
})

service.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) return response.data
    showToast(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
      showToast('登录已过期')
    } else {
      showToast(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service