'use client';

import { useState } from 'react';
import Image from 'next/image';
import { generatePKCE, storeCodeVerifier } from '@/lib/auth/pkce';

export default function LoginPage() {
  const [isRedirecting, setIsRedirecting] = useState(false);

  const handleLogin = async () => {
    setIsRedirecting(true);

    // Generate PKCE parameters
    const { codeVerifier, codeChallenge } = await generatePKCE();
    storeCodeVerifier(codeVerifier);

    // Build the Keycloak authorization URL with PKCE
    const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8180';
    const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'fraud-detection';
    const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'fraud-rule-engine-ui';
    const redirectUri = window.location.origin + '/callback';

    // Add prompt=login to force fresh login (skip SSO if you want to test)
    // Remove &prompt=login if you want SSO behavior
    const authUrl = `${keycloakUrl}/realms/${realm}/protocol/openid-connect/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=openid&code_challenge=${codeChallenge}&code_challenge_method=S256`;

    // Redirect to Keycloak login
    window.location.href = authUrl;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          {/* Capitec Logo */}
          <div className="mb-6 flex justify-center">
            <Image
              src="/capitec-logo.svg"
              alt="Capitec Bank"
              width={200}
              height={60}
              priority
            />
          </div>

          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Fraud Detection System
          </h1>
          <p className="text-gray-600">
            Sign in to access the fraud rule engine
          </p>
        </div>

        <button
          onClick={handleLogin}
          disabled={isRedirecting}
          className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-4 px-6 rounded-lg font-semibold text-lg shadow-md hover:shadow-lg transform hover:-translate-y-0.5 transition-all duration-200 flex items-center justify-center gap-3 mb-6"
        >
          {isRedirecting ? (
            <>
              <div className="animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
              Redirecting...
            </>
          ) : (
            <>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
              </svg>
              Sign In with Keycloak
            </>
          )}
        </button>

        <div className="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-100">
          <p className="text-sm text-gray-700 mb-2 font-semibold">Test Credentials:</p>
          <div className="text-xs text-gray-600 space-y-1">
            <p><strong>Fraud Analyst:</strong> john.smith / FraudDetect123!</p>
            <p><strong>Fraud Viewer:</strong> sarah.jones / ViewOnly123!</p>
            <p><strong>Admin:</strong> admin.user / Admin123!</p>
          </div>
        </div>

        <div className="mt-6 text-center text-xs text-gray-500">
          <p>Protected by Keycloak Authentication</p>
          <p className="mt-1">© 2026 Capitec Bank - Fraud Prevention</p>
        </div>
      </div>
    </div>
  );
}
