/**
 * Authentication Interceptor
 * Automatically adds auth tokens to requests and handles token refresh
 */

/**
 * Setup authentication interceptor on axios instance
 * @param {import('axios').AxiosInstance} axiosInstance
 */
export function setupAuthInterceptor(axiosInstance) {
  axiosInstance.interceptors.request.use(
    (config) => {
      // Get token from sessionStorage (current implementation)
      const token = sessionStorage.getItem('token')

      if (token) {
        // Add Bearer token to Authorization header
        config.headers.Authorization = `Bearer ${token}`
      }

      // Add user ID if available (for server-side logging)
      const userId = sessionStorage.getItem('userId')
      if (userId) {
        config.headers['X-User-ID'] = userId
      }

      // Add user type for role-based routing
      const userType = sessionStorage.getItem('userType')
      if (userType) {
        config.headers['X-User-Type'] = userType
      }

      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config

      // Handle 401 Unauthorized - Token expired or invalid
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        // Check if we have a refresh token
        const refreshToken = sessionStorage.getItem('refreshToken')

        if (refreshToken) {
          try {
            // Attempt to refresh the token
            const response = await axiosInstance.post('/api/auth/refresh', {
              refreshToken,
            })

            const { token: newToken } = response.data

            // Update stored token
            sessionStorage.setItem('token', newToken)

            // Retry original request with new token
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return axiosInstance(originalRequest)
          } catch (refreshError) {
            // Refresh failed - clear auth data and redirect to login
            handleAuthFailure()
            return Promise.reject(refreshError)
          }
        } else {
          // No refresh token - redirect to login
          handleAuthFailure()
        }
      }

      return Promise.reject(error)
    }
  )
}

/**
 * Handle authentication failure
 * Clears session and redirects to login
 */
function handleAuthFailure() {
  // Clear all auth data
  sessionStorage.clear()
  localStorage.clear()

  // Redirect to login page
  // Using window.location to ensure full page reload and Vue router reset
  const currentPath = window.location.pathname
  if (currentPath !== '/login' && currentPath !== '/') {
    window.location.href = '/login'
  }
}

/**
 * Manually clear authentication (for logout)
 */
export function clearAuth() {
  handleAuthFailure()
}
