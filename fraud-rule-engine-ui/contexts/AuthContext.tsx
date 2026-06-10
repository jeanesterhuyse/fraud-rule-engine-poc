'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/lib/api/auth';
import { LoginRequest, AuthResponse } from '@/types/api';

interface AuthContextType {
  user: { username: string; roles: string[] } | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<{ username: string; roles: string[] } | null>(() => {
    // Initialize state from storage immediately to avoid flash
    if (typeof window !== 'undefined') {
      const storedUser = authService.getUser();
      const token = authService.getToken();
      console.log('AuthProvider INIT: token exists:', !!token, 'user exists:', !!storedUser);
      if (token && storedUser) {
        return storedUser;
      }
    }
    return null;
  });
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    console.log('AuthProvider mounted with user:', user?.username || 'none');

    // Re-check storage on every mount to catch any changes
    const token = authService.getToken();
    const storedUser = authService.getUser();

    if (token && storedUser && !user) {
      console.log('AuthProvider: Found auth in storage but not in state, restoring');
      setUser(storedUser);
    } else if (!token && user) {
      console.log('AuthProvider: No token in storage but user in state, clearing');
      setUser(null);
    }
  }, [user]);

  const login = async (credentials: LoginRequest) => {
    try {
      console.log('AuthContext: Attempting login...');
      const authData: AuthResponse = await authService.login(credentials);
      console.log('AuthContext: Login successful, saving auth data...');

      // Save to storage first
      authService.saveAuth(authData);

      // Then update state
      const userData = { username: authData.username, roles: authData.roles };
      setUser(userData);

      console.log('AuthContext: User state updated, redirecting to dashboard...');

      // Use window.location for more reliable navigation
      if (typeof window !== 'undefined') {
        window.location.href = '/dashboard';
      }
    } catch (error) {
      console.error('AuthContext: Login failed:', error);
      throw error;
    }
  };

  const logout = () => {
    console.log('AuthContext: Logging out...');
    authService.logout();
    setUser(null);
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
