import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * Theme store for dark mode support
 * Persists theme preference in localStorage
 */
export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  // Initialize theme from localStorage or system preference
  const initTheme = () => {
    const stored = localStorage.getItem('theme')
    if (stored) {
      isDark.value = stored === 'dark'
    } else {
      // Use system preference
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  // Toggle between light and dark mode
  const toggleTheme = () => {
    isDark.value = !isDark.value
    applyTheme()
  }

  // Apply theme to document
  const applyTheme = () => {
    const theme = isDark.value ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }

  // Watch for system theme changes
  window
    .matchMedia('(prefers-color-scheme: dark)')
    .addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        isDark.value = e.matches
        applyTheme()
      }
    })

  return {
    isDark,
    toggleTheme,
    initTheme,
  }
})
