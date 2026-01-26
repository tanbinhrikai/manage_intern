import { InternStatus } from './common.js'

/**
 * @typedef {Object} Intern
 * @property {number} id
 * @property {string} fullName
 * @property {import('./position').Position} [position]
 * @property {import('./user').Mentor} [mentor]
 * @property {string} startDate
 * @property {string} endDate
 * @property {keyof typeof InternStatus} internStatus
 * @property {string} [createdAt]
 * @property {string} [updatedAt]
 */

/**
 * @typedef {Object} InternCreationRequest
 * @property {string} fullName
 * @property {number} positionId
 * @property {string} mentorId
 * @property {string} startDate
 * @property {string} endDate
 * @property {number} internshipBatchId
 */

/**
 * @typedef {Object} InternUpdateRequest
 * @property {string} fullName
 * @property {number} positionId
 * @property {string} mentorId
 * @property {string} startDate
 * @property {string} endDate
 * @property {keyof typeof InternStatus} internStatus
 */

/**
 * @param {number} [internshipBatchId]
 * @returns {InternCreationRequest}
 */
export const createInternCreationRequest = (internshipBatchId = null) => ({
    fullName: "",
    positionId: null,
    mentorId: "",
    startDate: "",
    endDate: "",
    internshipBatchId
})

/**
 * @returns {InternUpdateRequest}
 */
export const createInternUpdateRequest = () => ({
    fullName: "",
    positionId: null,
    mentorId: "",
    startDate: "",
    endDate: "",
    internStatus: InternStatus.ACTIVE
})
