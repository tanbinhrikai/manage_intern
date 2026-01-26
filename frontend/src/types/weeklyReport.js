/**
 * @typedef {Object} WeeklyReportDetail
 * @property {number} [id]
 * @property {number} criteriaId
 * @property {string} [criteriaName]
 * @property {number|null} score
 * @property {string} [comment]
 */

/**
 * @typedef {Object} WeeklyReportDetailRequest
 * @property {number} criteriaId
 * @property {number|null} score
 * @property {string} [comment]
 */

/**
 * @typedef {Object} WeeklyReport
 * @property {number} id
 * @property {number} internId
 * @property {string} [internName]
 * @property {string} [mentorId]
 * @property {string} [mentorName]
 * @property {number} weekNumber
 * @property {string} weekStartDate
 * @property {string} [tasksAssigned]
 * @property {string} [tasksCompleted]
 * @property {string} [issuesRisks]
 * @property {string} [mentorOverallComment]
 * @property {number|null} [averageScore]
 * @property {WeeklyReportDetail[]} [details]
 * @property {string} [createdAt]
 * @property {string} [updatedAt]
 */

/**
 * @typedef {Object} WeeklyReportRequest
 * @property {number} internId
 * @property {string} weekStartDate
 * @property {string} [weekEndDate]
 * @property {string} [tasksAssigned]
 * @property {string} [tasksCompleted]
 * @property {string} [issuesRisks]
 * @property {string} [mentorOverallComment]
 * @property {WeeklyReportDetailRequest[]} [details]
 */

/**
 * @typedef {Object} WeeklyReportUpdateRequest
 * @property {string} [weekStartDate]
 * @property {string} [tasksAssigned]
 * @property {string} [tasksCompleted]
 * @property {string} [issuesRisks]
 * @property {string} [mentorOverallComment]
 * @property {WeeklyReportDetailRequest[]} [details]
 */

/**
 * @param {number} [criteriaId]
 * @returns {WeeklyReportDetailRequest}
 */
export const createWeeklyReportDetailRequest = (criteriaId = null) => ({
    criteriaId,
    score: null,
    comment: ""
})

/**
 * @param {number} [internId]
 * @returns {WeeklyReportRequest & { weekEndDate: string }}
 */
export const createWeeklyReportRequest = (internId = null) => ({
    internId,
    weekStartDate: "",
    weekEndDate: "",
    tasksAssigned: "",
    tasksCompleted: "",
    issuesRisks: "",
    mentorOverallComment: "",
    details: []
})

/**
 * @returns {WeeklyReportUpdateRequest}
 */
export const createWeeklyReportUpdateRequest = () => ({
    weekStartDate: "",
    tasksAssigned: "",
    tasksCompleted: "",
    issuesRisks: "",
    mentorOverallComment: "",
    details: []
})
