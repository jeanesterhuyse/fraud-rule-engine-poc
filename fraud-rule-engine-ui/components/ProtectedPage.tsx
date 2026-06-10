'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { TokenManager } from '@/lib/auth/token-manager';

export function ProtectedPage({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // Check if user is authenticated
    const checkAuth = () => {
      const token = TokenManager.getAccessToken();
      console.log('ProtectedPage: Checking auth, token exists:', !!token);

      if (!token) {
        console.log('ProtectedPage: No token found, redirecting to login');
        router.push('/login-keycloak');
      } else {
        console.log('ProtectedPage: Token found, showing page');
        setIsChecking(false);
      }
    };

    // Small delay to ensure token is saved
    setTimeout(checkAuth, 100);
  }, [router]);

  if (isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
