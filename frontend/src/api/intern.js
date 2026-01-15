import http from "./http"

export function getInterns(params = {}) {
    return http.get("/interns", { params })
}

export function getInternById(id) {
    return http.get(`/interns/${id}`)
}

export function createIntern(data) {
    return http.post("/interns", data)
}

export function updateIntern(id, data) {
    return http.put(`/interns/${id}`, data)
}

export function deleteIntern(id) {
    return http.delete(`/interns/${id}`)
}

export function getInternsByStatus(status, params = {}) {
    return http.get(`/interns/status/${status}`, { params })
}

export function getInternsByPosition(positionId, params = {}) {
    return http.get(`/interns/position/${positionId}`, { params })
}

export function getInternsByMentor(mentorId, params = {}) {
    return http.get(`/interns/mentor/${mentorId}`, { params })
}
