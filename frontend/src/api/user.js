import http from "./http";

// Mentor API functions
export function getMentors(params) {
  return http.get("/users/mentors", { params });
}

export function getMentorsForSelect() {
  return http.get("/users/mentors", { params: { limit: 100 } });
}

// HR API functions
export function getHRs(params) {
  return http.get("/users/hrs", { params });
}

// Common user API functions
export function getMyInfo() {
  return http.get("/users/get-my-info");
}

export function createUser(data) {
  return http.post("/users", data);
}

export function updateUser(id, data) {
  return http.put(`/users/${id}`, data);
}

export function toggleUserStatus(id) {
  return http.patch(`/users/status/${id}`);
}

// Alias for backward compatibility
export const createMentor = createUser;
export const updateMentor = updateUser;

export function getMentorsByDepartment(departmentId) {
  return http.get(`/users/departments/${departmentId}/mentors`);
}

export function permanentDeleteUser(id) {
  return http.delete(`/users/${id}/permanent`);
}