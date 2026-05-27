import axios from 'axios'
import { getToken, removeToken } from './auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return response.data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service