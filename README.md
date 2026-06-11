# Fraud Rule Engine POC

A real-time fraud detection system with a rules engine, built with Spring Boot, React, Kafka, and PostgreSQL.

**Status:** ✅ Production-Ready with Enterprise Observability + Async Processing + Blocklists  
**Last Updated:** June 11, 2026 - 01:30

---

## 🚀 Quick Start (Automated Setup)

### Prerequisites
- Docker and Docker Compose (4GB+ RAM recommended)
- Node.js 18+ (for UI development)
- Java 21 (for API development)

### One-Command Startup

```bash
# Automated setup - configures everything automatically
./start-dev.sh

# Then start the UI
cd fraud-rule-engine-ui
npm install  # First time only
npm run dev
```

**Login:** http://localhost:3000/login-keycloak
- Username: `john.smith`
- Password: `FraudDetect123!`

> 📚 **New to the project?** See **[GETTING_STARTED.md](GETTING_STARTED.md)** for complete setup instructions, test users, and troubleshooting.

### Access Points
- 🎨 **UI Dashboard**: http://localhost:3000
- 🔐 **Login Page**: http://localhost:3000/login-keycloak
- 📊 **Grafana Observability**: http://localhost:3001 (anonymous access enabled)
- 🔐 **Keycloak Auth**: http://localhost:8180 (admin/admin)
- 🔧 **API**: http://localhost:8080
- 📈 **Kafka UI**: http://localhost:8090
- 🗄️ **PostgreSQL**: localhost:5432

### Development Mode

**API Development:**
```bash
cd fraud-rule-engine-api
mvn spring-boot:run
```

**UI Development:**
```bash
cd fraud-rule-engine-ui
npm install
npm run dev
```

---

## 📚 Documentation

### Core Documentation
- **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and components
- **[Development Guide](docs/DEVELOPMENT.md)** - Development workflow and conventions
- **[API Documentation](fraud-rule-engine-api/README.md)** - Backend API reference
- **[Risk Score Calculation](docs/RISK_SCORE_CALCULATION.md)** - How risk scores are calculated ⚡

### Authentication (Keycloak)
- **[Quick Start Guide](docs/guides/KEYCLOAK_QUICKSTART.md)** - 5-minute Keycloak setup ⚡
- **[Complete Setup](docs/guides/KEYCLOAK_SETUP.md)** - Full Keycloak configuration guide
- **[Test Users](docs/guides/TEST_USERS.md)** - User credentials and API testing
- **[Implementation Summary](docs/guides/KEYCLOAK_IMPLEMENTATION_SUMMARY.md)** - What was changed

### Design & Database
- **[Capitec Design System](docs/design/CAPITEC_THEME.md)** - UI/UX design system
- **[Database Schema](docs/database/SCHEMA.md)** - Database structure and migrations

### Architecture Decision Records (ADRs)
Located in `/docs/adr/`:
- ADR-001: PostgreSQL as Persistence Layer
- ADR-002: Only Triggered Transactions Persisted
- ADR-004: Kafka Topic Design and DLQ
- ADR-005: Rule Engine Strategy Pattern
- ADR-006: Relational Storage Not JSON

---

## 🏗️ System Architecture

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   React UI  │─────▶│  Spring Boot │─────▶│ PostgreSQL  │
│  (Next.js)  │◀─────│     API      │◀─────│  Database   │
└─────────────┘      └──────────────┘      └─────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │    Kafka     │
                     │  (Event Bus) │
                     └──────────────┘
