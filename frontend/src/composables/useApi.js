import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'

/**
 * Composable for API calls with error handling
 * @param {Object} options - Configuration options
 * @param {boolean} options.showSuccessMessage - Show success message (default: false)
 * @param {boolean} options.showErrorMessage - Show error message (default: true)
 * @param {Function} options.onSuccess - Success callback
 * @param {Function} options.onError - Error callback
 * @returns {Object} API call methods
 */
export function useApi(options = {}) {
  const {
    showSuccessMessage = false,
    showErrorMessage = true,
    onSuccess = null,
    onError = null
  } = options

  const localeStore = useLocaleStore()
  const error = ref(null)
  const lastResponse = ref(null)

  /**
   * Execute API call with error handling
   * @param {Function} apiCall - API function to call
   * @param {string|boolean} successMessage - Success message key (for i18n) or true to show default, false/null to hide
   * @param {string|boolean} errorMessage - Error message key (for i18n) or true to show default, false/null to hide
   * @returns {Promise} Promise from API call
   */
  async function execute(apiCall, successMessage = null, errorMessage = true) {
    error.value = null
    try {
      const response = await apiCall()
      lastResponse.value = response

      // Show success message if enabled
      if (successMessage === true || (showSuccessMessage && successMessage)) {
        const message = typeof successMessage === 'string' 
          ? (localeStore.t(successMessage) || successMessage)
          : 'Operation completed successfully'
        ElMessage.success(message)
      }

      if (onSuccess) {
        onSuccess(response)
      }

      return response
    } catch (err) {
      error.value = err
      
      // Extract error message
      let message = null
      if (err.response?.data?.message) {
        message = err.response.data.message
      } else if (errorMessage === true) {
        message = err.message || 'An error occurred'
      } else if (typeof errorMessage === 'string') {
        message = localeStore.t(errorMessage) || errorMessage
      } else if (err.message) {
        message = err.message
      } else {
        message = 'An error occurred'
      }

      if (showErrorMessage && errorMessage !== false) {
        ElMessage.error(message)
      }

      if (onError) {
        onError(err, message)
      }

      throw err
    }
  }

  /**
   * Execute API call and extract data from response
   * @param {Function} apiCall - API function to call
   * @param {string} dataPath - Path to data in response (e.g., 'data.data.items')
   * @param {any} defaultValue - Default value if data not found
   * @returns {Promise<any>} Extracted data
   */
  async function executeAndExtract(apiCall, dataPath = 'data.data', defaultValue = null) {
    try {
      const response = await execute(apiCall)
      
      // Navigate through nested object path
      const paths = dataPath.split('.')
      let data = response
      
      for (const path of paths) {
        if (data && typeof data === 'object' && path in data) {
          data = data[path]
        } else {
          return defaultValue
        }
      }
      
      return data !== undefined ? data : defaultValue
    } catch (err) {
      return defaultValue
    }
  }

  /**
   * Clear error state
   */
  function clearError() {
    error.value = null
  }

  return {
    error,
    lastResponse,
    execute,
    executeAndExtract,
    clearError
  }
}
