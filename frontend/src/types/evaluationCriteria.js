import { CriteriaCategory, ScoreLabel } from './common.js'

/**
 * @typedef {Object} ScoreDefinition
 * @property {number} [id]
 * @property {keyof typeof ScoreLabel} scoreLabel
 * @property {number} minScore
 * @property {number} maxScore
 * @property {string} description
 */

/**
 * @typedef {Object} EvaluationCriteria
 * @property {number} id
 * @property {string} name
 * @property {string} [description]
 * @property {keyof typeof CriteriaCategory} category
 * @property {string} [categoryDisplayName]
 * @property {string} [categoryDescription]
 * @property {number} [parentId]
 * @property {string} [parentName]
 * @property {number} [displayOrder]
 * @property {boolean} [isActive]
 * @property {ScoreDefinition[]} [scoreDefinitions]
 * @property {EvaluationCriteria[]} [children]
 */

/**
 * @typedef {Object} CriteriaGroup
 * @property {number} id
 * @property {string} name
 * @property {number} [displayOrder]
 * @property {EvaluationCriteria[]} [mainCriteria]
 */

/**
 * @typedef {Object} CriteriaGroupCreationRequest
 * @property {string} name
 * @property {number} displayOrder
 */

/**
 * @typedef {Object} CriteriaGroupUpdateRequest
 * @property {string} [name]
 * @property {number} [displayOrder]
 */

/**
 * @typedef {Object} EvaluationCriteriaCreationRequest
 * @property {number} groupId
 * @property {string} name
 * @property {string} [description]
 * @property {number} [weight]
 * @property {number} [parentId]
 * @property {number} [displayOrder]
 * @property {boolean} [isActive]
 */

/**
 * @typedef {Object} EvaluationCriteriaUpdateRequest
 * @property {number} [groupId]
 * @property {string} [name]
 * @property {string} [description]
 * @property {number} [weight]
 * @property {number} [parentId]
 * @property {number} [displayOrder]
 * @property {boolean} [isActive]
 */

/**
 * @typedef {Object} CriteriaScoreDefinitionCreationRequest
 * @property {number} criteriaId
 * @property {keyof typeof ScoreLabel} scoreLabel
 * @property {string} [description]
 */

/**
 * @typedef {Object} CriteriaScoreDefinitionUpdateRequest
 * @property {number} criteriaId
 * @property {keyof typeof ScoreLabel} scoreLabel
 * @property {string} [description]
 */

/**
 * @param {number} [displayOrder]
 * @returns {CriteriaGroupCreationRequest}
 */
export const createCriteriaGroupCreationRequest = (displayOrder = 1) => ({
    name: "",
    displayOrder
})

/**
 * @returns {CriteriaGroupUpdateRequest}
 */
export const createCriteriaGroupUpdateRequest = () => ({
    name: "",
    displayOrder: 1
})

/**
 * @param {number} [groupId]
 * @param {number} [parentId]
 * @returns {EvaluationCriteriaCreationRequest}
 */
export const createEvaluationCriteriaCreationRequest = (groupId = null, parentId = null) => ({
    groupId,
    name: "",
    description: "",
    weight: 1.0,
    parentId,
    displayOrder: 1,
    isActive: true
})

/**
 * @returns {EvaluationCriteriaUpdateRequest}
 */
export const createEvaluationCriteriaUpdateRequest = () => ({
    groupId: null,
    name: "",
    description: "",
    weight: 1.0,
    parentId: null,
    displayOrder: 1,
    isActive: true
})

/**
 * @param {number} [criteriaId]
 * @returns {CriteriaScoreDefinitionCreationRequest}
 */
export const createCriteriaScoreDefinitionCreationRequest = (criteriaId = null) => ({
    criteriaId,
    scoreLabel: ScoreLabel.AVERAGE,
    description: ""
})

/**
 * @param {number} [criteriaId]
 * @returns {CriteriaScoreDefinitionUpdateRequest}
 */
export const createCriteriaScoreDefinitionUpdateRequest = (criteriaId = null) => ({
    criteriaId,
    scoreLabel: ScoreLabel.AVERAGE,
    description: ""
})
