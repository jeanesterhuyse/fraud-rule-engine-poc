# Fraud Rule Engine POC

A real-time fraud detection system with a configurable rules engine, built with Spring Boot, React, Kafka, and PostgreSQL.

**Status:** ✅ Production-Ready POC  
**Last Updated:** June 12, 2026

---

> 📚 **New User?** See **[GETTING_STARTED.md](docs/GETTING_STARTED.md)** for complete setup instructions, prerequisites, test users, and troubleshooting.

## 🚀 Quick Start

```bash
# First-time setup: Build backend JAR
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..

# Start all backend services
./start-dev.sh

# Start frontend (in new terminal)
cd fraud-rule-engine-ui
npm install  # First time only
npm run dev
```

**Access:** http://localhost:3000/login-keycloak  
**Login:** `john.smith` / `FraudDetect123!`

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

## 🐛 Troubleshooting

See [GETTING_STARTED.md - Troubleshooting](GETTING_STARTED.md#-troubleshooting) for common issues:
- Port conflicts
- API won't start
- Keycloak configuration
- Database connection issues
- UI errors

---

**Maintainer:** Development Team  
**License:** Internal - Capitec Bank
