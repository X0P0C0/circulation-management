import request from '@/utils/request'
export function getAllWorkers() { return request.get('/api/workers/all') }