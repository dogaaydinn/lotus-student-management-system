/**
 * Error Handling Interceptor
 * Transforms API errors into user-friendly messages and handles global error scenarios
 */

import { useToastStore } from '@/stores/toast.store.js'

/**
 * Setup error interceptor on axios instance
 * @param {import('axios').AxiosInstance} axiosInstance
 */
export function setupErrorInterceptor(axiosInstance) {
  axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
      // Get toast store for notifications
      let toastStore
      try {
        toastStore = useToastStore()
      } catch (e) {
        // Store not available (e.g., during SSR or before app initialization)
        console.error('[Error Interceptor] Toast store not available:', e)
      }

      const enhancedError = transformError(error)

      // Show user-friendly notification
      if (toastStore && enhancedError.showToast) {
        toastStore.error(enhancedError.userMessage, {
          duration: enhancedError.toastDuration,
        })
      }

      // Log detailed error for debugging
      if (import.meta.env.DEV) {
        console.error('[API Error]', {
          message: enhancedError.userMessage,
          technical: enhancedError.technicalMessage,
          status: enhancedError.status,
          code: enhancedError.code,
          originalError: error,
        })
      }

      return Promise.reject(enhancedError)
    }
  )
}

/**
 * Transform axios error into enhanced error object
 * @param {import('axios').AxiosError} error
 * @returns {EnhancedError}
 */
function transformError(error) {
  const enhancedError = {
    originalError: error,
    status: error.response?.status,
    code: error.code,
    userMessage: 'An unexpected error occurred',
    technicalMessage: error.message,
    showToast: true,
    toastDuration: 5000,
    data: error.response?.data,
  }

  // Network errors (no response from server)
  if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
    enhancedError.userMessage = 'Network error. Please check your internet connection.'
    enhancedError.toastDuration = 7000
    return enhancedError
  }

  // Request timeout
  if (error.code === 'ETIMEDOUT') {
    enhancedError.userMessage = 'Request timed out. Please try again.'
    enhancedError.toastDuration = 5000
    return enhancedError
  }

  // Server responded with error status
  if (error.response) {
    const { status, data } = error.response

    switch (status) {
      case 400:
        enhancedError.userMessage = data?.message || 'Invalid request. Please check your input.'
        break

      case 401:
        enhancedError.userMessage = 'Session expired. Please log in again.'
        enhancedError.showToast = false // Auth interceptor handles redirect
        break

      case 403:
        enhancedError.userMessage = 'Access denied. You do not have permission to perform this action.'
        break

      case 404:
        enhancedError.userMessage = data?.message || 'The requested resource was not found.'
        break

      case 409:
        enhancedError.userMessage = data?.message || 'Conflict. The resource already exists or has been modified.'
        break

      case 422:
        enhancedError.userMessage = data?.message || 'Validation failed. Please check your input.'
        // Include validation errors if available
        if (data?.errors) {
          enhancedError.validationErrors = data.errors
        }
        break

      case 429:
        enhancedError.userMessage = 'Too many requests. Please wait a moment and try again.'
        enhancedError.toastDuration = 7000
        // Extract retry-after header if available
        const retryAfter = error.response.headers['retry-after']
        if (retryAfter) {
          enhancedError.retryAfter = parseInt(retryAfter)
          enhancedError.userMessage = `Too many requests. Please wait ${retryAfter} seconds and try again.`
        }
        break

      case 500:
        enhancedError.userMessage = 'Server error. Our team has been notified. Please try again later.'
        enhancedError.toastDuration = 7000
        break

      case 502:
      case 503:
        enhancedError.userMessage = 'Service temporarily unavailable. Please try again in a few moments.'
        enhancedError.toastDuration = 7000
        break

      case 504:
        enhancedError.userMessage = 'Gateway timeout. The server took too long to respond. Please try again.'
        enhancedError.toastDuration = 7000
        break

      default:
        enhancedError.userMessage = data?.message || `An error occurred (${status}). Please try again.`
    }

    enhancedError.technicalMessage = data?.error || data?.message || error.message
  }

  return enhancedError
}

/**
 * @typedef {Object} EnhancedError
 * @property {Error} originalError - Original axios error
 * @property {number} [status] - HTTP status code
 * @property {string} [code] - Error code
 * @property {string} userMessage - User-friendly error message
 * @property {string} technicalMessage - Technical error message for debugging
 * @property {boolean} showToast - Whether to show toast notification
 * @property {number} toastDuration - Toast duration in ms
 * @property {any} [data] - Response data
 * @property {Object} [validationErrors] - Validation errors (422)
 * @property {number} [retryAfter] - Retry after seconds (429)
 */
