import { SessionType, EvaluationConclusion } from './common.js'

/**
 * @typedef {Object} EvaluationScore
 * @property {number} [id]
 * @property {number} criteriaId
 * @property {string} [criteriaName]
 * @property {string} [criteriaCategory]
 * @property {number|null} score
 * @property {string} [comment]
 */

/**
 * @typedef {Object} EvaluationSession
 * @property {number} id
 * @property {number} internId
 * @property {string} [internName]
 * @property {string} [mentorId]
 * @property {string} [mentorName]
 * @property {keyof typeof SessionType} sessionType
 * @property {string} evaluationDate
 * @property {number|null} [finalScore]
 * @property {string} [levelAssessment]
 * @property {keyof typeof EvaluationConclusion} [conclusion]
 * @property {string} [overallComment]
 * @property {EvaluationScore[]} [scores]
 * @property {string} [createdAt]
 * @property {string} [updatedAt]
 */

/**
 * @typedef {Object} EvaluationScoreRequest
 * @property {number} criteriaId
 * @property {number|null} score
 * @property {string} [comment]
 */

/**
 * @typedef {Object} EvaluationSessionCreateRequest
 * @property {number} internId
 * @property {keyof typeof SessionType} sessionType
 * @property {string} evaluationDate
 * @property {string} [overallComment]
 * @property {EvaluationScoreRequest[]} [scores]
 */

/**
 * @typedef {Object} EvaluationSessionUpdateRequest
 * @property {string} [evaluationDate]
 * @property {number|null} [finalScore]
 * @property {string} [levelAssessment]
 * @property {keyof typeof EvaluationConclusion} [conclusion]
 * @property {string} [overallComment]
 * @property {EvaluationScoreRequest[]} [scores]
 */

/**
 * @returns {EvaluationScoreRequest}
 */
export const createEvaluationScoreRequest = () => ({
    criteriaId: null,
    score: null,
    comment: ""
})

/**
 * @param {number} [internId]
 * @returns {EvaluationSessionCreateRequest}
 */
export const createEvaluationSessionCreateRequest = (internId = null) => ({
    internId,
    sessionType: SessionType.FIRST_TERM,
    evaluationDate: "",
    overallComment: "",
    scores: []
})

/**
 * @returns {EvaluationSessionUpdateRequest}
 */
export const createEvaluationSessionUpdateRequest = () => ({
    evaluationDate: "",
    finalScore: null,
    levelAssessment: "",
    conclusion: null,
    overallComment: "",
    scores: []
})

/**
 * @typedef {Object} WeeklyScoreData
 * @property {string} weekStartDate
 * @property {number|null} averageScore
 */

/**
 * @typedef {Object} InternEvaluationSummaryResponse
 * @property {number} internId
 * @property {string} internName
 * @property {string} startDate
 * @property {string} endDate
 * @property {number} totalWeeklyReports
 * @property {number|null} averageWeeklyScore
 * @property {EvaluationSession|null} firstTermSession
 * @property {EvaluationSession|null} midTermSession
 * @property {EvaluationSession|null} finalSession
 * @property {number|null} overallFinalScore
 * @property {string} overallLevel
 * @property {string} overallConclusion
 * @property {WeeklyScoreData[]} weeklyScoreTrend
 */
