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

# Check Java version and switch to Java 21 if needed
echo "☕ Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    if ! echo "$JAVA_VERSION" | grep -q "version \"21"; then
        echo "⚠️  Current Java version: $JAVA_VERSION"
        if [[ "$OSTYPE" == "darwin"* ]] && command -v /usr/libexec/java_home &> /dev/null; then
            if /usr/libexec/java_home -v 21 &> /dev/null; then
                export JAVA_HOME=$(/usr/libexec/java_home -v 21)
                export PATH="$JAVA_HOME/bin:$PATH"
                echo "✅ Switched to Java 21: $JAVA_HOME"
            else
                echo "❌ Java 21 not found. Please install Java 21 from https://adoptium.net/"
                exit 1
            fi
        else
            echo "❌ Java 21 required. Please set JAVA_HOME:"
            echo "   export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
            exit 1
        fi
    else
        echo "✅ Java 21 detected"
    fi
else
    echo "❌ Java not installed. Please install Java 21 from https://adoptium.net/"
    exit 1
fi
echo ""

# Check if backend JAR file exists
JAR_FILE="$SCRIPT_DIR/fraud-rule-engine-api/target/fraud-rule-engine-api-1.0.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Backend JAR file not found!"
    echo ""
    echo "   This is your first time running the project."
    echo "   Please build the backend first:"
    echo ""
    echo "   cd fraud-rule-engine-api"
    echo "   JAVA_HOME=\$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests"
    echo "   cd .."
    echo ""
    echo "   Then run this script again: ./start-dev.sh"
    echo ""
    exit 1
fi

# Step 0: Check port availability (non-blocking warning)
echo "🔍 Checking port availability..."
if [ -f "$SCRIPT_DIR/check-ports.sh" ]; then
    "$SCRIPT_DIR/check-ports.sh" || echo "⚠️  Some ports are in use but continuing..."
    echo ""
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
echo "   Grafana Dashboard:  http://localhost:3001 (No login - Dashboard pre-loaded)"
echo "   pgAdmin (DB UI):    http://localhost:5050 (admin@admin.com/admin)"
echo "   Keycloak Admin:     http://localhost:8180 (admin/admin)"
echo "   Kafka UI:           http://localhost:8090"
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
echo "   1. Start the UI: cd fraud-rule-engine-ui && npm run dev"
echo "   2. Open: http://localhost:3000/login-keycloak"
echo "   3. Login with john.smith / FraudDetect123!"
echo ""
echo "📝 To stop all services: docker-compose down"
echo "🗑️  To reset everything:  docker-compose down -v"
echo ""
