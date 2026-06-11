'use client';

import { KeycloakAuthProvider } from '@/contexts/KeycloakAuthContext';

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <KeycloakAuthProvider>
      {children}
    </KeycloakAuthProvider>
  );
}
