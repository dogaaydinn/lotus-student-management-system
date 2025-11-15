import axios from 'axios';
import { API_BASE_URL } from '../utils/constants';

interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    studentId?: string;
  };
}

interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  studentId: string;
  faculty: string;
  department: string;
}

class AuthService {
  async login(email: string, password: string): Promise<LoginResponse> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/v1/auth/login`, {
        email,
        password,
      });

      return response.data;
    } catch (error: any) {
      throw new Error(
        error.response?.data?.message || 'Login failed. Please check your credentials.'
      );
    }
  }

  async register(data: RegisterData): Promise<LoginResponse> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/v1/auth/register`, data);

      return response.data;
    } catch (error: any) {
      throw new Error(
        error.response?.data?.message || 'Registration failed. Please try again.'
      );
    }
  }

  async forgotPassword(email: string): Promise<void> {
    try {
      await axios.post(`${API_BASE_URL}/api/v1/auth/forgot-password`, {
        email,
      });
    } catch (error: any) {
      throw new Error(
        error.response?.data?.message || 'Failed to send password reset email.'
      );
    }
  }

  async refreshToken(token: string): Promise<{ token: string }> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/v1/auth/refresh`, {
        token,
      });

      return response.data;
    } catch (error: any) {
      throw new Error('Failed to refresh token');
    }
  }
}

export const authService = new AuthService();
