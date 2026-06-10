# Test User Credentials - Keycloak Authentication

**Fraud Detection System - Test Environment**

---

## 🔐 Keycloak Admin Access

**Keycloak Admin Console:** http://localhost:8180

| Field | Value |
|-------|-------|
| **Username** | admin |
| **Password** | admin |
| **Realm** | fraud-detection |

**Purpose:** Manage users, roles, clients, and view sessions.

---

## 👥 Test Users

Three pre-configured users for testing different access levels:

---

### 1. John Smith - Fraud Analyst (Full Access)

| Field | Value |
|-------|-------|
| **Username** | `john.smith` |
| **Password** | `FraudDetect123!` |
| **Email** | john.smith@capitec.co.za |
| **First Name** | John |
| **Last Name** | Smith |
| **Roles** | `fraud_analyst`, `raas_consumer` |
| **Department** | Fraud Prevention |
| **Employee ID** | EMP12345 |
| **Region** | South Africa |

**Access Level:** Full access to all fraud detection features
- ✅ View dashboard
- ✅ Create/edit/delete rules
- ✅ View triggered transactions
- ✅ Export data
- ✅ Manage system settings

**Use Case:** Primary user for testing full system functionality

---

### 2. Sarah Jones - Fraud Viewer (Read-Only)

| Field | Value |
|-------|-------|
| **Username** | `sarah.jones` |
| **Password** | `ViewOnly123!` |
| **Email** | sarah.jones@capitec.co.za |
| **First Name** | Sarah |
| **Last Name** | Jones |
| **Roles** | `fraud_viewer`, `raas_consumer` |
| **Department** | Compliance |
| **Employee ID** | EMP67890 |
| **Region** | South Africa |

**Access Level:** Read-only access
- ✅ View dashboard
- ✅ View rules (no edit)
- ✅ View triggered transactions
- ❌ Cannot create/edit/delete rules
- ❌ Cannot modify system settings

**Use Case:** Testing read-only access and role-based restrictions

---

### 3. Admin User - System Administrator

| Field | Value |
|-------|-------|
| **Username** | `admin.user` |
| **Password** | `Admin123!` |
| **Email** | admin@capitec.co.za |
| **First Name** | Admin |
| **Last Name** | User |
| **Roles** | `fraud_analyst`, `raas_consumer` |
| **Department** | IT Security |
| **Employee ID** | ADMIN001 |
| **Region** | South Africa |

**Access Level:** Full system access with administrative privileges
- ✅ All fraud_analyst permissions
- ✅ System administration
- ✅ User management (via Keycloak)
- ✅ Configuration changes

**Use Case:** Testing administrative functions and system configuration

---

## 🔑 Getting an Access Token (for API testing)

### Using curl:

```bash
# Get token for John Smith
curl -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=john.smith' \
  -d 'password=FraudDetect123!' \
  | jq -r '.access_token'
```

### Save token to variable:

```bash
# John Smith (Fraud Analyst)
export TOKEN_JOHN=$(curl -s -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=john.smith' \
  -d 'password=FraudDetect123!' \
  | jq -r '.access_token')

# Sarah Jones (Viewer)
export TOKEN_SARAH=$(curl -s -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=sarah.jones' \
  -d 'password=ViewOnly123!' \
  | jq -r '.access_token')

# Admin User
export TOKEN_ADMIN=$(curl -s -X POST 'http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=fraud-rule-engine-ui' \
  -d 'grant_type=password' \
  -d 'username=admin.user' \
  -d 'password=Admin123!' \
  | jq -r '.access_token')
```

### Use token in API calls:

```bash
# Get dashboard summary (any user)
curl -H "Authorization: Bearer $TOKEN_JOHN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

# Get rules (any user)
curl -H "Authorization: Bearer $TOKEN_SARAH" \
  http://localhost:8080/api/v1/rules | jq

# Create rule (analyst only)
curl -X POST -H "Authorization: Bearer $TOKEN_JOHN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Rule","ruleType":"AMOUNT_THRESHOLD","thresholdAmount":10000}' \
  http://localhost:8080/api/v1/rules | jq
```

---

## 🧪 Testing Scenarios

### Scenario 1: Successful Login Flow

