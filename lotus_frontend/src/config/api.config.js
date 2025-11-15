/**
 * API Configuration
 * Centralized configuration for API endpoints and settings
 */

export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085/api',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 30000,
  headers: {
    'Content-Type': 'application/json',
  },
}

export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh-token',
    REGISTER: '/auth/register',
    MFA_SETUP: '/auth/mfa/setup',
    MFA_VERIFY: '/auth/mfa/verify',
  },
  STUDENTS: {
    LIST: '/students',
    DETAIL: (id) => `/students/${id}`,
    CREATE: '/students',
    UPDATE: (id) => `/students/${id}`,
    DELETE: (id) => `/students/${id}`,
    SEARCH: '/students/search',
  },
  OPPORTUNITIES: {
    LIST: '/opportunities',
    DETAIL: (id) => `/opportunities/${id}`,
    CREATE: '/opportunities',
    UPDATE: (id) => `/opportunities/${id}`,
    DELETE: (id) => `/opportunities/${id}`,
    SEARCH: '/opportunities/search',
    ACTIVE: '/opportunities/active',
  },
  APPLICATIONS: {
    LIST: '/applications',
    DETAIL: (id) => `/applications/${id}`,
    CREATE: '/applications',
    UPDATE: (id) => `/applications/${id}`,
    DELETE: (id) => `/applications/${id}`,
    BY_STUDENT: (studentId) => `/applications/student/${studentId}`,
    BY_OPPORTUNITY: (opportunityId) => `/applications/opportunity/${opportunityId}`,
  },
  DOCUMENTS: {
    LIST: '/documents',
    UPLOAD: '/documents/upload',
    DOWNLOAD: (id) => `/documents/${id}/download`,
    DELETE: (id) => `/documents/${id}`,
  },
  MESSAGES: {
    LIST: '/messages',
    DETAIL: (id) => `/messages/${id}`,
    SEND: '/messages/send',
    MARK_READ: (id) => `/messages/${id}/read`,
    UNREAD_COUNT: '/messages/unread/count',
  },
  ANALYTICS: {
    DASHBOARD: '/analytics/dashboard',
    ENROLLMENT: '/analytics/enrollment',
    PLACEMENTS: '/analytics/placements',
    TRENDS: '/analytics/trends',
  },
  ADMIN: {
    USERS: '/admin/users',
    STATS: '/admin/stats',
    SETTINGS: '/admin/settings',
  },
}

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
}
