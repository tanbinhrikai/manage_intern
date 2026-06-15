import http from "./http"

export const ACTIVITY_STREAM_URL = `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/dashboard/recent-activities/stream`

export function getRecentActivities(params = {}) {
    return http.get("/dashboard/recent-activities", { params })
}

export function getInternsByDepartment() {
    return http.get("/dashboard/chart/mentors-by-department")
}

export function getInternsByPosition() {
    return http.get("/dashboard/chart/interns-by-position")
}

export function getInternsTrend(params = {}) {
    return http.get("/dashboard/chart/interns-trend", { params })
}

export function getAverageScoreTrend(params = {}) {
    return http.get("/dashboard/chart/average-score-trend", { params })
}

export function getCompletionRateTrend(params = {}) {
    return http.get("/dashboard/chart/completion-rate-trend", { params })
}

// Multi-series chart APIs with groupBy support
export function getAverageScoreTrendByGroup(params = {}) {
    return http.get("/dashboard/chart/average-score-trend-by-group", { params })
}

export function getCompletionRateTrendByGroup(params = {}) {
    return http.get("/dashboard/chart/completion-rate-trend-by-group", { params })
}

// Batch score trend APIs (drill-down chart)
export function getBatchScoreTrend() {
    return http.get("/dashboard/chart/batch-score-trend")
}

export function getInternScoreTrendByBatch(batchId) {
    return http.get(`/dashboard/chart/batch/${batchId}/intern-score-trend`)
}

// Mentor dashboard APIs
export function getMentorStatistics() {
    return http.get("/dashboard/mentor/statistics")
}

export function getMentorInternStatusDistribution() {
    return http.get("/dashboard/mentor/chart/intern-status-distribution")
}
