import http from "./http";

export function getMentors() {
  return http.get("/users");
}
export function createMentor(data) {
  return http.post("/users", data);
}
export function updateMentor(id, data) {
  return http.put(`/users/${id}`, data);
}
// export function lockMentor(id, isActive) {
//   return http.patch('/users/status', { id, isActive })
// @PostMapping("/status/{id}/{active}")
export function lockMentor(id, isActive) {
  return http.patch(`/users/status/${id}/${isActive}`);
}
