/**
 * Toast Notification Store
 * Global state management for toast notifications
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  // State
  const toasts = ref([])
  let nextId = 0

  /**
   * Add a toast notification
   * @param {Object} options
   * @param {string} options.message - Toast message
   * @param {string} [options.type='info'] - Toast type: 'success', 'error', 'warning', 'info'
   * @param {number} [options.duration=5000] - Auto-dismiss duration in ms (0 = no auto-dismiss)
   * @param {string} [options.title] - Optional title
   */
  function addToast({ message, type = 'info', duration = 5000, title }) {
    const id = nextId++
    const toast = {
      id,
      message,
      type,
      title,
      timestamp: new Date(),
    }

    toasts.value.push(toast)

    // Auto-dismiss after duration
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }

    return id
  }

  /**
   * Remove toast by ID
   * @param {number} id
   */
  function removeToast(id) {
    const index = toasts.value.findIndex((t) => t.id === id)
    if (index !== -1) {
      toasts.value.splice(index, 1)
    }
  }

  /**
   * Clear all toasts
   */
  function clearAll() {
    toasts.value = []
  }

  /**
   * Success toast
   * @param {string} message
   * @param {Object} [options]
   */
  function success(message, options = {}) {
    return addToast({
      message,
      type: 'success',
      duration: 3000,
      ...options,
    })
  }

  /**
   * Error toast
   * @param {string} message
   * @param {Object} [options]
   */
  function error(message, options = {}) {
    return addToast({
      message,
      type: 'error',
      duration: 5000,
      ...options,
    })
  }

  /**
   * Warning toast
   * @param {string} message
   * @param {Object} [options]
   */
  function warning(message, options = {}) {
    return addToast({
      message,
      type: 'warning',
      duration: 4000,
      ...options,
    })
  }

  /**
   * Info toast
   * @param {string} message
   * @param {Object} [options]
   */
  function info(message, options = {}) {
    return addToast({
      message,
      type: 'info',
      duration: 3000,
      ...options,
    })
  }

  return {
    // State
    toasts,

    // Actions
    addToast,
    removeToast,
    clearAll,
    success,
    error,
    warning,
    info,
  }
})
