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

export function generatePhases(payload) {
  return http.post("/roadmaps/generate-phases", payload);
}

export function expandNode(payload) {
  return http.post("/roadmaps/nodes/expand", payload);
}

export function addNode(payload) {
  return http.post("/roadmaps/nodes", payload);
}

export function editNodeDetail(id, payload) {
  return http.put(`/roadmaps/nodes/${id}`, payload);
}

export function removeNode(id) {
  return http.delete(`/roadmaps/nodes/${id}`);
}

export function moveNode(id, payload) {
  return http.patch(`/roadmaps/nodes/${id}/move`, payload);
}

export function saveDraftRoadmap(payload) {
  return http.post("/roadmaps/drafts", payload);
}
export function fetchRoadmaps() { return http.get("/roadmaps/list"); }
