'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { TokenManager } from '@/lib/auth/token-manager';
import { getCodeVerifier, clearCodeVerifier } from '@/lib/auth/pkce';

export default function CallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const hasProcessed = useRef(false);

  useEffect(() => {
    // Prevent double execution in React StrictMode
    if (hasProcessed.current) {
      console.log('Callback already processed, skipping');
      return;
    }

    const handleCallback = async () => {
      hasProcessed.current = true;
      const code = searchParams.get('code');
      const error = searchParams.get('error');

      if (error) {
        setError(`Authentication failed: ${error}`);
        setTimeout(() => router.push('/login-keycloak'), 3000);
        return;
      }

      if (!code) {
        setError('No authorization code received');
        setTimeout(() => router.push('/login-keycloak'), 3000);
        return;
      }

      try {
        // Get the code verifier from session storage
        const codeVerifier = getCodeVerifier();
        console.log('Code verifier retrieved:', codeVerifier ? 'Yes' : 'No');
        if (!codeVerifier) {
          throw new Error('Missing code verifier - please try logging in again');
        }

        // Exchange authorization code for tokens
        const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8180';
        const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'fraud-detection';
        const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'fraud-rule-engine-ui';
        const redirectUri = window.location.origin + '/callback';

        const tokenResponse = await fetch(
          `${keycloakUrl}/realms/${realm}/protocol/openid-connect/token`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
              grant_type: 'authorization_code',
              client_id: clientId,
              code: code,
              redirect_uri: redirectUri,
              code_verifier: codeVerifier,
            }),
          }
        );

        if (!tokenResponse.ok) {
          const errorData = await tokenResponse.json().catch(() => ({ error: 'unknown' }));
          console.error('Token exchange failed:', tokenResponse.status, errorData);
          throw new Error(`Failed to exchange authorization code for tokens: ${errorData.error_description || errorData.error || tokenResponse.statusText}`);
        }

        const tokens = await tokenResponse.json();
        console.log('Token exchange successful, tokens received');

        // Store tokens
        TokenManager.setTokens(tokens.access_token, tokens.refresh_token);

        // Clear code verifier
        clearCodeVerifier();

        // Redirect to dashboard
        router.push('/dashboard');
      } catch (err: any) {
        console.error('Token exchange error:', err);
        setError(err.message || 'Failed to complete authentication');
        setTimeout(() => router.push('/login-keycloak'), 3000);
      }
    };

    handleCallback();
  }, [searchParams, router]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-capitec-navy via-capitec-purple to-capitec-magenta">
        <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md text-center">
          <div className="text-red-500 mb-4">
            <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-gray-800 mb-2">Authentication Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <p className="text-sm text-gray-500">Redirecting to login...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-capitec-navy via-capitec-purple to-capitec-magenta">
      <div className="text-center">
        <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-white mx-auto mb-4"></div>
        <p className="text-white text-lg">Completing authentication...</p>
      </div>
    </div>
  );
}
