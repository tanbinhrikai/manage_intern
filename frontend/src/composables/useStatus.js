import { computed } from 'vue'
import { InternStatus } from '@/types/common'
import { getInternStatusType, getInternStatusOptions } from '@/utils/status'

/**
 * Composable for managing status-related functionality
 * @returns {Object} Status utilities
 */
export function useStatus() {
  /**
   * Get Element Plus tag type for intern status
   * @param {keyof typeof InternStatus} status - Intern status
   * @returns {string} Element Plus tag type
   */
  function getStatusType(status) {
    return getInternStatusType(status)
  }

  /**
   * Get all intern status options
   * @returns {string[]} Array of status values
   */
  const statusOptions = computed(() => getInternStatusOptions())

  return {
    getStatusType,
    statusOptions
  }
}
