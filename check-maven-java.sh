#!/bin/bash

echo "=========================================="
echo "Maven Java Version Diagnostic"
echo "=========================================="
echo ""

echo "1️⃣ System Java Version:"
java -version 2>&1
echo ""

echo "2️⃣ JAVA_HOME Environment Variable:"
echo "   JAVA_HOME = $JAVA_HOME"
echo ""

echo "3️⃣ Which Java:"
which java
echo ""

echo "4️⃣ Maven Version (includes Java info):"
mvn -version 2>&1
echo ""

echo "5️⃣ Maven's Java System Properties:"
mvn help:system 2>/dev/null | grep -E "java\.(version|runtime\.version|vendor|home)" | head -10
echo ""

echo "=========================================="
echo "If Maven shows a different Java version than"
echo "what 'java -version' shows, then JAVA_HOME"
echo "is not set correctly for Maven."
echo ""
echo "Fix: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
echo "=========================================="
