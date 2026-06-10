import request from '@/utils/request'

export function getShelves(params) {
  return request.get('/api/shelves', { params })
}

export function createShelf(data) {
  return request.post('/api/shelves', data)
}

export function updateShelf(id, data) {
  return request.put('/api/shelves/' + id, data)
}

export function deleteShelf(id) {
  return request.delete('/api/shelves/' + id)
}
