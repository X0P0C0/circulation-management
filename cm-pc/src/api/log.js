import request from '@/utils/request'

export function getLogs(params) {
  return request.get('/api/logs', { params })
}