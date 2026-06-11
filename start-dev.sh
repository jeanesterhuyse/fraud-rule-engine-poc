#!/bin/bash

# Fraud Rule Engine - Complete Development Startup Script
# This script starts all services and configures Keycloak automatically

set -e

KEYCLOAK_URL="http://localhost:8180"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "Starting Fraud Rule Engine Development Environment"
echo "=========================================="
echo ""

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install Docker Desktop."
    exit 1
fi

# Step 1: Start all Docker services
echo "📦 Starting Docker services..."
cd "$SCRIPT_DIR"
docker-compose up -d

echo ""
echo "⏳ Waiting for services to become healthy..."
echo "   This may take 60-90 seconds on first start..."

# Wait for PostgreSQL
echo -n "   Waiting for PostgreSQL..."
until docker exec fraud-postgres pg_isready -U fraud_user -d fraud_rule_engine &> /dev/null; do
    echo -n "."
    sleep 2
done
echo " ✅"

# Wait for Kafka
echo -n "   Waiting for Kafka..."
until docker exec fraud-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 &> /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ✅"

# Wait for API
echo -n "   Waiting for API..."
until curl -sf http://localhost:8080/actuator/health &> /dev/null; do
    echo -n "."
    sleep 2
done
echo " ✅"

# Wait for Keycloak
echo -n "   Waiting for Keycloak..."
until curl -sf "${KEYCLOAK_URL}/health/ready" &> /dev/null; do
    echo -n "."
    sleep 2
done
echo " ✅"

echo ""
echo "✅ All services are healthy!"
echo ""

# Step 2: Configure Keycloak (check if already configured)
echo "🔐 Configuring Keycloak authentication..."

# Check if realm already exists
REALM_CHECK=$(curl -s -o /dev/null -w "%{http_code}" "${KEYCLOAK_URL}/realms/fraud-detection")

if [ "$REALM_CHECK" = "200" ]; then
    echo "✅ Keycloak realm 'fraud-detection' already configured"
else
    echo "📥 Setting up Keycloak realm and users..."
    bash "$SCRIPT_DIR/setup-keycloak.sh"
    if [ $? -eq 0 ]; then
        echo "✅ Keycloak configured successfully"
    else
        echo "⚠️  Keycloak setup failed, but services are running"
        echo "   You can manually run: ./setup-keycloak.sh"
    fi
fi

echo ""
echo "=========================================="
echo "✅ Fraud Rule Engine is Ready!"
echo "=========================================="
echo ""
echo "🌐 Access Points:"
echo "   Frontend UI:        http://localhost:3000"
echo "   Login Page:         http://localhost:3000/login-keycloak"
echo "   API:                http://localhost:8080"
echo "   Grafana Logs:       http://localhost:3001"
echo "   Keycloak Admin:     http://localhost:8180 (admin/admin)"
echo ""
echo "🔐 Test Users:"
echo "   Analyst:  john.smith / FraudDetect123!"
echo "   Viewer:   sarah.jones / ViewOnly123!"
echo "   Admin:    admin.user / Admin123!"
echo ""
echo "📊 Database:"
echo "   - 16 active fraud detection rules loaded"
echo "   - 2 blocked customers in blocklist"
echo "   - 2 blocked merchants in blocklist"
echo ""
echo "🚀 Next Steps:"

# Check if port 3000 is available
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "   ⚠️  WARNING: Port 3000 is already in use!"
    echo "   Run: lsof -ti:3000 | xargs kill -9  (to free the port)"
    echo ""
else
    echo "   ✅ Port 3000 is available"
fi

echo "   1. Start the UI: cd fraud-rule-engine-ui && npm run dev"
echo "   2. Open: http://localhost:3000/login-keycloak"
echo "   3. Login with john.smith / FraudDetect123!"
echo ""
echo "📝 To stop all services: docker-compose down"
echo "🗑️  To reset everything:  docker-compose down -v"
echo ""
