// Token management for Keycloak OAuth2
const TOKEN_KEY = 'keycloak_token';
const REFRESH_TOKEN_KEY = 'keycloak_refresh_token';

export const TokenManager = {
  setTokens: (accessToken: string, refreshToken?: string) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(TOKEN_KEY, accessToken);
      if (refreshToken) {
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
      }
    }
  },

  getAccessToken: (): string | null => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(TOKEN_KEY);
    }
    return null;
  },

  getRefreshToken: (): string | null => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem(REFRESH_TOKEN_KEY);
    }
    return null;
  },

  clearTokens: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    }
  },

  isAuthenticated: (): boolean => {
    return !!TokenManager.getAccessToken();
  },

  logout: () => {
    const token = TokenManager.getAccessToken();
    TokenManager.clearTokens();

    // End Keycloak SSO session
    const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8180';
    const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'fraud-detection';
    const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'fraud-rule-engine-ui';
    const redirectUri = encodeURIComponent(window.location.origin + '/login-keycloak');

    // Use the logout endpoint with post_logout_redirect_uri
    if (token) {
      window.location.href = `${keycloakUrl}/realms/${realm}/protocol/openid-connect/logout?post_logout_redirect_uri=${redirectUri}&client_id=${clientId}`;
    } else {
      // No token, just redirect to login
      window.location.href = '/login-keycloak';
    }
  }
};
