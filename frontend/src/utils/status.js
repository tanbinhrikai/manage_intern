import { InternStatus } from '@/types/common'

/**
 * Get Element Plus tag type for intern status
 * @param {keyof typeof InternStatus} status - Intern status
 * @returns {string} Element Plus tag type
 */
export function getInternStatusType(status) {
  const map = {
    ACTIVE: 'success',
    WARNING: 'warning',
    COMPLETED: 'primary',
    DROPPED: 'danger'
  }
  return map[status] || 'info'
}

/**
 * Get all intern status options
 * @returns {string[]} Array of status values
 */
export function getInternStatusOptions() {
  return Object.values(InternStatus)
}
