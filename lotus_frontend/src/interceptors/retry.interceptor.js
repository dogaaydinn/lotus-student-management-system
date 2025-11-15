/**
 * Retry Interceptor
 * Implements exponential backoff retry logic for failed requests
 */

import { apiConfig } from '@/config/api.config.js'

/**
 * Setup retry interceptor on axios instance
 * @param {import('axios').AxiosInstance} axiosInstance
 */
export function setupRetryInterceptor(axiosInstance) {
  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const config = error.config

      // Don't retry if:
      // 1. Retry is disabled
      // 2. Request already has max retries
      // 3. Request is marked as non-retryable
      if (!config || config._noRetry || !shouldRetry(error)) {
        return Promise.reject(error)
      }

      // Initialize retry count
      config._retryCount = config._retryCount || 0

      // Check if we've exceeded max retry attempts
      if (config._retryCount >= apiConfig.retry.attempts) {
        return Promise.reject(error)
      }

      // Increment retry count
      config._retryCount += 1

      // Calculate delay with exponential backoff
      const delay = calculateBackoffDelay(
        config._retryCount,
        apiConfig.retry.delay,
        apiConfig.retry.backoff
      )

      if (apiConfig.enableLogging) {
        console.log(
          `[Retry] Attempt ${config._retryCount}/${apiConfig.retry.attempts} for ${config.url} after ${delay}ms delay`
        )
      }

      // Wait for calculated delay
      await sleep(delay)

      // Retry the request
      return axiosInstance(config)
    }
  )
}

/**
 * Determine if request should be retried
 * @param {import('axios').AxiosError} error
 * @returns {boolean}
 */
function shouldRetry(error) {
  // Network errors (no response) - always retry
  if (!error.response) {
    return true
  }

  const status = error.response.status
  const retryableStatuses = apiConfig.retry.retryableStatuses

  // Retry if status is in retryable list
  if (retryableStatuses.includes(status)) {
    return true
  }

  // Don't retry client errors (4xx) except specific ones
  if (status >= 400 && status < 500) {
    return false
  }

  // Retry server errors (5xx)
  return status >= 500
}

/**
 * Calculate exponential backoff delay
 * @param {number} retryCount - Current retry attempt
 * @param {number} baseDelay - Base delay in ms
 * @param {number} backoffMultiplier - Multiplier for exponential backoff
 * @returns {number} Delay in ms
 */
function calculateBackoffDelay(retryCount, baseDelay, backoffMultiplier) {
  // Exponential backoff: delay * (backoff ^ (retryCount - 1))
  // Example with baseDelay=1000, backoff=2:
  // Retry 1: 1000ms
  // Retry 2: 2000ms
  // Retry 3: 4000ms
  const delay = baseDelay * Math.pow(backoffMultiplier, retryCount - 1)

  // Add jitter (±20%) to prevent thundering herd
  const jitter = delay * 0.2 * (Math.random() - 0.5)

  return Math.round(delay + jitter)
}

/**
 * Sleep for specified milliseconds
 * @param {number} ms - Milliseconds to sleep
 * @returns {Promise<void>}
 */
function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * Disable retry for specific request
 * @param {import('axios').AxiosRequestConfig} config
 * @returns {import('axios').AxiosRequestConfig}
 */
export function disableRetry(config) {
  return {
    ...config,
    _noRetry: true,
  }
}
