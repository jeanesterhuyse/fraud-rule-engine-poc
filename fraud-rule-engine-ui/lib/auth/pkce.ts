// PKCE (Proof Key for Code Exchange) utilities

function base64URLEncode(str: ArrayBuffer): string {
  return btoa(String.fromCharCode(...new Uint8Array(str)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=/g, '');
}

function sha256(plain: string): Promise<ArrayBuffer> {
  const encoder = new TextEncoder();
  const data = encoder.encode(plain);
  return crypto.subtle.digest('SHA-256', data);
}

export async function generatePKCE(): Promise<{ codeVerifier: string; codeChallenge: string }> {
  // Generate random code verifier
  const randomBytes = new Uint8Array(32);
  crypto.getRandomValues(randomBytes);
  const codeVerifier = base64URLEncode(randomBytes.buffer);

  // Generate code challenge (SHA256 hash of verifier)
  const hashed = await sha256(codeVerifier);
  const codeChallenge = base64URLEncode(hashed);

  return { codeVerifier, codeChallenge };
}

export function storeCodeVerifier(verifier: string): void {
  sessionStorage.setItem('pkce_code_verifier', verifier);
}

export function getCodeVerifier(): string | null {
  return sessionStorage.getItem('pkce_code_verifier');
}

export function clearCodeVerifier(): void {
  sessionStorage.removeItem('pkce_code_verifier');
}
