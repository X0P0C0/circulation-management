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

export function importAccessories(formData) {
  return request.post('/api/accessories/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export function getInventoryGroup(params) {
  return request.get('/api/accessories/inventory-group', { params })
}

export function deleteAccessory(id) {
  return request.delete('/api/accessories/' + id)
}

export function updateAccessory(data) {
  return request.put('/api/accessories', data)
}

export function importExcel(formData) {
  return request.post('/api/accessories/import-excel', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export function getAccessoriesByIds(ids) {
  return request.post('/api/accessories/batch', ids)
}

export function getAccessoriesByItemCodes(itemCodes) {
  return request.post('/api/accessories/batch-by-code', itemCodes)
}
