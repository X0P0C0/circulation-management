import request from '@/utils/request'

export function getWorkers(params) {
  return request.get('/api/workers', { params })
}

export function getAllWorkers() {
  return request.get('/api/workers/all')
}

export function createWorker(data) {
  return request.post('/api/workers', data)
}

export function updateWorker(id, data) {
  return request.put('/api/workers/' + id, data)
}

export function deleteWorker(id) {
  return request.delete('/api/workers/' + id)
}

export function getWorkerInventory(id) {
  return request.get('/api/workers/' + id + '/inventory')
}