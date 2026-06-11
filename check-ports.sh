#!/bin/bash

# Port Availability Checker for Fraud Rule Engine
# Checks all required ports and reports conflicts

echo "🔍 Checking port availability for Fraud Rule Engine..."
echo ""

PORTS=(
    "3000:Frontend UI"
    "3001:Grafana"
    "3100:Loki"
    "5432:PostgreSQL"
    "8080:API"
    "8090:Kafka UI"
    "8180:Keycloak"
    "9092:Kafka"
    "2181:Zookeeper"
)

ALL_CLEAR=true

for port_info in "${PORTS[@]}"; do
    IFS=':' read -r port service <<< "$port_info"

    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo "❌ Port $port ($service) is IN USE"
        echo "   Process: $(lsof -ti:$port | xargs ps -p | tail -n +2)"
        echo "   To free: lsof -ti:$port | xargs kill -9"
        echo ""
        ALL_CLEAR=false
    else
        echo "✅ Port $port ($service) is available"
    fi
done

echo ""
if [ "$ALL_CLEAR" = true ]; then
    echo "🎉 All ports are available! Ready to start services."
    exit 0
else
    echo "⚠️  Some ports are in use. Free them before starting services."
    exit 1
fi
