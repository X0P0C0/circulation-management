import request from '@/utils/request'
export function transferOut(data) { return request.post('/api/flows/transfer-out', data) }
export function transferIn(data) { return request.post('/api/flows/transfer-in', data) }
export function sellAccessory(data) { return request.post('/api/flows/sell', data) }
export function traceBarcode(barcode) { return request.get('/api/flows/trace', { params: { barcode } }) }
export function traceItem(itemCode) { return request.get('/api/flows/trace/' + itemCode) }
export function getFlowRecords(params) { return request.get('/api/flows/records', { params }) }