```

**Key Features:**
- ✅ Real-time transaction processing via Kafka
- ✅ Rule-based fraud detection engine with risk scoring
- ✅ **12 comprehensive rule types** (blocklists, cross-border, CNP fraud, time-of-day anomalies, etc.)
- ✅ **Customer & merchant blocklists** with instant blocking (risk score 100)
- ✅ **Async processing architecture** for scalability (custom thread pool)
- ✅ **Keycloak OAuth2/OIDC authentication** (AD-ready)
- ✅ **Enterprise observability with Grafana Loki** (structured JSON logging)
- ✅ **Automated database initialization** (Keycloak DB auto-created)
- ✅ **Custom exception handling** with consistent error responses
- ✅ Professional Capitec-branded UI with logo and blocklists management
- ✅ RESTful API with JWT token validation
- ✅ Audit trail preservation (transactions kept when rules deleted)
- ✅ Error handling with Dead Letter Queue (DLQ)
- ✅ Role-based access control (fraud_analyst, fraud_viewer)
- ✅ **Comprehensive test suite** (54 unit tests, 8 optional integration tests)
- ✅ **Production-ready code quality** (clean, self-documenting)

---

## 🎯 Features

### Dashboard
- Summary statistics (triggers, active rules, risk scores)
- Average triggers per rule metric
- Quick navigation to Rules and Transactions

### Rules Management
- Create, edit, enable/disable, and delete rules
- 7 rule types: Amount Threshold, Velocity, Geographic Anomaly, Merchant Risk, Amount Range, Rapid Fire, Dormant Account
- Priority-based rule evaluation
- Dynamic form fields based on rule type

### Transactions
- View all triggered transactions
- Filter by date range and rule type
- Pagination (10/20/50/100 per page)
- Detailed transaction information with risk scores

---

## 📊 Rule Types (12 Types)

| Rule Type | Description | Key Parameters | Risk Score |
|-----------|-------------|----------------|------------|
| **Customer Blocklist** | Instant block for blocklisted customers | None (checks DB) | 100 |
| **Merchant Blocklist** | Instant block for blocklisted merchants | None (checks DB) | 95 |
| **Amount Threshold** | Triggers on transactions exceeding a specific amount | threshold_amount | 50-100 |
| **Geographic Anomaly** | Triggers on transactions from high-risk countries | country_code | 75 |
| **Merchant Risk** | Triggers on high-risk merchant categories | merchant_category | 65 |
| **Amount Range** | Triggers on transactions within a specific range (structuring) | min_amount, max_amount | 70 |
| **Time of Day Anomaly** | Triggers on transactions during unusual hours (2-5 AM) | start_hour, end_hour | 60 |
| **Round Amount** | Triggers on large round amounts (card testing) | minimum_amount, round_to_nearest | 55-65 |
| **CNP High Risk** | Card-not-present fraud at high-risk merchants | merchant_category | 60-75 |
| **Currency Mismatch** | Foreign currency in foreign country | customer_home_country, customer_home_currency | 55 |
| **Cross-Border High Risk** | Cross-border to high-risk countries | customer_home_country, country_code | 90 |
| **Large Withdrawal** | Large ATM/cash withdrawals | threshold_amount | 50-80 |

---

## 🗄️ Database

**Schema Version:** v5  
**Migration Tool:** Flyway

**Core Tables:**
- `rules` - Fraud detection rules configuration
- `triggered_transactions` - Transactions that matched rules  
- `flyway_schema_history` - Database migration tracking

**Important:** Rule deletion preserves transaction history (rule_id becomes NULL, denormalized fields maintain context)

---

## 🔧 Configuration

### Environment Variables

**API:**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/fraud_rule_engine
SPRING_DATASOURCE_USERNAME=fraud_user
SPRING_DATASOURCE_PASSWORD=fraud_pass_123
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
```

**UI:**
```properties
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

See [API Configuration](fraud-rule-engine-api/CONFIGURATION.md) for detailed settings.

---

## 🐛 Troubleshooting

**API won't start:**
- Check PostgreSQL: `docker-compose ps postgres`
- View logs: `docker logs fraud-api`

**UI shows connection errors:**
- Verify API: `curl http://localhost:8080/actuator/health`
- Check `.env.local` file

**Kafka errors:**
- Check Kafka: `docker-compose ps kafka`
- View Kafka UI: http://localhost:8090

---

## 📊 Observability & Monitoring

This system includes enterprise-grade observability powered by **Grafana Loki**.

### Access Grafana Dashboard
- **URL**: http://localhost:3001
- **Authentication**: Anonymous access enabled (Admin role)
- **Pre-built Dashboard**: "Fraud Detection - Log Monitoring"

### Features
- 📈 **Real-time log streaming** from all services
- 🔍 **Structured JSON logging** with MDC context (transaction_id, customer_id, rule_id)
- 🎯 **Log filtering by level** (INFO, DEBUG, WARN, ERROR, TRACE)
- 📊 **Visual dashboards** showing log rates, error counts, and service health
- 🔎 **LogQL queries** for advanced log analysis
- 💾 **7-day retention** policy for local development

### LogQL Query Examples
```
# All fraud-api logs
{container_name="fraud-api"}

# Only errors
{container_name="fraud-api", level="ERROR"}

# Filter by transaction ID
{container_name="fraud-api"} | json | transaction_id="TXN-12345"

# System-wide error rate
sum(rate({container_name=~"fraud-.*", level="ERROR"} [5m]))
```

**Why Loki?** 90% less resource usage than Elasticsearch (no full-text indexing, only label-based).

---

## 📝 Project Status

**Version:** 1.0.0-SNAPSHOT  
**Completed:**
- ✅ Backend API with rule engine
- ✅ Kafka integration with DLQ
- ✅ PostgreSQL with Flyway migrations
- ✅ React UI with Capitec theme
- ✅ Full CRUD for rules
- ✅ Transaction filtering & pagination
- ✅ Audit-safe rule deletion
- ✅ **Custom exception handling with @ControllerAdvice**
- ✅ **Structured JSON logging with Logback**
- ✅ **Grafana Loki observability stack**
- ✅ **Production-ready code cleanup**

**Future Enhancements:**
- Rule versioning
- Advanced analytics dashboards
- CSV export
- Batch operations
- Alert notifications (email/Slack)

---

**Maintainer:** Development Team  
**License:** Internal - Capitec Bank
