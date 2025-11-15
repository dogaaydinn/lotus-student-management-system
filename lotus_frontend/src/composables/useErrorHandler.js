/**
 * Composable for centralized error handling
 * Provides consistent error handling across components
 */

import { ref } from 'vue'
import { useToastStore } from '@/stores/toast.store.js'

export function useErrorHandler() {
  const toastStore = useToastStore()
  const error = ref(null)
  const isLoading = ref(false)

  /**
   * Handle async operation with error handling and loading state
   * @param {Function} asyncFn - Async function to execute
   * @param {Object} options - Options
   * @param {string} [options.errorMessage] - Custom error message
   * @param {string} [options.successMessage] - Success message to show
   * @param {Function} [options.onError] - Custom error handler
   * @param {Function} [options.onSuccess] - Custom success handler
   * @param {boolean} [options.showToast=true] - Whether to show toast notification
   * @returns {Promise<any>} Result from async function
   */
  async function handleAsync(asyncFn, options = {}) {
    const {
      errorMessage,
      successMessage,
      onError,
      onSuccess,
      showToast = true
    } = options

    error.value = null
    isLoading.value = true

    try {
      const result = await asyncFn()

      // Show success message if provided
      if (successMessage && showToast) {
        toastStore.success(successMessage)
      }

      // Call success handler if provided
      if (onSuccess) {
        onSuccess(result)
      }

      return result
    } catch (err) {
      error.value = err

      // Show error toast
      if (showToast) {
        const message = errorMessage || err.userMessage || 'An error occurred'
        toastStore.error(message)
      }

      // Call error handler if provided
      if (onError) {
        onError(err)
      }

      // Log error in development
      if (import.meta.env.DEV) {
        console.error('[useErrorHandler] Error:', err)
      }

      throw err
    } finally {
      isLoading.value = false
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
    isLoading,
    handleAsync,
    clearError
  }
}
