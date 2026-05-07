import { ref } from 'vue'

/**
 * Composable for managing loading states
 * @param {Object} options - Configuration options
 * @param {boolean} options.initialValue - Initial loading state (default: false)
 * @returns {Object} Loading state and methods
 */
export function useLoading(options = {}) {
  const { initialValue = false } = options

  const loading = ref(initialValue)
  const loadingStates = ref({})

  function setLoading(value) {
    loading.value = value
  }

  function startLoading() {
    loading.value = true
  }

  function stopLoading() {
    loading.value = false
  }

  function toggleLoading() {
    loading.value = !loading.value
  }

  /**
   * Set loading state for a specific key
   * @param {string} key - Key identifier
   * @param {boolean} value - Loading state
   */
  function setLoadingState(key, value) {
    loadingStates.value[key] = value
  }

  /**
   * Get loading state for a specific key
   * @param {string} key - Key identifier
   * @returns {boolean} Loading state
   */
  function isLoading(key) {
    if (key) {
      return loadingStates.value[key] || false
    }
    return loading.value
  }

  /**
   * Execute async function with loading state
   * @param {Function} asyncFn - Async function to execute
   * @param {string} key - Optional key for specific loading state
   * @returns {Promise} Promise from async function
   */
  async function withLoading(asyncFn, key = null) {
    try {
      if (key) {
        setLoadingState(key, true)
      } else {
        startLoading()
      }
      return await asyncFn()
    } finally {
      if (key) {
        setLoadingState(key, false)
      } else {
        stopLoading()
      }
    }
  }

  /**
   * Reset all loading states
   */
  function reset() {
    loading.value = initialValue
    loadingStates.value = {}
  }

  return {
    loading,
    loadingStates,
    setLoading,
    startLoading,
    stopLoading,
    toggleLoading,
    setLoadingState,
    isLoading,
    withLoading,
    reset
  }
}
