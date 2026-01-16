import http from "./http"

// Evaluation Criteria
export function getEvaluationCriteria() {
    return http.get("/evaluation-criteria")
}

export function getEvaluationCriteriaById(id) {
    return http.get(`/evaluation-criteria/${id}`)
}

export function createEvaluationCriteria(data) {
    return http.post("/evaluation-criteria", data)
}

export function updateEvaluationCriteria(id, data) {
    return http.put(`/evaluation-criteria/${id}`, data)
}

export function deleteEvaluationCriteria(id) {
    return http.delete(`/evaluation-criteria/${id}`)
}

// Score Definitions
export function getScoreDefinitions() {
    return http.get("/criteria-score-definitions")
}

export function getScoreDefinitionsByCriteriaId(criteriaId) {
    return http.get(`/criteria-score-definitions/criteria/${criteriaId}`)
}

export function getScoreDefinitionById(id) {
    return http.get(`/criteria-score-definitions/${id}`)
}

export function createScoreDefinition(data) {
    return http.post("/criteria-score-definitions", data)
}

export function updateScoreDefinition(id, data) {
    return http.put(`/criteria-score-definitions/${id}`, data)
}

export function deleteScoreDefinition(id) {
    return http.delete(`/criteria-score-definitions/${id}`)
}
