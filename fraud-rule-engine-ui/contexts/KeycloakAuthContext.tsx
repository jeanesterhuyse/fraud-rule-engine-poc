'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { getKeycloak, initKeycloak, login as keycloakLogin, logout as keycloakLogout, getUserInfo, getToken } from '@/lib/auth/keycloak';
import Keycloak from 'keycloak-js';

interface KeycloakAuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: {
    username?: string;
    email?: string;
    name?: string;
    roles: string[];
  } | null;
  token: string | undefined;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
}

const KeycloakAuthContext = createContext<KeycloakAuthContextType | undefined>(undefined);

export const KeycloakAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<KeycloakAuthContextType['user']>(null);
  const [token, setToken] = useState<string | undefined>(undefined);

  useEffect(() => {
    const initialize = async () => {
      try {
        const authenticated = await initKeycloak();
        setIsAuthenticated(authenticated);

        if (authenticated) {
          const userInfo = getUserInfo();
          setUser(userInfo);
          setToken(getToken());

          // Listen for token updates
          const keycloak = getKeycloak();
          keycloak.onTokenExpired = () => {
            keycloak.updateToken(30).then(() => {
              setToken(getToken());
            }).catch(() => {
              handleLogout();
            });
          };
        }
      } catch (error) {
        // Keycloak initialization failed
      } finally {
        setIsLoading(false);
      }
    };

    initialize();
  }, []);

  const handleLogin = async () => {
    setIsLoading(true);
    try {
      await keycloakLogin();
    } catch (error) {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      await keycloakLogout();
      setIsAuthenticated(false);
      setUser(null);
      setToken(undefined);
    } catch (error) {
      // Logout failed
    } finally {
      setIsLoading(false);
    }
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    return user.roles.includes(role);
  };

  return (
    <KeycloakAuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        user,
        token,
        login: handleLogin,
        logout: handleLogout,
        hasRole,
      }}
    >
      {children}
    </KeycloakAuthContext.Provider>
  );
};

export const useKeycloakAuth = () => {
  const context = useContext(KeycloakAuthContext);
  if (context === undefined) {
    throw new Error('useKeycloakAuth must be used within a KeycloakAuthProvider');
  }
  return context;
};
