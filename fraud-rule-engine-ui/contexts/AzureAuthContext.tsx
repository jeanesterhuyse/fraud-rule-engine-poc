'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  PublicClientApplication,
  AccountInfo,
  AuthenticationResult,
  InteractionRequiredAuthError,
  EventType,
  EventMessage,
  AuthError
} from '@azure/msal-browser';
import { msalConfig, loginRequest, tokenRequest } from '@/lib/auth/msalConfig';

interface AzureAuthContextType {
  account: AccountInfo | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => Promise<void>;
  logout: () => void;
  getAccessToken: () => Promise<string | null>;
  getUserRoles: () => string[];
  hasRole: (role: string) => boolean;
}

const AzureAuthContext = createContext<AzureAuthContextType | undefined>(undefined);

// Initialize MSAL instance
let msalInstance: PublicClientApplication | null = null;

const getMsalInstance = async (): Promise<PublicClientApplication> => {
  if (!msalInstance) {
    msalInstance = new PublicClientApplication(msalConfig);
    await msalInstance.initialize();
  }
  return msalInstance;
};

export function AzureAuthProvider({ children }: { children: React.ReactNode }) {
  const [account, setAccount] = useState<AccountInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const initialize = async () => {
      try {
        console.log('[AzureAuth] Initializing MSAL...');
        const instance = await getMsalInstance();

        // Handle redirect promise
        const response = await instance.handleRedirectPromise();
        if (response) {
          console.log('[AzureAuth] Login redirect successful:', response.account?.username);
          setAccount(response.account);
        } else {
          // Check if there's an existing session
          const accounts = instance.getAllAccounts();
          console.log('[AzureAuth] Found', accounts.length, 'existing accounts');
          if (accounts.length > 0) {
            setAccount(accounts[0]);
            console.log('[AzureAuth] Using existing account:', accounts[0].username);
          }
        }

        // Register event callbacks
        instance.addEventCallback((event: EventMessage) => {
          if (event.eventType === EventType.LOGIN_SUCCESS && event.payload) {
            const payload = event.payload as AuthenticationResult;
            console.log('[AzureAuth] Login success event:', payload.account?.username);
            setAccount(payload.account);
          }

          if (event.eventType === EventType.LOGOUT_SUCCESS) {
            console.log('[AzureAuth] Logout success event');
            setAccount(null);
          }

          if (event.eventType === EventType.LOGIN_FAILURE) {
            console.error('[AzureAuth] Login failed:', event.error);
          }
        });

        setIsInitialized(true);
      } catch (error) {
        console.error('[AzureAuth] Initialization error:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initialize();
  }, []);

  const login = async () => {
    try {
      console.log('[AzureAuth] Starting login...');
      const instance = await getMsalInstance();

      // Use redirect for more reliable flow (popup can be blocked)
      await instance.loginRedirect(loginRequest);
    } catch (error) {
      console.error('[AzureAuth] Login error:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      console.log('[AzureAuth] Starting logout...');
      const instance = await getMsalInstance();

      await instance.logoutRedirect({
        account: account || undefined,
      });

      setAccount(null);
    } catch (error) {
      console.error('[AzureAuth] Logout error:', error);
    }
  };

  const getAccessToken = async (): Promise<string | null> => {
    if (!account) {
      console.warn('[AzureAuth] Cannot get token: no account');
      return null;
    }

    try {
      const instance = await getMsalInstance();

      // Try silent token acquisition first
      try {
        const response = await instance.acquireTokenSilent({
          ...tokenRequest,
          account,
        });
        console.log('[AzureAuth] Token acquired silently');
        return response.accessToken;
      } catch (error) {
        // If silent acquisition fails due to interaction required, use redirect
        if (error instanceof InteractionRequiredAuthError) {
          console.log('[AzureAuth] Interaction required, redirecting...');
          await instance.acquireTokenRedirect({
            ...tokenRequest,
            account,
          });
          return null; // Will redirect, so return null
        }
        throw error;
      }
    } catch (error) {
      console.error('[AzureAuth] Token acquisition error:', error);
      return null;
    }
  };

  const getUserRoles = (): string[] => {
    if (!account || !account.idTokenClaims) {
      return [];
    }

    const claims = account.idTokenClaims as any;

    // Azure AD can include groups in different claim names
    const groups = claims.groups || claims.roles || [];

    console.log('[AzureAuth] User groups:', groups);
    return groups;
  };

  const hasRole = (role: string): boolean => {
    const roles = getUserRoles();
    return roles.includes(role);
  };

  const contextValue: AzureAuthContextType = {
    account,
    isAuthenticated: !!account && isInitialized,
    isLoading,
    login,
    logout,
    getAccessToken,
    getUserRoles,
    hasRole,
  };

  return (
    <AzureAuthContext.Provider value={contextValue}>
      {children}
    </AzureAuthContext.Provider>
  );
}

export function useAzureAuth() {
  const context = useContext(AzureAuthContext);
  if (!context) {
    throw new Error('useAzureAuth must be used within AzureAuthProvider');
  }
  return context;
}
