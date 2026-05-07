/**
 * @typedef {Object} Role
 * @property {string} roleName
 * @property {string} [description]
 */

/**
 * @typedef {Object} User
 * @property {string} id
 * @property {string} email
 * @property {string} fullName
 * @property {string} [dateOfBirth]
 * @property {boolean} isActive
 * @property {string} [createdAt]
 * @property {string} [updatedAt]
 * @property {Role} [role]
 * @property {import('./department').Department} [department]
 */

/**
 * @typedef {Object} Mentor
 * @property {string} id
 * @property {string} email
 * @property {string} fullName
 * @property {string} [dateOfBirth]
 */

/**
 * @typedef {Object} UserCreationRequest
 * @property {string} email
 * @property {string} password
 * @property {string} fullName
 * @property {keyof typeof import('./common').RoleType} roleName
 * @property {string} [dateOfBirth]
 * @property {number} departmentId
 */

/**
 * @typedef {Object} UserUpdateRequest
 * @property {string} email
 * @property {string} password
 * @property {string} fullName
 * @property {string} [dateOfBirth]
 * @property {boolean} isActive
 * @property {number} departmentId
 */

/**
 * @param {string} [roleName]
 * @param {number} [departmentId]
 * @returns {UserCreationRequest}
 */
export const createUserCreationRequest = (roleName = "", departmentId = null) => ({
    email: "",
    password: "",
    fullName: "",
    roleName,
    dateOfBirth: "",
    departmentId
})

/**
 * @param {number} [departmentId]
 * @returns {UserUpdateRequest}
 */
export const createUserUpdateRequest = (departmentId = null) => ({
    email: "",
    password: "",
    fullName: "",
    dateOfBirth: "",
    isActive: true,
    departmentId
})
