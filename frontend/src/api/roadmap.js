import http from "./http";

export function chatProcess(payload) {
  return http.post("/roadmaps/chat-process", payload);
}
export function chatSimple(payload) {
  return http.post("/roadmaps/chat", payload);
}
export function confirmDraft(sessionId) {
  return http.post(`/roadmaps/drafts/${sessionId}/confirm`);
}

export function fetchRoadmapById(id) {
  return http.get(`/roadmaps/${id}`);
}

export function deleteRoadmap(id) {
  return http.delete(`/roadmaps/${id}`);
}
