# Keycloak Authentication Setup

**Last Updated:** June 9, 2026

Complete guide for Keycloak integration with the Fraud Rule Engine POC.

---

## Overview

The fraud rule engine now uses **Keycloak** for authentication, replacing the previous hardcoded JWT implementation. Keycloak provides enterprise-grade identity and access management with support for:

- ✅ OAuth 2.0 / OpenID Connect
- ✅ Single Sign-On (SSO)
- ✅ Role-based access control
- ✅ AD/LDAP federation (future)
- ✅ Token refresh and session management
- ✅ Admin UI for user management

---

## Architecture

```
┌─────────────┐
│  Keycloak   │ Port 8180
│  (IdP)      │
└──────┬──────┘
       │ OAuth2/OIDC
       ├───────────────────┐
       ▼                   ▼
┌─────────────┐     ┌─────────────┐
│ Spring Boot │     │  Next.js UI │
│     API     │     │             │
│  Port 8080  │     │  Port 3001  │
└─────────────┘     └─────────────┘
```

---

## Quick Start

### 1. Start All Services

```bash
cd fraud-rule-engine-poc

# Start Keycloak + all services
docker-compose up -d

# Wait for services to be healthy (especially Keycloak - takes ~90s)
docker-compose ps
```

### 2. Configure Keycloak

```bash
# Run the setup script (creates realm, clients, users)
./setup-keycloak.sh
```

The script will:
- Create the `fraud-detection` realm
- Configure API and UI clients
- Create 3 test users
- Display all credentials

### 3. Install Frontend Dependencies

```bash
cd fraud-rule-engine-ui

# Install Keycloak dependencies
npm install next-auth@^4.24.5 keycloak-js@^23.0.0

# Copy environment file
cp .env.keycloak .env.local

# Start frontend
npm run dev
```

### 4. Test Authentication

1. Open http://localhost:3001
2. Click "Sign In with Keycloak"
3. Login with test credentials (see below)
4. You'll be redirected to the dashboard

---

## Test Users

Three test users are pre-configured:

### 1. John Smith (Fraud Analyst)
- **Username:** `john.smith`
- **Password:** `FraudDetect123!`
- **Email:** john.smith@capitec.co.za
- **Roles:** `fraud_analyst`, `raas_consumer`
- **Description:** Full access fraud analyst

### 2. Sarah Jones (Fraud Viewer)
- **Username:** `sarah.jones`
- **Password:** `ViewOnly123!`
- **Email:** sarah.jones@capitec.co.za
- **Roles:** `fraud_viewer`, `raas_consumer`
- **Description:** Read-only access

### 3. Admin User
- **Username:** `admin.user`
- **Password:** `Admin123!`
- **Email:** admin@capitec.co.za
- **Roles:** `fraud_analyst`, `raas_consumer`
- **Description:** System administrator

---

## Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Keycloak Admin** | http://localhost:8180 | admin / admin |
| **Frontend UI** | http://localhost:3001 | Use test users above |
| **Backend API** | http://localhost:8080 | Bearer token only |
| **Kafka UI** | http://localhost:8090 | No auth |

---

## Configuration

### Backend (Spring Boot)

**pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**application-docker.yml:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/fraud-detection
          jwk-set-uri: http://keycloak:8080/realms/fraud-detection/protocol/openid-connect/certs
```

**SecurityConfig.java:**
- Validates JWT tokens from Keycloak
- Extracts realm and resource roles
- Maps to Spring Security authorities
- Configures CORS for frontend

### Frontend (Next.js)

**Dependencies:**
- `keycloak-js`: Official Keycloak JavaScript adapter
- `next-auth` (optional): For server-side auth

**Key Files:**
- `lib/auth/keycloak.ts` - Keycloak initialization
- `contexts/KeycloakAuthContext.tsx` - React context
- `app/login-keycloak/page.tsx` - Login page
- `lib/api/keycloak-client.ts` - Axios interceptor for tokens

**Environment Variables (.env.local):**
```bash
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8180
NEXT_PUBLIC_KEYCLOAK_REALM=fraud-detection
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=fraud-rule-engine-ui
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

---

## Keycloak Realm Configuration

### Realm: `fraud-detection`

**Settings:**
- Access Token Lifespan: 1 hour
- SSO Session Idle: 10 hours
- SSO Session Max: 24 hours

### Clients

#### 1. fraud-rule-engine-api (Backend)
- **Type:** Bearer-only
- **Purpose:** Validates access tokens
- **Configuration:**
  - Public Client: No
  - Standard Flow: Disabled
  - Direct Access Grants: Disabled

