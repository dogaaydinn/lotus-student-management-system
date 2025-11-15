// API Configuration
export const API_BASE_URL = __DEV__
  ? 'http://localhost:8080'  // Development
  : 'https://api.lotus-sms.com';  // Production

// App Configuration
export const APP_NAME = 'Lotus SMS';
export const APP_VERSION = '1.0.0';

// Storage Keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_DATA: 'user',
  THEME: 'theme',
  LANGUAGE: 'language',
};

// API Endpoints
export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/api/v1/auth/login',
  REGISTER: '/api/v1/auth/register',
  FORGOT_PASSWORD: '/api/v1/auth/forgot-password',
  REFRESH_TOKEN: '/api/v1/auth/refresh',

  // Student
  STUDENT_PROFILE: '/api/v1/students/profile',
  UPDATE_PROFILE: '/api/v1/students/update',

  // Internships
  INTERNSHIPS: '/api/v1/career-center/opportunities',
  APPLY_INTERNSHIP: '/api/v1/career-center/applications',
  MY_APPLICATIONS: '/api/v1/career-center/applications/student',

  // Documents
  DOCUMENTS: '/api/v1/documents',
  UPLOAD_DOCUMENT: '/api/v1/documents/upload',
  DOWNLOAD_DOCUMENT: '/api/v1/documents/download',

  // Messages
  MESSAGES: '/api/v1/messages',
  SEND_MESSAGE: '/api/v1/messages/send',

  // Notifications
  NOTIFICATIONS: '/api/v1/notifications',
  MARK_READ: '/api/v1/notifications/read',
};

// Application Status
export const APPLICATION_STATUS = {
  PENDING: 'PENDING',
  UNDER_REVIEW: 'UNDER_REVIEW',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  WITHDRAWN: 'WITHDRAWN',
};

// Document Types
export const DOCUMENT_TYPES = {
  RESUME: 'RESUME',
  TRANSCRIPT: 'TRANSCRIPT',
  COVER_LETTER: 'COVER_LETTER',
  CERTIFICATE: 'CERTIFICATE',
  ID_CARD: 'ID_CARD',
  OTHER: 'OTHER',
};

// User Roles
export const USER_ROLES = {
  STUDENT: 'STUDENT',
  COORDINATOR: 'COORDINATOR',
  COMPANY: 'COMPANY',
  ADMIN: 'ADMIN',
};

// Pagination
export const DEFAULT_PAGE_SIZE = 20;
export const MAX_PAGE_SIZE = 100;

// File Upload
export const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
export const ALLOWED_FILE_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'image/jpeg',
  'image/png',
];
