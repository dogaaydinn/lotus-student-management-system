<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-boundary-container">
      <div class="error-icon">⚠</div>
      <h2>Something went wrong</h2>
      <p class="error-message">{{ errorMessage }}</p>

      <details v-if="showDetails" class="error-details">
        <summary>Technical Details</summary>
        <pre>{{ errorStack }}</pre>
      </details>

      <div class="error-actions">
        <button @click="handleReset" class="btn-primary">
          Try Again
        </button>
        <button @click="handleGoHome" class="btn-secondary">
          Go to Home
        </button>
      </div>
    </div>
  </div>
  <slot v-else></slot>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  fallback: {
    type: Function,
    default: null
  },
  showDetails: {
    type: Boolean,
    default: import.meta.env.DEV // Show details only in development
  },
  onError: {
    type: Function,
    default: null
  }
})

const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('')
const errorStack = ref('')

/**
 * Capture errors from child components
 */
onErrorCaptured((err, instance, info) => {
  hasError.value = true
  errorMessage.value = err.message || 'An unexpected error occurred'
  errorStack.value = err.stack || ''

  // Log error for debugging
  console.error('[Error Boundary] Caught error:', {
    error: err,
    component: instance?.$options?.name || 'Unknown component',
    info
  })

  // Call custom error handler if provided
  if (props.onError) {
    props.onError(err, instance, info)
  }

  // Prevent error from propagating further
  return false
})

/**
 * Reset error state and try again
 */
function handleReset() {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
}

/**
 * Navigate to home page
 */
function handleGoHome() {
  handleReset()
  router.push('/')
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 20px;
}

.error-boundary-container {
  max-width: 600px;
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 64px;
  margin-bottom: 20px;
  color: #ef4444;
}

h2 {
  margin-bottom: 12px;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.error-message {
  color: #6b7280;
  margin-bottom: 24px;
  font-size: 16px;
}

.error-details {
  text-align: left;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.error-details summary {
  cursor: pointer;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.error-details pre {
  margin-top: 12px;
  font-size: 12px;
  color: #6b7280;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}
</style>
