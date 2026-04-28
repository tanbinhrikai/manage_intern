import http from './http'

export function getBatches(params = {}) {
  return http.get('/internship-batches', { params })
}

// Backward-compatible alias
export function getInternshipBatches(params = {}) {
  return getBatches(params)
}

export function getBatchById(id) {
  return http.get(`/internship-batches/${id}`)
}

export function createBatch(data) {
  return http.post('/internship-batches', data)
}

export function updateBatch(id, data) {
  return http.put(`/internship-batches/${id}`, data)
}

export function deleteBatch(id) {
  return http.delete(`/internship-batches/${id}`)
}

export function getInternsByBatch(batchId, params = {}) {
  return http.get(`/interns/batch/${batchId}`, { params })
}
