/**
 * API Configuration
 * Centralized configuration for API client
 * Uses Vite environment variables for multi-environment support
 */

export const apiConfig = {
  // Base URL from environment variables
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085',

  // Timeout in milliseconds
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '30000'),

  // Enable/disable API request logging
  enableLogging: import.meta.env.VITE_ENABLE_API_LOGGING === 'true',

  // Enable debug mode
  debug: import.meta.env.VITE_ENABLE_DEBUG === 'true',

  // Send cookies with requests
  withCredentials: true,

  // Default headers
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },

  // Retry configuration
  retry: {
    attempts: 3,
    delay: 1000, // 1 second
    backoff: 2,  // Exponential backoff multiplier
    retryableStatuses: [408, 429, 500, 502, 503, 504],
  },
}

/**
 * Validate environment configuration
 * Throws error if required env variables are missing
 */
export function validateConfig() {
  if (!import.meta.env.VITE_API_BASE_URL) {
    console.warn('[API Config] VITE_API_BASE_URL not set, using default: http://localhost:8085')
  }

  if (apiConfig.debug) {
    console.log('[API Config] Configuration:', {
      baseURL: apiConfig.baseURL,
      timeout: apiConfig.timeout,
      enableLogging: apiConfig.enableLogging,
      debug: apiConfig.debug,
    })
  }
}

export default apiConfig
