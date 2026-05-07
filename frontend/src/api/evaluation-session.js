import http from "./http"

/**
 * Evaluation Sessions API
 * Maps to backend EvaluationSessionController endpoints
 */

/**
 * Create evaluation session (manual)
 * @param {EvaluationSessionCreateRequest} data - Session creation data
 * @returns {Promise} API response
 */
export function createEvaluationSession(data) {
    return http.post("/evaluation-sessions", data)
}

/**
 * Auto-generate evaluation session from weekly reports
 * @param {number} internId - Intern ID
 * @param {SessionType} sessionType - Session type (FIRST_TERM, MID_TERM, FINAL)
 * @returns {Promise} API response
 */
export function generateEvaluationSession(internId, sessionType) {
    return http.post("/evaluation-sessions/generate", null, {
        params: { internId, sessionType }
    })
}

/**
 * Get all evaluation sessions with pagination
 * @param {Object} params - Query parameters
 * @param {number} [params.internId] - Filter by intern ID
 * @param {number} [params.page=0] - Page number
 * @param {number} [params.limit=10] - Page size
 * @returns {Promise} API response
 */
export function getEvaluationSessions(params = {}) {
    return http.get("/evaluation-sessions", { params })
}

/**
 * Get evaluation session by ID
 * @param {number} id - Session ID
 * @returns {Promise} API response
 */
export function getEvaluationSessionById(id) {
    return http.get(`/evaluation-sessions/${id}`)
}

/**
 * Update evaluation session
 * @param {number} id - Session ID
 * @param {EvaluationSessionUpdateRequest} data - Update data
 * @returns {Promise} API response
 */
export function updateEvaluationSession(id, data) {
    return http.put(`/evaluation-sessions/${id}`, data)
}

/**
 * Delete evaluation session
 * @param {number} id - Session ID
 * @returns {Promise} API response
 */
export function deleteEvaluationSession(id) {
    return http.delete(`/evaluation-sessions/${id}`)
}

/**
 * Get evaluation summary for an intern
 * @param {number} internId - Intern ID
 * @returns {Promise} API response
 */
export function getInternEvaluationSummary(internId) {
    return http.get(`/evaluation-sessions/interns/${internId}/evaluation-summary`)
}
