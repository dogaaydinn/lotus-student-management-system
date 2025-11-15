import { useAuthStore } from '@/stores/auth.store'
import { logger } from '@/utils/logger'

let requestInterceptorId = null
let responseInterceptorId = null

/**
 * Setup Axios interceptors for request/response handling
 * @param {import('axios').AxiosInstance} axiosInstance
 */
export function setupInterceptors(axiosInstance) {
  // Request interceptor
  requestInterceptorId = axiosInstance.interceptors.request.use(
    (config) => {
      // Add correlation ID for distributed tracing
      config.headers['X-Correlation-ID'] = crypto.randomUUID()

      // Add authentication token
      const authStore = useAuthStore()
      const token = authStore.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }

      // Log request
      logger.debug('API Request', {
        method: config.method,
        url: config.url,
        correlationId: config.headers['X-Correlation-ID'],
      })

      // Start request timer
      config.metadata = { startTime: new Date() }

      return config
    },
    (error) => {
      logger.error('Request Error', error)
      return Promise.reject(error)
    }
  )

  // Response interceptor
  responseInterceptorId = axiosInstance.interceptors.response.use(
    (response) => {
      // Calculate request duration
      const duration = new Date() - response.config.metadata.startTime

      logger.debug('API Response', {
        status: response.status,
        url: response.config.url,
        duration: `${duration}ms`,
        correlationId: response.headers['x-correlation-id'],
      })

      // Warn on slow requests
      if (duration > 2000) {
        logger.warn('Slow API request detected', {
          url: response.config.url,
          duration: `${duration}ms`,
        })
      }

      return response
    },
    async (error) => {
      const originalRequest = error.config

      // Handle 401 Unauthorized - try to refresh token
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        try {
          const authStore = useAuthStore()
          await authStore.refreshToken()

          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${authStore.token}`
          return axiosInstance(originalRequest)
        } catch (refreshError) {
          // Refresh failed - logout user
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        }
      }

      // Handle 429 Rate Limit - retry with exponential backoff
      if (error.response?.status === 429 && !originalRequest._retryCount) {
        originalRequest._retryCount = 0
      }

      if (error.response?.status === 429 && originalRequest._retryCount < 3) {
        originalRequest._retryCount++
        const delay = Math.pow(2, originalRequest._retryCount) * 1000

        logger.warn('Rate limited - retrying', {
          retryCount: originalRequest._retryCount,
          delayMs: delay,
        })

        await new Promise((resolve) => setTimeout(resolve, delay))
        return axiosInstance(originalRequest)
      }

      // Handle network errors - retry with exponential backoff
      if (
        error.message === 'Network Error' &&
        !originalRequest._networkRetryCount
      ) {
        originalRequest._networkRetryCount = 0
      }

      if (
        error.message === 'Network Error' &&
        originalRequest._networkRetryCount < 2
      ) {
        originalRequest._networkRetryCount++
        const delay = Math.pow(2, originalRequest._networkRetryCount) * 1000

        logger.warn('Network error - retrying', {
          retryCount: originalRequest._networkRetryCount,
          delayMs: delay,
        })

        await new Promise((resolve) => setTimeout(resolve, delay))
        return axiosInstance(originalRequest)
      }

      // Log error
      logger.error('API Error', {
        status: error.response?.status,
        message: error.response?.data?.message || error.message,
        url: error.config?.url,
        correlationId: error.response?.headers?.['x-correlation-id'],
      })

      return Promise.reject(error)
    }
  )
}

/**
 * Remove interceptors
 */
export function removeInterceptors(axiosInstance) {
  if (requestInterceptorId !== null) {
    axiosInstance.interceptors.request.eject(requestInterceptorId)
    requestInterceptorId = null
  }

  if (responseInterceptorId !== null) {
    axiosInstance.interceptors.response.eject(responseInterceptorId)
    responseInterceptorId = null
  }
}
