#!/bin/bash

# Fraud Rule Engine - Prerequisites Check
# Run this script to verify your system has all required software

echo "=========================================="
echo "Checking Prerequisites for Fraud Rule Engine"
echo "=========================================="
echo ""

ERRORS=0
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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

# Check Java and auto-switch to Java 21
echo "☕ Checking Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "   Java installed: $JAVA_VERSION"

    # Extract version number
    if echo "$JAVA_VERSION" | grep -q "version \"21"; then
        echo "   ✅ Java 21 detected - CORRECT VERSION"
    else
        echo "   ⚠️  Java 21 recommended but you have: $JAVA_VERSION"
        echo ""

        # Try to auto-switch to Java 21
        if [ -f "$SCRIPT_DIR/use-java-21.sh" ]; then
            echo "   🔄 Attempting to auto-switch to Java 21..."
            source "$SCRIPT_DIR/use-java-21.sh" 2>/dev/null
            if [ $? -eq 0 ]; then
                JAVA_VERSION=$(java -version 2>&1 | head -n 1)
                echo "   ✅ Successfully switched to: $JAVA_VERSION"
                echo "   ✅ JAVA_HOME set to: $JAVA_HOME"
                echo ""
                echo "   💡 This is temporary for your current shell session."
                echo "      To make it permanent, add this to your ~/.zshrc or ~/.bash_profile:"
                echo "      export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
                echo "      export PATH=\$JAVA_HOME/bin:\$PATH"
            else
                # Auto-switch failed
                if [[ "$OSTYPE" == "darwin"* ]] && command -v /usr/libexec/java_home &> /dev/null; then
                    if /usr/libexec/java_home -v 21 &> /dev/null; then
                        JAVA_21_HOME=$(/usr/libexec/java_home -v 21)
                        echo "   ℹ️  Java 21 found at: $JAVA_21_HOME"
                        echo ""
                        echo "   📝 Run this to switch (temporary):"
                        echo "      source ./use-java-21.sh"
                        echo ""
                        echo "   📝 Or add to ~/.zshrc for permanent switch:"
                        echo "      export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
                        echo "      export PATH=\$JAVA_HOME/bin:\$PATH"
                    else
                        echo "   ❌ Java 21 not found on system"
                        echo "      Download Java 21 from: https://adoptium.net/"
                    fi
                else
                    echo "   ❌ Java 21 not found"
                    echo "      Download Java 21 from: https://adoptium.net/"
                fi
                ERRORS=$((ERRORS + 1))
            fi
        else
            # Helper script not found, manual instructions
            if [[ "$OSTYPE" == "darwin"* ]] && command -v /usr/libexec/java_home &> /dev/null; then
                echo "   🔍 Checking for available Java versions:"
                /usr/libexec/java_home -V 2>&1 | grep -E "^\s+[0-9]+" | sed 's/^/      /'
                echo ""
                if /usr/libexec/java_home -v 21 &> /dev/null; then
                    echo "   ✅ Java 21 is available"
                    echo "   📝 Run: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
                else
                    echo "   ❌ Java 21 not found - Download from: https://adoptium.net/"
                fi
            fi
            ERRORS=$((ERRORS + 1))
        fi
        echo ""
    fi

    # Check Maven's Java
    if command -v mvn &> /dev/null; then
        MVN_JAVA=$(mvn -version 2>&1 | grep "Java version")
        echo "   Maven will use: $MVN_JAVA"
        if echo "$MVN_JAVA" | grep -q "Java version: 21"; then
            echo "   ✅ Maven is configured to use Java 21"
        else
            echo "   ⚠️  Maven is NOT using Java 21"
            echo "      This might cause build failures. Make sure JAVA_HOME is set correctly."
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
    echo "  1. ./start-dev.sh  (handles everything automatically!)"
    echo ""
    echo "Or manually:"
    echo "  1. cd fraud-rule-engine-api"
    echo "  2. mvn clean package -DskipTests"
    echo "  3. cd .."
    echo "  4. ./start-dev.sh"
    echo ""
    echo "💡 Note: Scripts will auto-switch to Java 21 if needed"
    echo ""
    echo "If Maven still complains about Java version, try:"
    echo "  JAVA_HOME=\$(/usr/libexec/java_home -v 21) mvn clean package -DskipTests"
else
    echo "❌ Found $ERRORS issue(s) - please install missing software"
    echo ""
    echo "See docs/JAVA_21_SETUP.md for Java issues"
    echo "See docs/GETTING_STARTED.md for detailed instructions"
fi
echo "=========================================="
