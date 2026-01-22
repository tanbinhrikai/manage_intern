import http from "./http"

// Criteria Groups
export function getCriteriaGroups() {
    return http.get("/criteria-groups")
}

export function getCriteriaGroupById(id) {
    return http.get(`/criteria-groups/${id}`)
}

export function createCriteriaGroup(data) {
    return http.post("/criteria-groups", data)
}

export function updateCriteriaGroup(id, data) {
    return http.put(`/criteria-groups/${id}`, data)
}

export function deleteCriteriaGroup(id) {
    return http.delete(`/criteria-groups/${id}`)
}

// Evaluation Criteria
export function getEvaluationCriteria() {
    return http.get("/evaluation-criteria")
}

export function getEvaluationCriteriaById(id) {
    return http.get(`/evaluation-criteria/${id}`)
}

export function getMainCriteria() {
    return http.get("/evaluation-criteria/main")
}

export function getSubCriteria() {
    return http.get("/evaluation-criteria/sub")
}

export function getSubCriteriaByParentId(parentId) {
    return http.get(`/evaluation-criteria/${parentId}/sub-criteria`)
}

export function getCriteriaByGroupId(groupId) {
    return http.get(`/evaluation-criteria/group/${groupId}`)
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

// Score Labels
export function getScoreLabels() {
    return http.get("/evaluation-criteria/score-labels")
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
