#!/bin/bash

# Keycloak Setup Script for Fraud Detection System
# This script configures Keycloak with realm, clients, and test users

set -e

KEYCLOAK_URL="http://localhost:8180"
ADMIN_USER="admin"
ADMIN_PASS="admin"
REALM_NAME="fraud-detection"

echo "=========================================="
echo "Keycloak Setup for Fraud Detection System"
echo "=========================================="
echo ""

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to be ready..."
until curl -sf "${KEYCLOAK_URL}/health/ready" > /dev/null; do
    echo "   Keycloak not ready yet, waiting..."
    sleep 5
done
echo "✅ Keycloak is ready!"
echo ""

# Get admin token
echo "🔑 Authenticating as admin..."
TOKEN_RESPONSE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASS}" \
  -d "grant_type=password" \
  -d "client_id=admin-cli")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get admin token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi
echo "✅ Authenticated successfully"
echo ""

# Check if realm exists
echo "🔍 Checking if realm '${REALM_NAME}' exists..."
REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}")

if [ "$REALM_EXISTS" = "200" ]; then
    echo "⚠️  Realm '${REALM_NAME}' already exists. Deleting..."
    curl -s -X DELETE \
      -H "Authorization: Bearer ${ACCESS_TOKEN}" \
      "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}"
    echo "✅ Existing realm deleted"
fi

# Import realm configuration
echo "📥 Importing realm configuration..."
curl -s -X POST "${KEYCLOAK_URL}/admin/realms" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d @keycloak-realm-config.json

if [ $? -eq 0 ]; then
    echo "✅ Realm '${REALM_NAME}' created successfully"
else
    echo "❌ Failed to create realm"
    exit 1
fi
echo ""

echo "=========================================="
echo "✅ Keycloak Setup Complete!"
echo "=========================================="
echo ""
echo "🔗 Keycloak Admin Console: ${KEYCLOAK_URL}"
echo "   Username: ${ADMIN_USER}"
echo "   Password: ${ADMIN_PASS}"
echo ""
echo "👥 Test Users Created:"
echo ""
echo "   1. John Smith (Fraud Analyst)"
echo "      Username: john.smith"
echo "      Password: FraudDetect123!"
echo "      Email: john.smith@capitec.co.za"
echo "      Roles: fraud_analyst, raas_consumer"
echo ""
echo "   2. Sarah Jones (Fraud Viewer)"
echo "      Username: sarah.jones"
echo "      Password: ViewOnly123!"
echo "      Email: sarah.jones@capitec.co.za"
echo "      Roles: fraud_viewer, raas_consumer"
echo ""
echo "   3. Admin User"
echo "      Username: admin.user"
echo "      Password: Admin123!"
echo "      Email: admin@capitec.co.za"
echo "      Roles: fraud_analyst, raas_consumer"
echo ""
echo "🔐 OpenID Connect Endpoints:"
echo "   Token: ${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/token"
echo "   UserInfo: ${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/userinfo"
echo "   JWKS: ${KEYCLOAK_URL}/realms/${REALM_NAME}/protocol/openid-connect/certs"
echo ""
echo "🎯 Next Steps:"
echo "   1. Start the backend API: cd fraud-rule-engine-api && mvn spring-boot:run"
echo "   2. Start the frontend UI: cd fraud-rule-engine-ui && npm run dev"
echo "   3. Login with any test user above"
echo ""
