import request from '@/utils/request'
export function accessoryInbound(data) { return request.post('/api/accessories/inbound', data) }
export function getAccessoryByBarcode(barcode) { return request.get('/api/accessories/' + barcode) }