#### 2. fraud-rule-engine-ui (Frontend)
- **Type:** Public client
- **Purpose:** Authenticates users
- **Configuration:**
  - Public Client: Yes
  - Standard Flow: Enabled (Authorization Code + PKCE)
  - Direct Access Grants: Enabled (for testing)
  - Valid Redirect URIs:
    - `http://localhost:3000/*`
    - `http://localhost:3001/*`
  - Web Origins:
    - `http://localhost:3000`
    - `http://localhost:3001`
  - PKCE: S256 (required)

### Roles

| Role | Description |
|------|-------------|
| `fraud_analyst` | Full access to fraud detection system |
| `fraud_viewer` | Read-only access |
| `raas_consumer` | Consumer of Rules as a Service |

---

## Authentication Flow

```
1. User → Frontend → Click "Login"
   ↓
2. Frontend redirects to Keycloak login page
   http://localhost:8180/realms/fraud-detection/protocol/openid-connect/auth
   ↓
3. User enters credentials (e.g., john.smith / FraudDetect123!)
   ↓
4. Keycloak validates credentials
   ↓
5. Keycloak redirects back to frontend with authorization code
   http://localhost:3001/dashboard?code=...
   ↓
6. Frontend exchanges code for tokens:
   - access_token (JWT)
   - id_token (JWT)
   - refresh_token
   ↓
7. Frontend stores tokens and makes API calls:
   Authorization: Bearer {access_token}
   ↓
8. Backend validates token with Keycloak:
   - Fetches public keys from JWKS endpoint
   - Validates signature, expiration, issuer
   - Extracts user info and roles
   ↓
9. Backend processes request and returns response
```

---

## Token Structure

**Access Token (JWT) Claims:**
```json
{
  "exp": 1717948800,
  "iat": 1717945200,
  "iss": "http://localhost:8180/realms/fraud-detection",
  "sub": "user-uuid",
  "preferred_username": "john.smith",
  "email": "john.smith@capitec.co.za",
  "name": "John Smith",
  "realm_access": {
    "roles": ["fraud_analyst", "raas_consumer"]
  },
  "resource_access": {
    "fraud-rule-engine-api": {
      "roles": []
    }
  }
}
```

---

## API Testing with Keycloak

### Get Access Token

```bash
# Using Direct Access Grants (password flow - testing only)
curl -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=john.smith' \
  -d 'password=FraudDetect123!'

# Response:
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5...",
  "expires_in": 3600,
  "refresh_expires_in": 36000,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5...",
  "token_type": "Bearer"
}
```

### Use Token for API Calls

```bash
# Save token
TOKEN="eyJhbGciOiJSUzI1NiIsInR5..."

# Call protected API
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/rules | jq
```

### Refresh Token

```bash
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5..."

curl -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=refresh_token' \
  -d "refresh_token=$REFRESH_TOKEN"
```

---

## Troubleshooting

### Keycloak Not Starting

**Check logs:**
```bash
docker logs fraud-keycloak
```

**Common issues:**
- PostgreSQL not ready → Wait for postgres to be healthy
- Port 8180 in use → Change port in docker-compose.yml
- Memory issues → Increase Docker memory to 6GB+

**Solution:**
```bash
docker-compose down
docker-compose up -d postgres
# Wait 30 seconds
docker-compose up -d keycloak
```

### Setup Script Fails

**Error: "Failed to get admin token"**
- Keycloak not ready yet → Wait 90 seconds after startup
- Check health: `curl http://localhost:8180/health/ready`

**Error: "Realm already exists"**
- Script deletes existing realm → Check admin console
- Manually delete: Keycloak Admin → Select Realm → Delete

### Frontend Authentication Issues

**Error: "Failed to initialize Keycloak"**
- Check browser console for errors
- Verify environment variables in `.env.local`
- Check Keycloak is accessible: `curl http://localhost:8180`

**Token not sent to API:**
- Check browser Network tab → Authorization header present?
- Verify `lib/api/keycloak-client.ts` is imported
- Check token is valid: paste into jwt.io

**Redirect loop:**
- Clear browser cache and cookies
- Check redirect URIs in Keycloak client config
- Verify PKCE is enabled

### Backend Authorization Issues

**Error: "401 Unauthorized"**
- Token expired → Refresh token
- Token invalid → Get new token
- Check backend logs for JWT validation errors

