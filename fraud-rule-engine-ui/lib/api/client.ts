import axios from 'axios';
import { TokenManager } from '@/lib/auth/token-manager';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request interceptor to add token
apiClient.interceptors.request.use(
  (config) => {
    const token = TokenManager.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error('API returned 401 Unauthorized');
      console.error('Request URL:', error.config?.url);
      console.error('Token present:', !!TokenManager.getAccessToken());

      // Token expired or invalid - redirect to login
      TokenManager.clearTokens();
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        console.log('Redirecting to login due to 401');
        window.location.href = '/login-keycloak';
      }
    }
    return Promise.reject(error);
  }
);
