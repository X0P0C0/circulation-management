import request from '@/utils/request'
export function getInventories(params) { return request.get('/api/inventories', { params }) }