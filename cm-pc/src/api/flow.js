import request from '@/utils/request'

export function flowOutbound(data) {
  return request.post('/api/flows/outbound', data)
}

export function flowReturn(data) {
  return request.post('/api/flows/return', data)
}

export function flowSell(data) {
  return request.post('/api/flows/sell', data)
}

export function flowTransfer(data) {
  return request.post('/api/flows/transfer', data)
}

export function traceByBarcode(barcode) {
  return request.get('/api/flows/trace', { params: { barcode } })
}

export function traceItem(itemCode) {
  return request.get('/api/flows/trace/' + itemCode)
}

export function getFlowRecords(params) {
  return request.get('/api/flows/records', { params })
}
export function getWorkerRecords(workerId) {
  return request.get('/api/flows/records', { params: { workerId } })
}