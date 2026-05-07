import http from "./http"

export function getDepartments(params = {}) {
  return http.get("/departments", { params })
}

export function getDepartmentById(id) {
  return http.get(`/departments/${id}`)
}

export function createDepartment(data) {
  return http.post("/departments", data)
}

export function updateDepartment(id, data) {
  return http.put(`/departments/${id}`, data)
}

export function deleteDepartment(id) {
  return http.delete(`/departments/${id}`)
}