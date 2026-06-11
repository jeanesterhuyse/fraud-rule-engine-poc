# Role-Based Access Control Implementation

**Date:** June 11, 2026  
**Status:** ✅ Implemented

---

## Overview

Implemented role-based access control (RBAC) to restrict create/edit/delete operations to users with `fraud_analyst` or `admin` roles. Users with `fraud_viewer` role have read-only access.

---

## Backend Changes

### Security Annotations Added

Added `@PreAuthorize` annotations to protect write operations in:

#### RuleController.java
- `@PreAuthorize("hasRole('FRAUD_ANALYST') or hasRole('ADMIN')")` on:
  - `POST /api/v1/rules` - Create rule
  - `PUT /api/v1/rules/{id}` - Update rule
  - `DELETE /api/v1/rules/{id}` - Delete rule
  - `PATCH /api/v1/rules/{id}/enable` - Enable rule
  - `PATCH /api/v1/rules/{id}/disable` - Disable rule

- **Read operations remain open** to authenticated users:
  - `GET /api/v1/rules` - List rules
  - `GET /api/v1/rules/{id}` - Get rule by ID

#### BlocklistController.java
- `@PreAuthorize("hasRole('FRAUD_ANALYST') or hasRole('ADMIN')")` on:
  - `POST /api/v1/blocklists/customers` - Block customer
  - `DELETE /api/v1/blocklists/customers/{customerId}` - Unblock customer
  - `POST /api/v1/blocklists/merchants` - Block merchant
  - `DELETE /api/v1/blocklists/merchants/{merchantName}` - Unblock merchant

- **Read operations remain open** to authenticated users:
  - `GET /api/v1/blocklists/customers` - List blocked customers
  - `GET /api/v1/blocklists/merchants` - List blocked merchants

---

## Frontend Changes

### Rules Page (`app/dashboard/rules/page.tsx`)

1. **Import auth context:**
   ```tsx
   import { useKeycloakAuth } from '@/contexts/KeycloakAuthContext';
   ```

2. **Check user role:**
   ```tsx
   const { hasRole } = useKeycloakAuth();
   const canEdit = hasRole('fraud_analyst') || hasRole('admin');
   ```

3. **Conditional UI rendering:**
   - Hide "Create Rule" button for viewers
   - Hide "Edit", "Delete", "Enable/Disable" buttons for viewers
   - Show "(Read-only access)" indicator for viewers

### Blocklists Page (`app/dashboard/blocklists/page.tsx`)

1. **Import auth context:**
   ```tsx
   import { useKeycloakAuth } from '@/contexts/KeycloakAuthContext';
   ```

2. **Check user role:**
   ```tsx
   const { hasRole } = useKeycloakAuth();
   const canEdit = hasRole('fraud_analyst') || hasRole('admin');
   ```

3. **Conditional UI rendering:**
   - Hide "+ Add Block" button for viewers
   - Hide "Unblock" buttons for viewers (show "-" instead)
   - Show "(Read-only access)" indicator for viewers

---

## User Roles

| Role | Permissions | Test User |
|------|-------------|-----------|
| **fraud_analyst** | Full access - create/edit/delete rules and blocklists | john.smith / FraudDetect123! |
| **fraud_viewer** | Read-only access - view rules, transactions, blocklists | sarah.jones / ViewOnly123! |
| **admin** | Full access - same as fraud_analyst | admin.user / Admin123! |

---

## Testing Instructions

### Test as Fraud Analyst (Full Access)
1. Login as `john.smith` / `FraudDetect123!`
2. Navigate to Rules page
3. ✅ "Create Rule" button should be visible
4. ✅ "Edit", "Delete", "Enable/Disable" buttons should be visible
5. Navigate to Blocklists page
6. ✅ "+ Add Block" button should be visible
7. ✅ "Unblock" buttons should be visible

### Test as Fraud Viewer (Read-Only)
1. Login as `sarah.jones` / `ViewOnly123!`
2. Navigate to Rules page
3. ❌ "Create Rule" button should be hidden
4. ❌ "Edit", "Delete", "Enable/Disable" buttons should be hidden
5. ✅ "(Read-only access)" indicator should be visible
6. Navigate to Blocklists page
7. ❌ "+ Add Block" button should be hidden
8. ❌ "Unblock" buttons should be hidden (shows "-")
9. ✅ "(Read-only access)" indicator should be visible

### Test API Security
```bash
# Get token for fraud_viewer
TOKEN=$(curl -s -X POST http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=fraud-ui&username=sarah.jones&password=ViewOnly123!&grant_type=password" \
  | jq -r '.access_token')

# Try to create a rule (should fail with 403 Forbidden)
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Rule",
    "ruleType": "AMOUNT_THRESHOLD",
    "enabled": true,
    "priority": 100,
    "thresholdAmount": 10000
  }'

# Expected response: 403 Forbidden
```

---

## Security Configuration

### SecurityConfig.java
- `@EnableMethodSecurity` is already enabled in SecurityConfig
- JWT token extraction includes realm roles from Keycloak
- Roles are prefixed with "ROLE_" (e.g., "fraud_analyst" → "ROLE_FRAUD_ANALYST")

### Keycloak Role Mapping
- Roles are defined in Keycloak realm: `fraud-detection`
- Roles are assigned to users in Keycloak
- JWT tokens include roles in the `realm_access.roles` claim

---

## Files Modified

### Backend
- `fraud-rule-engine-api/src/main/java/com/fraud/ruleengine/controller/RuleController.java`
- `fraud-rule-engine-api/src/main/java/com/fraud/ruleengine/controller/BlocklistController.java`

### Frontend
- `fraud-rule-engine-ui/app/dashboard/rules/page.tsx`
- `fraud-rule-engine-ui/app/dashboard/blocklists/page.tsx`

---

## Notes

- **Dashboard and Transactions pages** are read-only by default, so no changes were needed
- **API health endpoints** remain public (no authentication required)
- **All authenticated users** can view data - only write operations are restricted
- **Future enhancement:** Could add `fraud_admin` role with additional permissions (e.g., user management)
