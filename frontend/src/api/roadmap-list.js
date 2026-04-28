import http from "./http";

export function fetchRoadmapList(page = 0, size = 10) {
  return http.get("/roadmaps/list", { params: { page, size } });
}
