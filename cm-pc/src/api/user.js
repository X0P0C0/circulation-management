import request from '@/utils/request'

export function getAllUsers() {
  return request.get('/api/user/list')
}