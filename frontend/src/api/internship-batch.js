import http from "./http";

export function getInternshipBatches (params = {}) {
    return http.get("/internship-batches", { params });
}