**Error: "403 Forbidden"**
- User lacks required role
- Check token claims: `echo $TOKEN | cut -d'.' -f2 | base64 -d | jq`
- Verify role mapping in SecurityConfig.java

---

## Manual Keycloak Configuration

If the setup script fails, configure manually:

### 1. Create Realm

1. Open http://localhost:8180
2. Login: admin / admin
3. Hover over "Master" → Click "Create Realm"
4. Name: `fraud-detection`
5. Click "Create"

### 2. Create Clients

**API Client:**
1. Clients → Create Client
2. Client ID: `fraud-rule-engine-api`
3. Client authentication: ON
4. Standard flow: OFF
5. Direct access grants: OFF
6. Save

**UI Client:**
1. Clients → Create Client
2. Client ID: `fraud-rule-engine-ui`
3. Client authentication: OFF (public)
4. Standard flow: ON
5. Direct access grants: ON
6. Valid redirect URIs: `http://localhost:3001/*`
7. Web origins: `http://localhost:3001`
8. Save
9. Advanced → Proof Key for Code Exchange Code Challenge Method: S256

### 3. Create Roles

1. Realm Roles → Create Role
2. Add: `fraud_analyst`, `fraud_viewer`, `raas_consumer`

### 4. Create Users

For each user:
1. Users → Create User
2. Username: `john.smith`
3. Email: `john.smith@capitec.co.za`
4. First name: John
5. Last name: Smith
6. Email verified: ON
7. Save
8. Credentials tab → Set password: `FraudDetect123!`
9. Temporary: OFF
10. Role mappings tab → Assign roles

---

## Advanced: AD/LDAP Federation

To connect to Active Directory:

### 1. Configure User Federation

1. Keycloak Admin → User Federation
2. Add Provider → LDAP
3. Configure:
   - Edit Mode: READ_ONLY
   - Vendor: Active Directory
   - Connection URL: `ldap://your-ad-server:389`
   - Users DN: `CN=Users,DC=capitec,DC=co,DC za`
   - Bind DN: `CN=service-account,CN=Users,DC=capitec,DC=co,DC=za`
   - Bind Credential: `password`
4. Test Connection
5. Save

### 2. Map AD Groups to Keycloak Roles

1. LDAP Provider → Mappers
2. Create Mapper:
   - Type: role-ldap-mapper
   - LDAP Groups DN: `CN=Groups,DC=capitec,DC=co,DC=za`
   - Group Object Classes: group
   - Group Name LDAP Attribute: cn
3. Sync Users

---

## Production Considerations

Before deploying to production:

### Security

- [ ] Change Keycloak admin password
- [ ] Enable HTTPS (TLS/SSL)
- [ ] Use PostgreSQL with SSL
- [ ] Store secrets in secret manager (AWS Secrets Manager, Vault)
- [ ] Disable Direct Access Grants (password flow)
- [ ] Enable MFA/2FA
- [ ] Configure session timeout policies
- [ ] Set up audit logging

### High Availability

- [ ] Run multiple Keycloak instances (cluster)
- [ ] Use external PostgreSQL (RDS)
- [ ] Configure load balancer
- [ ] Set up database replication
- [ ] Configure sticky sessions

### Integration

- [ ] Connect to corporate AD/LDAP
- [ ] Configure SSO with other systems
- [ ] Set up service accounts for APIs
- [ ] Implement group/role hierarchy
- [ ] Configure attribute mappers

### Monitoring

- [ ] Enable Keycloak metrics
- [ ] Set up health check alerts
- [ ] Monitor token issuance rates
- [ ] Track failed login attempts
- [ ] Set up log aggregation

---

## Migration from Old JWT

The previous custom JWT implementation has been removed:

**Removed:**
- Custom JWT filter (JwtAuthenticationFilter.java)
- JWT utility class (JwtUtil.java)
- Hardcoded test/test credentials
- TokenController (/api/v1/auth/login)

**Replaced with:**
- Spring Security OAuth2 Resource Server
- Keycloak JWT validation
- Role-based access from Keycloak

**No Database Changes:**
- Existing schema unchanged
- No migration scripts needed
- API endpoints remain the same (only auth mechanism changed)

---

## Support

**Keycloak Documentation:**
- https://www.keycloak.org/documentation

**Spring Security OAuth2:**
- https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html

**Common Issues:**
- Check `docker-compose logs keycloak`
- Check `docker-compose logs fraud-api`
- Check browser console for frontend errors
- Verify all services healthy: `docker-compose ps`

---

**Created by:** Claude  
**Date:** June 9, 2026  
**Version:** 1.0
