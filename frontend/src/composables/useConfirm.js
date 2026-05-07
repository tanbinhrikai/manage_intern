import { ElMessageBox } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'

/**
 * Composable for confirmation dialogs
 * Provides a reusable way to show confirmation dialogs with i18n support
 * @returns {Object} Confirmation dialog methods
 */
export function useConfirm() {
  const localeStore = useLocaleStore()

  /**
   * Show confirmation dialog
   * @param {Object} options - Confirmation options
   * @param {string} options.message - Confirmation message
   * @param {string} [options.title] - Dialog title (default: 'Confirm')
   * @param {string} [options.type] - Dialog type: 'warning', 'info', 'success', 'error' (default: 'warning')
   * @param {string} [options.confirmButtonText] - Confirm button text (default: 'OK')
   * @param {string} [options.cancelButtonText] - Cancel button text (default: 'Cancel')
   * @param {Function} [options.onConfirm] - Callback when confirmed
   * @param {Function} [options.onCancel] - Callback when cancelled
   * @returns {Promise} Promise that resolves when confirmed, rejects when cancelled
   */
  function confirm(options = {}) {
    const {
      message,
      title,
      type = 'warning',
      confirmButtonText,
      cancelButtonText,
      onConfirm,
      onCancel
    } = options

    const finalTitle = title || localeStore.t('common.confirm.title') || 'Confirm'
    const finalConfirmButtonText = confirmButtonText || localeStore.t('common.confirm.ok') || 'OK'
    const finalCancelButtonText = cancelButtonText || localeStore.t('common.confirm.cancel') || 'Cancel'

    return ElMessageBox.confirm(message, finalTitle, {
      confirmButtonText: finalConfirmButtonText,
      cancelButtonText: finalCancelButtonText,
      type,
      lockScroll: false,
      draggable: false,
      customClass: 'confirm-dialog-custom'
    })
      .then(() => {
        if (onConfirm) {
          onConfirm()
        }
      })
      .catch((error) => {
        if (error !== 'cancel' && onCancel) {
          onCancel(error)
        }
      })
  }

  /**
   * Show confirmation dialog for update operations
   * @param {Object} options - Options
   * @param {Function} options.onConfirm - Callback when confirmed
   * @param {Function} [options.onCancel] - Callback when cancelled
   * @param {string} [options.message] - Custom message
   */
  function confirmUpdate(options = {}) {
    const {
      onConfirm,
      onCancel,
      message
    } = options

    return confirm({
      message: message || localeStore.t('common.confirm.update') || 'Are you sure you want to update?',
      type: 'info',
      confirmButtonText: localeStore.t('common.confirm.yes') || 'Yes',
      cancelButtonText: localeStore.t('common.confirm.no') || 'No',
      onConfirm,
      onCancel
    })
  }

  /**
   * Show confirmation dialog for delete operations
   * @param {Object} options - Options
   * @param {Function} options.onConfirm - Callback when confirmed
   * @param {Function} [options.onCancel] - Callback when cancelled
   * @param {string} [options.message] - Custom message
   */
  function confirmDelete(options = {}) {
    const {
      onConfirm,
      onCancel,
      message,
      title
    } = options

    return confirm({
      message: message || localeStore.t('common.confirm.delete') || 'Are you sure you want to delete?',
      title: title || localeStore.t('common.confirm.title') || 'Confirm',
      type: 'warning',
      confirmButtonText: localeStore.t('common.confirm.deleteButton') || 'Delete',
      cancelButtonText: localeStore.t('common.confirm.cancel') || 'Cancel',
      onConfirm,
      onCancel
    })
  }

  return {
    confirm,
    confirmUpdate,
    confirmDelete
  }
}
