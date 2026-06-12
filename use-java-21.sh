#!/bin/bash

# Helper script to set Java 21 for the current shell session
# Usage: source ./use-java-21.sh
#
# This script:
# 1. Finds Java 21 on your system
# 2. Sets JAVA_HOME to Java 21
# 3. Updates PATH to use Java 21
# 4. Verifies Maven will use Java 21

# Detect if script is being sourced or executed
if [ -n "$BASH_VERSION" ]; then
    (return 0 2>/dev/null) && SOURCED=1 || SOURCED=0
elif [ -n "$ZSH_VERSION" ]; then
    [[ $ZSH_EVAL_CONTEXT =~ :file$ ]] && SOURCED=1 || SOURCED=0
else
    SOURCED=0
fi

if [ $SOURCED -eq 0 ]; then
    echo "⚠️  This script must be sourced, not executed."
    echo "   Usage: source ./use-java-21.sh"
    echo "   Or:    . ./use-java-21.sh"
    exit 1
fi

# Find and set Java 21
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    if command -v /usr/libexec/java_home &> /dev/null; then
        if /usr/libexec/java_home -v 21 &> /dev/null 2>&1; then
            export JAVA_HOME=$(/usr/libexec/java_home -v 21)
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "✅ JAVA_HOME set to: $JAVA_HOME"
            echo "✅ Java version:"
            java -version 2>&1 | head -3
            echo ""
            if command -v mvn &> /dev/null; then
                echo "✅ Maven will use:"
                mvn -version 2>&1 | grep "Java version"
                echo ""
                echo "💡 You can now run: mvn clean package -DskipTests"
            fi
        else
            echo "❌ Java 21 not found on this system."
            echo ""
            echo "Available Java versions:"
            /usr/libexec/java_home -V 2>&1 | grep -E "^\s+[0-9]+"
            echo ""
            echo "Download Java 21 from: https://adoptium.net/"
            return 1
        fi
    else
        echo "❌ /usr/libexec/java_home not found (not macOS?)"
        return 1
    fi
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux - try common paths
    JAVA_21_PATHS=(
        "/usr/lib/jvm/java-21-openjdk-amd64"
        "/usr/lib/jvm/java-21-openjdk"
        "/usr/lib/jvm/java-21"
        "/usr/lib/jvm/jdk-21"
        "/opt/java/jdk-21"
    )

    FOUND=0
    for JAVA_PATH in "${JAVA_21_PATHS[@]}"; do
        if [ -d "$JAVA_PATH" ]; then
            export JAVA_HOME="$JAVA_PATH"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "✅ JAVA_HOME set to: $JAVA_HOME"
            java -version 2>&1 | head -3
            FOUND=1
            break
        fi
    done

    if [ $FOUND -eq 0 ]; then
        echo "❌ Java 21 not found in common locations:"
        for path in "${JAVA_21_PATHS[@]}"; do
            echo "   - $path (not found)"
        done
        echo ""
        echo "Download Java 21 from: https://adoptium.net/"
        echo "Or install via package manager:"
        echo "  Ubuntu/Debian: sudo apt-get install openjdk-21-jdk"
        echo "  RHEL/Fedora:   sudo dnf install java-21-openjdk-devel"
        return 1
    fi
else
    echo "⚠️  Unsupported OS: $OSTYPE"
    echo "Please set JAVA_HOME manually to Java 21:"
    echo "  export JAVA_HOME=/path/to/java-21"
    echo "  export PATH=\$JAVA_HOME/bin:\$PATH"
    return 1
fi
