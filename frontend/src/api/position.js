import http from "./http"

export function getPositions(params = {}) {
    return http.get("/positions", { params })
}

export function getPositionById(id) {
    return http.get(`/positions/${id}`)
}

export function createPosition(data) {
    return http.post("/positions", data)
}

export function updatePosition(id, data) {
    return http.put(`/positions/${id}`, data)
}
