/**
 * Centralized Axios Client
 * Enterprise-grade HTTP client with interceptors, retry logic, and error handling
 */

import axios from 'axios'
import { apiConfig, validateConfig } from '@/config/api.config.js'
import { setupAuthInterceptor } from '@/interceptors/auth.interceptor.js'
import { setupErrorInterceptor } from '@/interceptors/error.interceptor.js'
import { setupRetryInterceptor } from '@/interceptors/retry.interceptor.js'

// Validate configuration on startup
validateConfig()

/**
 * Create axios instance with centralized configuration
 */
const apiClient = axios.create({
  baseURL: apiConfig.baseURL,
  timeout: apiConfig.timeout,
  withCredentials: apiConfig.withCredentials,
  headers: apiConfig.headers,
})

/**
 * Request Logging Interceptor (Development Only)
 */
if (apiConfig.enableLogging) {
  apiClient.interceptors.request.use(
    (config) => {
      const timestamp = new Date().toISOString()
      console.log(`[API Request] ${timestamp}`, {
        method: config.method?.toUpperCase(),
        url: config.url,
        params: config.params,
        data: config.data,
      })
      return config
    },
    (error) => {
      console.error('[API Request Error]', error)
      return Promise.reject(error)
    }
  )
}

/**
 * Response Logging Interceptor (Development Only)
 */
if (apiConfig.enableLogging) {
  apiClient.interceptors.response.use(
    (response) => {
      const timestamp = new Date().toISOString()
      console.log(`[API Response] ${timestamp}`, {
        status: response.status,
        url: response.config.url,
        data: response.data,
      })
      return response
    },
    (error) => {
      const timestamp = new Date().toISOString()
      console.error(`[API Response Error] ${timestamp}`, {
        status: error.response?.status,
        url: error.config?.url,
        message: error.message,
        data: error.response?.data,
      })
      return Promise.reject(error)
    }
  )
}

/**
 * Setup interceptors in correct order:
 * 1. Auth (adds tokens to requests)
 * 2. Retry (handles network failures)
 * 3. Error (transforms errors for UI)
 */
setupAuthInterceptor(apiClient)
setupRetryInterceptor(apiClient)
setupErrorInterceptor(apiClient)

/**
 * Request UUID for distributed tracing
 */
apiClient.interceptors.request.use((config) => {
  // Generate UUID for request correlation
  config.headers['X-Request-ID'] = crypto.randomUUID()
  return config
})

/**
 * Performance Monitoring
 */
if (apiConfig.debug) {
  apiClient.interceptors.request.use((config) => {
    config.metadata = { startTime: new Date() }
    return config
  })

  apiClient.interceptors.response.use(
    (response) => {
      const duration = new Date() - response.config.metadata.startTime
      if (duration > 1000) {
        console.warn(`[Performance] Slow request detected: ${response.config.url} took ${duration}ms`)
      }
      return response
    },
    (error) => {
      if (error.config?.metadata) {
        const duration = new Date() - error.config.metadata.startTime
        console.warn(`[Performance] Failed request took ${duration}ms`)
      }
      return Promise.reject(error)
    }
  )
}

export default apiClient
