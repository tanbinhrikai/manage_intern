/**
 * @typedef {Object} Department
 * @property {number} id
 * @property {string} title
 */

/**
 * @typedef {Object} DepartmentCreationRequest
 * @property {string} title
 */

/**
 * @typedef {Object} DepartmentUpdateRequest
 * @property {string} title
 */

/**
 * @returns {DepartmentCreationRequest}
 */
export const createDepartmentCreationRequest = () => ({
    title: ""
})

/**
 * @returns {DepartmentUpdateRequest}
 */
export const createDepartmentUpdateRequest = () => ({
    title: ""
})
