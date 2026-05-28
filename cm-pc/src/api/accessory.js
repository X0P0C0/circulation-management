import request from '@/utils/request'

export function accessoryInbound(data) {
  return request.post('/api/accessories/inbound', data)
}

export function getAccessoryById(id) {
  return request.get('/api/accessories/' + id)
}

export function getAccessoryByBarcode(barcode) {
  return request.get('/api/accessories/barcode/' + barcode)
}

export function searchAccessories(params) {
  return request.get('/api/accessories/search', { params })
}

export function getInventoryGroup(params) {
  return request.get('/api/accessories/inventory-group', { params })
}