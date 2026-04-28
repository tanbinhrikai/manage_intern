/**
 * Common enums - Maps to backend enums
 */

/**
 * @enum {string}
 */
export const InternStatus = {
  ACTIVE: 'ACTIVE',
  WARNING: 'WARNING',
  COMPLETED: 'COMPLETED',
  DROPPED: 'DROPPED'
}

/**
 * @enum {string}
 */
export const SessionType = {
  FIRST_TERM: 'FIRST_TERM',
  MID_TERM: 'MID_TERM',
  FINAL: 'FINAL'
}

/**
 * @enum {string}
 */
export const EvaluationConclusion = {
  PASS: 'PASS',
  NEED_IMPROVEMENT: 'NEED_IMPROVEMENT',
  FAIL: 'FAIL'
}

/**
 * @enum {string}
 */
export const CriteriaCategory = {
  WORK_PERFORMANCE: 'WORK_PERFORMANCE',
  ATTITUDE_SOFT_SKILLS: 'ATTITUDE_SOFT_SKILLS',
  KNOWLEDGE_APPLICATION: 'KNOWLEDGE_APPLICATION'
}

/**
 * @enum {string}
 */
export const ScoreLabel = {
  EXCELLENT: 'EXCELLENT',
  GOOD: 'GOOD',
  AVERAGE: 'AVERAGE',
  WEAK: 'WEAK'
}

/**
 * @enum {string}
 */
export const RoleType = {
  ADMIN: 'ADMIN',
  MENTOR: 'MENTOR',
  HR: 'HR'
}
