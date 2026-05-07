import { ref } from 'vue'
import { getDepartments } from '@/api/department'
import { getPositions } from '@/api/position'
import { getMentors, getHRs } from '@/api/user'
import { getInternshipBatches } from '@/api/internship-batch'
import { getInterns } from '@/api/intern'
import { useApi } from './useApi'

/**
 * Composable for fetching dropdown data
 * @param {Object} options - Configuration options
 * @param {boolean} options.autoFetch - Auto fetch on mount (default: false)
 * @param {number} options.limit - Limit for fetching (default: 100)
 * @returns {Object} Dropdown data and fetch methods
 */
export function useDropdownData(options = {}) {
  const { autoFetch = false, limit = 100 } = options
  
  const { execute: executeApi } = useApi({
    showErrorMessage: false
  })

  const departments = ref([])
  const positions = ref([])
  const mentors = ref([])
  const hrs = ref([])
  const internshipBatches = ref([])
  const interns = ref([])

  /**
   * Fetch departments
   */
  async function fetchDepartments() {
    try {
      const res = await executeApi(() => getDepartments({ limit }))
      departments.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch positions
   */
  async function fetchPositions() {
    try {
      const res = await executeApi(() => getPositions({ limit }))
      positions.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch mentors
   * @param {Object} params - Additional params
   */
  async function fetchMentors(params = {}) {
    try {
      const res = await executeApi(() => getMentors({ limit, ...params }))
      mentors.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch HR users
   * @param {Object} params - Additional params
   */
  async function fetchHRs(params = {}) {
    try {
      const res = await executeApi(() => getHRs({ limit, ...params }))
      hrs.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch internship batches
   */
  async function fetchInternshipBatches() {
    try {
      const res = await executeApi(() => getInternshipBatches({ limit }))
      internshipBatches.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch interns
   * @param {Object} params - Additional params
   */
  async function fetchInterns(params = {}) {
    try {
      const res = await executeApi(() => getInterns({ limit, ...params }))
      interns.value = res.data?.data?.items || []
    } catch (error) {
      // Error silently handled
    }
  }

  /**
   * Fetch all dropdown data
   */
  async function fetchAll() {
    await Promise.all([
      fetchDepartments(),
      fetchPositions(),
      fetchMentors(),
      fetchInternshipBatches()
    ])
  }

  return {
    departments,
    positions,
    mentors,
    hrs,
    internshipBatches,
    interns,
    fetchDepartments,
    fetchPositions,
    fetchMentors,
    fetchHRs,
    fetchInternshipBatches,
    fetchInterns,
    fetchAll
  }
}
