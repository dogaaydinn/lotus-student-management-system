/**
 * Logging utility
 * Provides structured logging with environment-based configuration
 */

const isProduction = import.meta.env.VITE_APP_ENVIRONMENT === 'production'
const loggingEnabled = import.meta.env.VITE_ENABLE_LOGGING === 'true'

class Logger {
  debug(message, data = {}) {
    if (!isProduction && loggingEnabled) {
      console.debug(`[DEBUG] ${message}`, data)
    }
  }

  info(message, data = {}) {
    if (loggingEnabled) {
      console.info(`[INFO] ${message}`, data)
    }
  }

  warn(message, data = {}) {
    if (loggingEnabled) {
      console.warn(`[WARN] ${message}`, data)
    }
  }

  error(message, error = null) {
    if (loggingEnabled) {
      console.error(`[ERROR] ${message}`, error)
    }

    // In production, send to error tracking service (e.g., Sentry)
    if (isProduction && error) {
      // window.Sentry?.captureException(error)
    }
  }
}

export const logger = new Logger()
