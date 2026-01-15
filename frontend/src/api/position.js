import http from "./http"

export function getPositions() {
    return http.get("/positions")
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
