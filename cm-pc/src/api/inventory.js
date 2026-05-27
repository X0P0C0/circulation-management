import request from '@/utils/request'

export function getInventories(params) {
  return request.get('/api/inventories', { params })
}

export function getInventoryStats() {
  return request.get('/api/inventories/stats')
}

export function exportInventoryCsv(params) {
  const query = new URLSearchParams(params).toString()
  const base = import.meta.env.VITE_API_BASE_URL || ''
  const token = localStorage.getItem('cm_token')
  window.open(base + '/api/inventories/export?' + query + '&_token=' + token, '_blank')
}