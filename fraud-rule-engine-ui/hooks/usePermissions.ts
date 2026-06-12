import { useMemo } from 'react';
import { useKeycloakAuth } from '@/contexts/KeycloakAuthContext';

/**
 * Custom hook for checking user permissions based on Keycloak roles.
 * Provides memoized permission checks to avoid unnecessary re-renders.
 */
export function usePermissions() {
  const { hasRole } = useKeycloakAuth();

  const permissions = useMemo(() => ({
    /**
     * Can create, edit, and delete rules and blocklists.
     * Requires fraud_analyst or admin role.
     */
    canEdit: hasRole('fraud_analyst') || hasRole('admin'),

    /**
     * Can view all data (read-only).
     * All authenticated users have this permission.
     */
    canView: true,

    /**
     * Is a fraud analyst (full access).
     */
    isFraudAnalyst: hasRole('fraud_analyst'),

    /**
     * Is a fraud viewer (read-only).
     */
    isFraudViewer: hasRole('fraud_viewer'),

    /**
     * Is an admin (full access).
     */
    isAdmin: hasRole('admin'),
  }), [hasRole]);

  return permissions;
}
