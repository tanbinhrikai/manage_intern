/**
 * Composable for date formatting utilities
 * @returns {Object} Date formatting functions
 */
export function useDateFormat() {
  /**
   * Format date string to localized format
   * @param {string|Date|null|undefined} date - ISO date string or Date object
   * @param {string} [locale='en-GB'] - Locale string (default: 'en-GB')
   * @param {string} [fallback='-'] - Fallback string when date is empty (default: '-')
   * @returns {string} Formatted date or fallback string
   */
  function formatDate(date, locale = 'en-GB', fallback = '-') {
    if (!date) return fallback
    try {
      const d = date instanceof Date ? date : new Date(date)
      if (isNaN(d.getTime())) return fallback
      return d.toLocaleDateString(locale)
    } catch (error) {
      console.error('Date formatting error:', error)
      return fallback
    }
  }

  /**
   * Format date and time string to localized format
   * @param {string|Date|null|undefined} dateTime - ISO date-time string or Date object
   * @param {string} [locale='en-GB'] - Locale string (default: 'en-GB')
   * @param {string} [fallback='-'] - Fallback string when date is empty (default: '-')
   * @returns {string} Formatted date-time or fallback string
   */
  function formatDateTime(dateTime, locale = 'en-GB', fallback = '-') {
    if (!dateTime) return fallback
    try {
      const d = dateTime instanceof Date ? dateTime : new Date(dateTime)
      if (isNaN(d.getTime())) return fallback
      return d.toLocaleString(locale)
    } catch (error) {
      console.error('DateTime formatting error:', error)
      return fallback
    }
  }

  return {
    formatDate,
    formatDateTime
  }
}
