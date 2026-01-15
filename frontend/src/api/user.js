import http from "./http";

export function getMentors(params) {
  return http.get("/users", { params });
}

export function getMentorsForSelect() {
  return http.get("/users");
}

export function getMyInfo() {
  return http.get("/users/get-my-info");
}

export function createMentor(data) {
  return http.post("/users", data);
}

export function updateMentor(id, data) {
  return http.put(`/users/${id}`, data);
}

export function toggleUserStatus(id) {
  return http.patch(`/users/status/${id}`);
}


