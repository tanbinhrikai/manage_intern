/**
 * @typedef {Object} Position
 * @property {number} id
 * @property {string} title
 */

/**
 * @typedef {Object} PositionCreationRequest
 * @property {string} title
 */

/**
 * @typedef {Object} PositionUpdateRequest
 * @property {string} title
 */

/**
 * @returns {PositionCreationRequest}
 */
export const createPositionCreationRequest = () => ({
    title: ""
})

/**
 * @returns {PositionUpdateRequest}
 */
export const createPositionUpdateRequest = () => ({
    title: ""
})
