import http from "./http";

export function getDepartment() {
  return http.get("/departments");
}