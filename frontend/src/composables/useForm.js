import { reactive, ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useLocaleStore } from '@/locales/locale'

/**
 * Composable for form management
 * @param {Object} options - Configuration options
 * @param {Object} options.initialValues - Initial form values
 * @param {Object} options.rules - Validation rules
 * @param {Function} options.onSubmit - Submit handler
 * @param {Function} options.onReset - Reset handler
 * @returns {Object} Form state and methods
 */
export function useForm(options = {}) {
  const {
    initialValues = {},
    rules = {},
    onSubmit = null,
    onReset = null
  } = options

  const localeStore = useLocaleStore()
  const formRef = ref(null)
  const isSubmitting = ref(false)
  const isDirty = ref(false)
  const touchedFields = ref(new Set())

  // Create reactive form data
  const formData = reactive({ ...initialValues })

  // Track original values for dirty check
  const originalValues = ref(JSON.parse(JSON.stringify(initialValues)))

  /**
   * Check if form is dirty
   */
  const isFormDirty = computed(() => {
    return JSON.stringify(formData) !== JSON.stringify(originalValues.value)
  })

  /**
   * Set form field value
   * @param {string} field - Field name
   * @param {any} value - Field value
   */
  function setFieldValue(field, value) {
    formData[field] = value
    touchedFields.value.add(field)
    isDirty.value = isFormDirty.value
  }

  /**
   * Set multiple field values
   * @param {Object} values - Object with field-value pairs
   */
  function setFieldsValue(values) {
    Object.keys(values).forEach(key => {
      formData[key] = values[key]
      touchedFields.value.add(key)
    })
    isDirty.value = isFormDirty.value
  }

  /**
   * Get field value
   * @param {string} field - Field name
   * @returns {any} Field value
   */
  function getFieldValue(field) {
    return formData[field]
  }

  /**
   * Reset form to initial values
   */
  function resetFields() {
    Object.keys(formData).forEach(key => {
      formData[key] = initialValues[key]
    })
    touchedFields.value.clear()
    isDirty.value = false
    originalValues.value = JSON.parse(JSON.stringify(initialValues))
    formRef.value?.clearValidate()
    
    if (onReset) {
      onReset()
    }
  }

  /**
   * Reset form to specific values
   * @param {Object} values - Values to reset to
   */
  function resetFieldsTo(values) {
    Object.keys(formData).forEach(key => {
      if (key in values) {
        formData[key] = values[key]
      } else {
        formData[key] = initialValues[key]
      }
    })
    touchedFields.value.clear()
    originalValues.value = JSON.parse(JSON.stringify(values))
    isDirty.value = false
    formRef.value?.clearValidate()
  }

  /**
   * Validate form
   * @returns {Promise<boolean>} True if valid
   */
  async function validate() {
    if (!formRef.value) {
      return false
    }

    try {
      await formRef.value.validate()
      return true
    } catch (error) {
      return false
    }
  }

  /**
   * Validate specific field
   * @param {string} field - Field name
   * @returns {Promise<boolean>} True if valid
   */
  async function validateField(field) {
    if (!formRef.value) {
      return false
    }

    try {
      await formRef.value.validateField(field)
      return true
    } catch (error) {
      return false
    }
  }

  /**
   * Clear validation
   */
  function clearValidate() {
    formRef.value?.clearValidate()
  }

  /**
   * Submit form
   * @param {Object} extraData - Extra data to include in submission
   * @returns {Promise<any>} Result from onSubmit handler
   */
  async function submit(extraData = {}) {
    const isValid = await validate()
    if (!isValid) {
      const message = localeStore.t('form.validationError') || 'Please fix validation errors'
      ElMessage.warning(message)
      return null
    }

    isSubmitting.value = true
    try {
      const payload = { ...formData, ...extraData }
      const result = onSubmit ? await onSubmit(payload) : payload
      
      // Update original values after successful submit
      originalValues.value = JSON.parse(JSON.stringify(formData))
      isDirty.value = false
      
      return result
    } catch (error) {
      throw error
    } finally {
      isSubmitting.value = false
    }
  }

  /**
   * Check if field is touched
   * @param {string} field - Field name
   * @returns {boolean} True if touched
   */
  function isFieldTouched(field) {
    return touchedFields.value.has(field)
  }

  /**
   * Mark field as touched
   * @param {string} field - Field name
   */
  function touchField(field) {
    touchedFields.value.add(field)
  }

  /**
   * Watch form data changes
   */
  watch(
    () => formData,
    () => {
      isDirty.value = isFormDirty.value
    },
    { deep: true }
  )

  return {
    // Refs
    formRef,
    formData,
    isSubmitting,
    isDirty,
    touchedFields,
    
    // Computed
    isFormDirty,
    
    // Methods
    setFieldValue,
    setFieldsValue,
    getFieldValue,
    resetFields,
    resetFieldsTo,
    validate,
    validateField,
    clearValidate,
    submit,
    isFieldTouched,
    touchField
  }
}
