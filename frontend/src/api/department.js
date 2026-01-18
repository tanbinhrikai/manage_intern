import http from "./http"

export function getDepartments() {
  return http.get("/departments")
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