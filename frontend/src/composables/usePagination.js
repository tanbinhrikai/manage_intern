import { ref, computed } from 'vue'

/**
 * Composable for managing pagination state
 * @param {Object} options - Configuration options
 * @param {number} options.initialPage - Initial page number (default: 1)
 * @param {number} options.initialPageSize - Initial page size (default: 10)
 * @param {Function} options.onPageChange - Callback when page changes
 * @returns {Object} Pagination state and methods
 */
export function usePagination(options = {}) {
  const {
    initialPage = 1,
    initialPageSize = 10,
    onPageChange = null
  } = options

  const currentPage = ref(initialPage)
  const pageSize = ref(initialPageSize)
  const totalItems = ref(0)

  const totalPages = computed(() => {
    return Math.ceil(totalItems.value / pageSize.value) || 1
  })

  const hasNextPage = computed(() => {
    return currentPage.value < totalPages.value
  })

  const hasPrevPage = computed(() => {
    return currentPage.value > 1
  })

  const offset = computed(() => {
    return (currentPage.value - 1) * pageSize.value
  })

  function setPage(page) {
    if (page >= 1 && page <= totalPages.value) {
      currentPage.value = page
      if (onPageChange) {
        onPageChange(page)
      }
    }
  }

  function nextPage() {
    if (hasNextPage.value) {
      setPage(currentPage.value + 1)
    }
  }

  function prevPage() {
    if (hasPrevPage.value) {
      setPage(currentPage.value - 1)
    }
  }

  function firstPage() {
    setPage(1)
  }

  function lastPage() {
    setPage(totalPages.value)
  }

  function setTotalItems(total) {
    totalItems.value = total
    // Auto-adjust page if current page exceeds total pages
    if (currentPage.value > totalPages.value && totalPages.value > 0) {
      currentPage.value = totalPages.value
    }
  }

  function reset() {
    currentPage.value = initialPage
    pageSize.value = initialPageSize
    totalItems.value = 0
  }

  function setPageSize(newPageSize) {
    pageSize.value = newPageSize
    // Reset to first page when page size changes
    currentPage.value = 1
    if (onPageChange) {
      onPageChange(1)
    }
  }

  // Get pagination params for API calls (0-indexed page for backend)
  const apiParams = computed(() => ({
    page: currentPage.value - 1, // Backend expects 0-indexed
    limit: pageSize.value
  }))

  return {
    // State
    currentPage,
    pageSize,
    totalItems,
    
    // Computed
    totalPages,
    hasNextPage,
    hasPrevPage,
    offset,
    apiParams,
    
    // Methods
    setPage,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
    setTotalItems,
    setPageSize,
    reset
  }
}
