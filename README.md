# Fraud Rule Engine POC

A real-time fraud detection system with a configurable rules engine, built with Spring Boot, React, Kafka, and PostgreSQL.

**Status:** ✅ Production-Ready POC  
**Last Updated:** June 12, 2026

---

> 📚 **New User?** See **[GETTING_STARTED.md](docs/GETTING_STARTED.md)** for complete setup instructions, prerequisites, test users, and troubleshooting.

## 🚀 Quick Start

**Prerequisites:** Java 21, Node.js 18+, Docker Desktop

```bash
# Check your system has everything needed (auto-switches to Java 21!)
./check-prerequisites.sh

# Start all backend services (builds JAR automatically if needed)
./start-dev.sh

# Start frontend (in new terminal)
cd fraud-rule-engine-ui
npm install  # First time only
npm run dev
```

**Access:** http://localhost:3000/login-keycloak  
**Login:** `john.smith` / `FraudDetect123!`

> 💡 **Have Java 25 or other versions?** No problem! The scripts **automatically switch to Java 21** for you. See **[docs/JAVA_21_SETUP.md](docs/JAVA_21_SETUP.md)** if you encounter issues.

---

## 📚 Documentation

**Start Here:**
- 📖 **[Getting Started Guide](GETTING_STARTED.md)** - Complete setup, test users, troubleshooting
- 🏗️ **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and components
- 🔐 **[Keycloak Quick Start](docs/guides/KEYCLOAK_QUICKSTART.md)** - 5-minute authentication setup
- 🔧 **[API Documentation](fraud-rule-engine-api/README.md)** - Backend API reference

**Additional Resources:**
- **Development:** [Development Guide](docs/DEVELOPMENT.md) | [Database Schema](docs/database/SCHEMA.md)
- **Design:** [Capitec Theme](docs/design/CAPITEC_THEME.md) | [Risk Scores](docs/RISK_SCORE_CALCULATION.md)
- **Authentication:** [Keycloak Setup](docs/guides/KEYCLOAK_SETUP.md) | [Test Users](docs/guides/TEST_USERS.md)
- **ADRs:** Located in `/docs/adr/` (PostgreSQL, Kafka, Rule Engine, etc.)

---

## 🏗️ What's Included

**Tech Stack:**
- **Backend:** Spring Boot 3.2, Java 21
- **Frontend:** Next.js 14, React 18, TypeScript
- **Database:** PostgreSQL 15 with Flyway migrations
- **Message Broker:** Apache Kafka
- **Authentication:** Keycloak OAuth2/OIDC
- **Observability:** Grafana + Loki

**Key Features:**
- ✅ **12 rule types** - Blocklists, cross-border, CNP fraud, time-of-day anomalies, structuring, etc.
- ✅ **Real-time processing** - Kafka event streaming with async processing
- ✅ **RBAC** - Fraud Analyst (full access) vs Viewer (read-only)
- ✅ **Enterprise observability** - Grafana Loki with structured JSON logging
- ✅ **Audit trail** - Transaction history preserved even after rule deletion
- ✅ **Professional UI** - Capitec-branded design with responsive layout
- ✅ **Test coverage** - 62 unit tests

**Rule Types:**
Customer Blocklist (100) • Merchant Blocklist (95) • Amount Threshold (50-100) • Geographic Anomaly (75) • Merchant Risk (65) • Amount Range (70) • Time of Day Anomaly (60) • Round Amount (55-65) • CNP High Risk (60-75) • Currency Mismatch (55) • Cross-Border High Risk (90) • Large Withdrawal (50-80)

**Architecture:**
```
React UI (Next.js) ←→ Spring Boot API ←→ PostgreSQL
                           ↓
                       Kafka + DLQ
```

> See [Architecture Overview](docs/ARCHITECTURE.md) for detailed system design.

---

## 🔗 Access Points

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| **Frontend UI** | http://localhost:3000 | See test users | Main application |
| **Grafana Dashboard** | http://localhost:3001 | Anonymous | Logs & monitoring |
| **pgAdmin** | http://localhost:5050 | admin@fraud.local / admin | Database web UI |
| **Keycloak Admin** | http://localhost:8180 | admin / admin | User management |
| **API** | http://localhost:8080 | Requires token | REST API |
| **Kafka UI** | http://localhost:8090 | None | Kafka monitoring |

---

## ☕ Java 21 Setup

This project **requires Java 21**. If you have multiple Java versions (like Java 25), you need to ensure Maven uses Java 21.

### Quick Fix (macOS)

```bash
# Set Java 21 for current terminal session
source ./use-java-21.sh

# Verify it worked
mvn -version | grep "Java version"
# Should show: Java version: 21.x.x
```

### Permanent Fix (macOS)

Add to your `~/.zshrc` or `~/.bash_profile`:

```bash
# Use Java 21 by default
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

Then restart your terminal or run `source ~/.zshrc`.

### Install Java 21

If you don't have Java 21 installed:

**macOS:**
```bash
# Option 1: Homebrew
brew install openjdk@21

# Option 2: Download from Adoptium
# Visit: https://adoptium.net/
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt-get install openjdk-21-jdk

# RHEL/Fedora
sudo dnf install java-21-openjdk-devel
```

**Verify Installation:**
```bash
/usr/libexec/java_home -V  # macOS - lists all Java versions
java -version              # Should show 21.x.x
mvn -version               # Maven should use Java 21
```

---

## 🐛 Troubleshooting

See [GETTING_STARTED.md - Troubleshooting](GETTING_STARTED.md#-troubleshooting) for common issues:
- Port conflicts
- API won't start
- Keycloak configuration
- Database connection issues
- UI errors
- **Java version conflicts** (see [docs/JAVA_21_SETUP.md](docs/JAVA_21_SETUP.md))

---

**Maintainer:** Development Team  
**License:** Internal - Capitec Bank
