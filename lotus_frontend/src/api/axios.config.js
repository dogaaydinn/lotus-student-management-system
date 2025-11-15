import axios from 'axios'
import { API_CONFIG } from '@/config/api.config'
import { setupInterceptors } from './interceptors'

/**
 * Axios instance with configured interceptors
 * Handles authentication, retry logic, and correlation IDs
 */
const apiClient = axios.create(API_CONFIG)

// Setup request/response interceptors
setupInterceptors(apiClient)

export default apiClient
