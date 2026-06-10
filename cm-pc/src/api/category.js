import request from '@/utils/request'

export function getCategories(params) {
  return request.get('/api/categories', { params })
}

export function createCategory(data) {
  return request.post('/api/categories', data)
}

export function updateCategory(id, data) {
  return request.put('/api/categories/' + id, data)
}

export function deleteCategory(id) {
  return request.delete('/api/categories/' + id)
}


export function moveCategorySort(id, direction) {
  return request.put('/api/categories/' + id + '/move', null, { params: { direction } })
}

export function resortCategories() {
  return request.put('/api/categories/resort')
}


export function getTopCategories() {
  return request.get('/api/categories/top')
}

export function batchSortCategories(ids) {
  return request.put('/api/categories/batch-sort', ids)
}
