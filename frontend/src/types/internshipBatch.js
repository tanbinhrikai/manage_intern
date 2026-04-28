/**
 * @enum {string}
 */
export const BatchStatus = {
    ACTIVE: 'ACTIVE',
    INACTIVE: 'INACTIVE',
    COMPLETED: 'COMPLETED'
}

/**
 * @typedef {Object} InternshipBatch
 * @property {number} id
 * @property {string} name
 * @property {string} startDate
 * @property {string} endDate
 * @property {string} [description]
 * @property {keyof typeof BatchStatus} [status]
 * @property {string} [createdAt]
 * @property {import('./intern').Intern[]} [internResponses]
 */

/**
 * @typedef {Object} InternshipBatchCreationRequest
 * @property {string} name
 * @property {string} startDate
 * @property {string} endDate
 * @property {string} [description]
 */

/**
 * @typedef {Object} InternshipBatchUpdateRequest
 * @property {string} [name]
 * @property {string} [startDate]
 * @property {string} [endDate]
 * @property {string} [description]
 * @property {keyof typeof BatchStatus} [batchStatus]
 * @property {string} [picMentorId]
 */

/**
 * @returns {InternshipBatchCreationRequest}
 */
export const createInternshipBatchCreationRequest = () => ({
    name: "",
    startDate: "",
    endDate: "",
    description: ""
})

/**
 * @returns {InternshipBatchUpdateRequest}
 */
export const createInternshipBatchUpdateRequest = () => ({
    name: "",
    startDate: "",
    endDate: "",
    description: "",
    batchStatus: null,
    picMentorId: ""
})
