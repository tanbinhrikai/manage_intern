import http from "./http"

// Weekly Reports
export function getWeeklyReports(params = {}) {
    return http.get("/weekly-reports", { params })
}

export function getWeeklyReportById(id) {
    return http.get(`/weekly-reports/${id}`)
}

export function getWeeklyReportsByInternId(internId, params) {
    return http.get(`/weekly-reports/intern/${internId}`, {
        params: params
    })
}

export function createWeeklyReport(data) {
    return http.post("/weekly-reports", data)
}

export function updateWeeklyReport(id, data) {
    return http.put(`/weekly-reports/${id}`, data)
}

export function deleteWeeklyReport(id) {
    return http.delete(`/weekly-reports/${id}`)
}

export function getMyReports(params = {}) {
    return http.get("/weekly-reports/my-reports", { params })
}
