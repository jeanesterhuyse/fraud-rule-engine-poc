import Keycloak from 'keycloak-js';

// Keycloak configuration
const keycloakConfig = {
  url: process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8180',
  realm: process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'fraud-detection',
  clientId: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'fraud-rule-engine-ui',
};

// Create Keycloak instance
let keycloakInstance: Keycloak | null = null;

export const getKeycloak = (): Keycloak => {
  if (typeof window === 'undefined') {
    throw new Error('Keycloak can only be initialized on the client side');
  }

  if (!keycloakInstance) {
    keycloakInstance = new Keycloak(keycloakConfig);
  }

  return keycloakInstance;
};

// Initialize Keycloak
export const initKeycloak = async (): Promise<boolean> => {
  const keycloak = getKeycloak();

  try {
    const authenticated = await keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });

    if (authenticated) {
      setupTokenRefresh(keycloak);
    }

    return authenticated;
  } catch (error) {
    return false;
  }
};

// Setup automatic token refresh
const setupTokenRefresh = (keycloak: Keycloak) => {
  setInterval(() => {
    keycloak.updateToken(70).catch(() => {
      keycloak.logout();
    });
  }, 60000);
};

// Login function
export const login = async (): Promise<void> => {
  const keycloak = getKeycloak();
  await keycloak.login({
    redirectUri: window.location.origin + '/dashboard',
  });
};

// Logout function
export const logout = async (): Promise<void> => {
  const keycloak = getKeycloak();
  await keycloak.logout({
    redirectUri: window.location.origin,
  });
};

// Get access token
export const getToken = (): string | undefined => {
  const keycloak = getKeycloak();
  return keycloak.token;
};

// Get user info
export const getUserInfo = () => {
  const keycloak = getKeycloak();
  return {
    username: keycloak.tokenParsed?.preferred_username,
    email: keycloak.tokenParsed?.email,
    name: keycloak.tokenParsed?.name,
    roles: keycloak.tokenParsed?.realm_access?.roles || [],
    authenticated: keycloak.authenticated,
  };
};

// Check if user has role
export const hasRole = (role: string): boolean => {
  const keycloak = getKeycloak();
  return keycloak.hasRealmRole(role);
};
