import http from "./http";

export function getMentors() {
  return http.get("/users");
}

export function getMentorsForSelect() {
  return http.get("/users");
}

export function createMentor(data) {
  return http.post("/users", data);
}

export function updateMentor(id, data) {
  return http.put(`/users/${id}`, data);
}

export function toggleUserStatus(id) {
  return http.post(`/users/status/${id}`);
}