1. Open http://localhost:3001/login-keycloak
2. Enter: `john.smith` / `FraudDetect123!`
3. Click "Sign In with Keycloak"
4. Expected: Redirected to Keycloak login
5. Expected: After login, redirected to dashboard
6. Expected: Can see all dashboard data

### Scenario 2: Read-Only Access

1. Login as `sarah.jones` / `ViewOnly123!`
2. Navigate to Rules page
3. Expected: Can view rules
4. Expected: "Create Rule" button disabled or hidden
5. Expected: Cannot delete rules

### Scenario 3: Token Expiration & Refresh

1. Login as any user
2. Wait 60+ minutes (token expires)
3. Make an API call
4. Expected: Token automatically refreshed
5. Expected: Request succeeds

### Scenario 4: Logout

1. Login as any user
2. Click "Logout" in UI
3. Expected: Redirected to login page
4. Expected: Token cleared from browser
5. Expected: Cannot access protected pages

---

## 🔍 Token Inspection

### Decode JWT Token:

```bash
# Get token
TOKEN="<your-token-here>"

# Decode (requires jq)
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq

# Online: Paste into https://jwt.io
```

### Expected Claims:

```json
{
  "exp": 1717948800,
  "iat": 1717945200,
  "iss": "http://localhost:8180/realms/fraud-detection",
  "sub": "f35f1e85-8f52-4a8e-bd72-3e41cb51a85c",
  "preferred_username": "john.smith",
  "email": "john.smith@capitec.co.za",
  "email_verified": true,
  "name": "John Smith",
  "given_name": "John",
  "family_name": "Smith",
  "realm_access": {
    "roles": [
      "fraud_analyst",
      "raas_consumer",
      "default-roles-fraud-detection",
      "offline_access",
      "uma_authorization"
    ]
  },
  "resource_access": {
    "account": {
      "roles": ["manage-account", "manage-account-links", "view-profile"]
    }
  }
}
```

---

## 🛠️ Adding New Test Users

### Via Keycloak Admin Console:

1. Open http://localhost:8180
2. Login as admin/admin
3. Select `fraud-detection` realm
4. Users → Create User
5. Fill in details:
   - Username: `firstname.lastname`
   - Email: `firstname.lastname@capitec.co.za`
   - First Name: Firstname
   - Last Name: Lastname
   - Email Verified: ON
6. Save
7. Credentials tab → Set Password
   - Password: Choose strong password
   - Temporary: OFF
8. Role Mappings tab → Assign roles
   - Select: `fraud_analyst` or `fraud_viewer`
   - Select: `raas_consumer`

### Via Keycloak REST API:

```bash
# Get admin token
ADMIN_TOKEN=$(curl -s -X POST 'http://localhost:8180/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=admin' \
  -d 'password=admin' \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | jq -r '.access_token')

# Create user
curl -X POST "http://localhost:8180/admin/realms/fraud-detection/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new.user",
    "email": "new.user@capitec.co.za",
    "firstName": "New",
    "lastName": "User",
    "enabled": true,
    "emailVerified": true,
    "credentials": [{
      "type": "password",
      "value": "Password123!",
      "temporary": false
    }],
    "realmRoles": ["fraud_analyst", "raas_consumer"]
  }'
```

---

## 📋 Password Policy

Current password requirements:
- ✅ Minimum 8 characters
- ✅ Must contain uppercase letter
- ✅ Must contain lowercase letter
- ✅ Must contain number
- ✅ Must contain special character (!@#$%^&*)

Examples of valid passwords:
- `FraudDetect123!`
- `SecurePass456#`
- `Admin2026$Test`

---

## 🔒 Security Notes

**⚠️ IMPORTANT - POC ONLY:**

These credentials are for **LOCAL DEVELOPMENT AND TESTING ONLY**.

**DO NOT USE IN PRODUCTION:**
- ❌ Simple passwords
- ❌ No password expiration
- ❌ No MFA/2FA
- ❌ Admin account with default password
- ❌ Credentials documented in plain text

**For Production:**
- ✅ Integrate with corporate AD/LDAP
- ✅ Enable MFA/2FA
- ✅ Enforce strong password policy
- ✅ Implement password rotation
- ✅ Use secure secret management
- ✅ Enable audit logging
- ✅ Restrict admin access

---

**Last Updated:** June 9, 2026  
**Environment:** Local Development / POC  
**Keycloak Version:** 23.0
