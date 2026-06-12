# Keycloak Authentication - Quick Start Guide

**5-Minute Setup** for adding Keycloak authentication to the Fraud Rule Engine.

---

## 🚀 Quick Start (5 Steps)

### Step 1: Start Keycloak

```bash
cd fraud-rule-engine-poc

# Start all services (including Keycloak)
docker-compose up -d

# Wait for Keycloak to be healthy (~90 seconds)
docker-compose ps
# fraud-keycloak should show "healthy"
```

### Step 2: Setup Keycloak Realm & Users

```bash
# Run the automated setup script
./setup-keycloak.sh

# This creates:
# - fraud-detection realm
# - API and UI clients
# - 3 test users
```

### Step 3: Rebuild Backend with OAuth2 Dependencies

```bash
cd fraud-rule-engine-api

# Clean build with new dependencies
mvn clean package -DskipTests

# Restart the API container
docker-compose restart fraud-api

# Check logs
docker-compose logs -f fraud-api
# Should see: "Using 'jwk-set-uri' property to configure ResourceServer security"
```

### Step 4: Install Frontend Keycloak Dependencies

```bash
cd ../fraud-rule-engine-ui

# Install Keycloak packages
npm install next-auth@^4.24.5 keycloak-js@^23.0.0

# Copy Keycloak environment config
cp .env.keycloak .env.local

# Start frontend
npm run dev
```

### Step 5: Test Login

1. Open http://localhost:3001/login-keycloak
2. Click "Sign In with Keycloak"
3. Login with:
   - Username: `john.smith`
   - Password: `FraudDetect123!`
4. You'll be redirected to the dashboard

---

## 🔐 Test User Credentials

| User | Username | Password | Role |
|------|----------|----------|------|
| Fraud Analyst | `john.smith` | `FraudDetect123!` | fraud_analyst |
| Fraud Viewer | `sarah.jones` | `ViewOnly123!` | fraud_viewer |
| Admin | `admin.user` | `Admin123!` | fraud_analyst |

---

## 🔗 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Keycloak Admin** | http://localhost:8180 | admin / admin |
| **Login Page** | http://localhost:3001/login-keycloak | Use test users |
| **Dashboard** | http://localhost:3001/dashboard | (after login) |
| **API** | http://localhost:8080/api/v1 | (requires Bearer token) |

---

## ✅ Verification Checklist

Check that everything is working:

- [ ] Keycloak is running: `curl http://localhost:8180/health/ready`
- [ ] Backend is running: `curl http://localhost:8080/actuator/health`
- [ ] Frontend is running: `curl http://localhost:3001`
- [ ] Can access Keycloak admin: http://localhost:8180 (admin/admin)
- [ ] Can see login page: http://localhost:3001/login-keycloak
- [ ] Can login with john.smith / FraudDetect123!
- [ ] After login, redirected to dashboard
- [ ] API calls include Bearer token in headers

---

## 🧪 Test API with Token

```bash
# Get access token
TOKEN=$(curl -s -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=john.smith' \
  -d 'password=FraudDetect123!' | jq -r '.access_token')

# Test API call
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

# Expected: JSON response with dashboard data
```

---

## 🐛 Troubleshooting

### Keycloak Not Starting

```bash
# Check logs
docker logs fraud-keycloak

# Wait longer (takes 60-90 seconds first time)
docker-compose ps

# Restart if needed
docker-compose restart keycloak
```

### Setup Script Fails

```bash
# Keycloak might not be ready yet
# Wait and retry:
sleep 30
./setup-keycloak.sh
```

### Frontend Can't Connect to Keycloak

```bash
# Check environment file exists
cat fraud-rule-engine-ui/.env.local

# Should contain:
# NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8180
# NEXT_PUBLIC_KEYCLOAK_REALM=fraud-detection
# NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=fraud-rule-engine-ui
```

### Backend 401 Errors

```bash
# Check backend logs
docker-compose logs fraud-api | grep -i "keycloak\|oauth\|jwt"

# Verify Keycloak connection
docker-compose exec fraud-api curl http://keycloak:8080/health/ready
```

---

## 📖 Full Documentation

For complete details, see [KEYCLOAK_SETUP.md](./KEYCLOAK_SETUP.md)

Topics covered:
- Architecture overview
- Detailed configuration
- Token structure
- Advanced troubleshooting
- Production considerations
- AD/LDAP integration

---

## 🎯 What Changed

**Added:**
- ✅ Keycloak service in docker-compose.yml
- ✅ OAuth2 Resource Server in Spring Boot
- ✅ SecurityConfig.java for JWT validation
- ✅ Keycloak adapter in Next.js frontend
- ✅ Login page with Keycloak redirect
- ✅ 3 pre-configured test users

**Removed:**
- ❌ Custom JWT implementation
- ❌ Hardcoded test/test credentials
- ❌ TokenController (/auth/login endpoint)

**Unchanged:**
- ✅ Database schema (no migrations needed)
- ✅ API endpoints (same URLs)
- ✅ Frontend pages (just auth mechanism changed)
- ✅ Kafka configuration
- ✅ Rule engine logic

---

**Setup Time:** ~5 minutes  
**Created:** June 9, 2026
