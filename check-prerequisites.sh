#!/bin/bash

# Fraud Rule Engine - Prerequisites Check
# Run this script to verify your system has all required software

echo "=========================================="
echo "Checking Prerequisites for Fraud Rule Engine"
echo "=========================================="
echo ""

ERRORS=0

# Check Docker
echo "🐳 Checking Docker..."
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo "   ✅ Docker installed: $DOCKER_VERSION"

    # Check if Docker is running
    if docker ps &> /dev/null; then
        echo "   ✅ Docker daemon is running"
    else
        echo "   ❌ Docker daemon is NOT running - please start Docker Desktop"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "   ❌ Docker NOT installed"
    echo "      Download from: https://www.docker.com/products/docker-desktop"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Docker Compose
echo "🐳 Checking Docker Compose..."
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version)
    echo "   ✅ Docker Compose installed: $COMPOSE_VERSION"
else
    echo "   ❌ Docker Compose NOT installed"
    echo "      Usually comes with Docker Desktop"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Java
echo "☕ Checking Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "   Java installed: $JAVA_VERSION"

    # Extract version number
    if echo "$JAVA_VERSION" | grep -q "version \"21"; then
        echo "   ✅ Java 21 detected - CORRECT VERSION"
    else
        echo "   ⚠️  Java 21 recommended but you have: $JAVA_VERSION"
        echo "      Maven enforcer will verify the correct version during build"
        echo "      If build fails, set JAVA_HOME to Java 21:"
        echo "      export JAVA_HOME=\$(/usr/libexec/java_home -v 21)  # macOS"
    fi

    # Check Maven's Java
    if command -v mvn &> /dev/null; then
        MVN_JAVA=$(mvn -version 2>&1 | grep "Java version")
        echo "   Maven will use: $MVN_JAVA"
        if echo "$MVN_JAVA" | grep -q "Java version: 21"; then
            echo "   ✅ Maven is configured to use Java 21"
        else
            echo "   ⚠️  Maven is NOT using Java 21 - set JAVA_HOME"
            echo "      export JAVA_HOME=\$(/usr/libexec/java_home -v 21)  # macOS"
        fi
    fi
else
    echo "   ❌ Java NOT installed"
    echo "      Download Java 21 from: https://adoptium.net/"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Maven
echo "📦 Checking Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "   ✅ Maven installed: $MVN_VERSION"
else
    echo "   ❌ Maven NOT installed"
    echo "      Download from: https://maven.apache.org/download.cgi"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Node.js
echo "🟢 Checking Node.js..."
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    NODE_MAJOR=$(echo $NODE_VERSION | cut -d'.' -f1 | sed 's/v//')

    echo "   Node.js installed: $NODE_VERSION"

    if [ "$NODE_MAJOR" -ge 18 ]; then
        echo "   ✅ Node.js 18+ detected - CORRECT VERSION"
    else
        echo "   ⚠️  Node.js 18+ recommended but you have v$NODE_MAJOR"
        echo "      Download from: https://nodejs.org/"
    fi
else
    echo "   ❌ Node.js NOT installed"
    echo "      Download from: https://nodejs.org/"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check npm
echo "📦 Checking npm..."
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm --version)
    echo "   ✅ npm installed: $NPM_VERSION"
else
    echo "   ❌ npm NOT installed (comes with Node.js)"
    ERRORS=$((ERRORS + 1))
fi
echo ""

# Check Git
echo "🔧 Checking Git..."
if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version)
    echo "   ✅ Git installed: $GIT_VERSION"
else
    echo "   ⚠️  Git NOT installed (recommended)"
    echo "      Download from: https://git-scm.com/"
fi
echo ""

# Summary
echo "=========================================="
if [ $ERRORS -eq 0 ]; then
    echo "✅ All prerequisites met! You're ready to build."
    echo ""
    echo "Next steps:"
    echo "  1. cd fraud-rule-engine-api"
    echo "  2. mvn clean package -DskipTests"
    echo "  3. cd .."
    echo "  4. ./start-dev.sh"
else
    echo "❌ Found $ERRORS issue(s) - please install missing software"
    echo ""
    echo "See docs/GETTING_STARTED.md for detailed instructions"
fi
echo "=========================================="
