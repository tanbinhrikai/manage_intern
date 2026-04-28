import { ref, computed, reactive } from 'vue'

/**
 * Composable for managing dialog state
 * Returns a reactive object that auto-unwraps refs when accessed in templates
 * @param {Object} options - Configuration options
 * @param {boolean} options.initialVisible - Initial visible state (default: false)
 * @returns {Object} Dialog state and methods
 */
export function useDialog(options = {}) {
  const { initialVisible = false } = options

  const visible = ref(initialVisible)
  const selectedItem = ref(null)

  /**
   * Check if dialog is in edit mode
   */
  const isEdit = computed(() => !!selectedItem.value)

  /**
   * Open dialog
   * @param {any} item - Item to edit (optional)
   */
  function open(item = null) {
    selectedItem.value = item
    visible.value = true
  }

  /**
   * Close dialog and reset selected item
   */
  function close() {
    visible.value = false
    selectedItem.value = null
  }

  /**
   * Toggle dialog visibility
   */
  function toggle() {
    visible.value = !visible.value
    if (!visible.value) {
      selectedItem.value = null
    }
  }

  /**
   * Reset dialog state
   */
  function reset() {
    visible.value = initialVisible
    selectedItem.value = null
  }

  // Return reactive object to enable auto-unwrapping of refs in templates
  // This fixes "Expected Boolean, got Object" errors when using v-model
  return reactive({
    visible,
    selectedItem,
    isEdit,
    open,
    close,
    toggle,
    reset
  })
}
