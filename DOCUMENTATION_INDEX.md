# Documentation Index

**Fraud Rule Engine POC - Complete Documentation**

Last Updated: June 9, 2026

---

## 🚀 Quick Start

Start here if you're new:

1. **[README.md](README.md)** - Project overview, quick start, features
2. **[KEYCLOAK_QUICKSTART.md](docs/guides/KEYCLOAK_QUICKSTART.md)** - 5-minute authentication setup
3. **[TEST_USERS.md](docs/guides/TEST_USERS.md)** - Login credentials

---

## 📖 Core Documentation

### System Architecture
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Complete system design, components, technology stack
- **[docs/RISK_SCORE_CALCULATION.md](docs/RISK_SCORE_CALCULATION.md)** - How risk scores are calculated for each rule type

### Observability & Monitoring
- **[docs/guides/OBSERVABILITY.md](docs/guides/OBSERVABILITY.md)** - Grafana Loki setup, LogQL queries, dashboard usage ⚡ NEW

### Development
- **[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)** - Development workflow, commands, troubleshooting
- **[docs/database/SCHEMA.md](docs/database/SCHEMA.md)** - Database schema, migrations, indexes

### Design
- **[docs/design/CAPITEC_THEME.md](docs/design/CAPITEC_THEME.md)** - UI/UX design system, colors, components

---

## 🔐 Authentication (Keycloak)

### Setup Guides
- **[docs/guides/KEYCLOAK_QUICKSTART.md](docs/guides/KEYCLOAK_QUICKSTART.md)** - 5-minute quick start guide
- **[docs/guides/KEYCLOAK_SETUP.md](docs/guides/KEYCLOAK_SETUP.md)** - Complete setup guide (62KB, comprehensive)
- **[docs/guides/TEST_USERS.md](docs/guides/TEST_USERS.md)** - Test credentials, API testing, scenarios

### Implementation Details
- **[docs/guides/KEYCLOAK_IMPLEMENTATION_SUMMARY.md](docs/guides/KEYCLOAK_IMPLEMENTATION_SUMMARY.md)** - What changed, architecture, OAuth2 flow
- **[keycloak-realm-config.json](keycloak-realm-config.json)** - Realm configuration (import/export)
- **[setup-keycloak.sh](setup-keycloak.sh)** - Automated setup script

---

## 🏗️ Architecture Decision Records (ADRs)

Located in `docs/adr/`:

- **[ADR-001](docs/adr/ADR-001-postgresql-as-persistence-layer.md)** - PostgreSQL as Persistence Layer
- **[ADR-002](docs/adr/ADR-002-only-triggered-transactions-persisted.md)** - Only Triggered Transactions Persisted
- **[ADR-004](docs/adr/ADR-004-kafka-topic-design-and-dlq.md)** - Kafka Topic Design and DLQ
- **[ADR-005](docs/adr/ADR-005-rule-engine-strategy-pattern.md)** - Rule Engine Strategy Pattern
- **[ADR-006](docs/adr/ADR-006-relational-storage-not-json.md)** - Relational Storage (Not JSON)

---

## 🔧 Component Documentation

### Backend (fraud-rule-engine-api)
- **[fraud-rule-engine-api/README.md](fraud-rule-engine-api/README.md)** - API endpoints, request/response examples
- **[fraud-rule-engine-api/CONFIGURATION.md](fraud-rule-engine-api/CONFIGURATION.md)** - Configuration reference, environment variables
- **[fraud-rule-engine-api/KAFKA_ERROR_HANDLING.md](fraud-rule-engine-api/KAFKA_ERROR_HANDLING.md)** - Kafka DLQ setup, error handling

### Frontend (fraud-rule-engine-ui)
- **[fraud-rule-engine-ui/README.md](fraud-rule-engine-ui/README.md)** - UI setup, development, build

---

## 📊 Session Summaries

- **[docs/guides/SESSION_SUMMARY_2026-06-09.md](docs/guides/SESSION_SUMMARY_2026-06-09.md)** - Keycloak implementation session (June 9, 2026)
- **[docs/guides/SESSION_SUMMARY_2026-06-10.md](docs/guides/SESSION_SUMMARY_2026-06-10.md)** - Production readiness refactoring: Exception handling, structured logging, Grafana Loki observability (June 10, 2026) ⚡

---

## 🎯 Documentation by Use Case

### I want to...

#### Run the System
→ Start with **[README.md](README.md)** Quick Start section

#### Understand Authentication
→ Read **[docs/guides/KEYCLOAK_QUICKSTART.md](docs/guides/KEYCLOAK_QUICKSTART.md)**, then **[docs/guides/TEST_USERS.md](docs/guides/TEST_USERS.md)**

#### View Logs & Monitor the System
→ Access **Grafana** at http://localhost:3001, read **[docs/guides/OBSERVABILITY.md](docs/guides/OBSERVABILITY.md)** for full guide ⚡

#### Develop New Features
→ Read **[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)** and **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**

#### Understand Risk Scoring
→ Read **[docs/RISK_SCORE_CALCULATION.md](docs/RISK_SCORE_CALCULATION.md)**

#### Modify Database Schema
→ Read **[docs/database/SCHEMA.md](docs/database/SCHEMA.md)**

#### Change UI Design
→ Read **[docs/design/CAPITEC_THEME.md](docs/design/CAPITEC_THEME.md)**

#### Debug Issues
→ Use **Grafana Explore** (http://localhost:3001/explore) with LogQL queries to filter logs by transaction_id, level, or service

#### Troubleshoot Issues
→ Read **[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)** Troubleshooting section

#### Deploy to Production
→ Read **[docs/guides/KEYCLOAK_SETUP.md](docs/guides/KEYCLOAK_SETUP.md)** Production Considerations section

#### Add New Rule Type
→ Read **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** Extension Points section

#### Integrate with AD/LDAP
→ Read **[docs/guides/KEYCLOAK_SETUP.md](docs/guides/KEYCLOAK_SETUP.md)** AD/LDAP Federation section

---

## 📁 Documentation Structure

```
fraud-rule-engine-poc/
├── README.md                                    # Main entry point
├── DOCUMENTATION_INDEX.md                       # This file
├── SESSION_SUMMARY_2026-06-09.md               # Session notes
│
├── docs/
│   ├── guides/                                 # All guides & summaries
│   │   ├── KEYCLOAK_QUICKSTART.md             # 5-min setup
│   │   ├── KEYCLOAK_SETUP.md                  # Complete guide
│   │   ├── KEYCLOAK_IMPLEMENTATION_SUMMARY.md # What changed
│   │   ├── TEST_USERS.md                      # Credentials
│   │   ├── OBSERVABILITY.md                   # Grafana Loki guide
│   │   ├── SESSION_SUMMARY_2026-06-09.md      # Keycloak session
│   │   ├── SESSION_SUMMARY_2026-06-10.md      # Observability session
│   │   ├── CLEANUP_RECOMMENDATIONS.md         # Cleanup guide
│   │   └── DOCUMENTATION_CLEANUP_SUMMARY.md   # Old cleanup notes
│   │
│   ├── ARCHITECTURE.md                         # System design
│   ├── DEVELOPMENT.md                          # Dev guide
│   ├── RISK_SCORE_CALCULATION.md               # Risk scoring
│   │
│   ├── adr/                                    # Architecture decisions
│   │   ├── ADR-001-postgresql-as-persistence-layer.md
│   │   ├── ADR-002-only-triggered-transactions-persisted.md
│   │   ├── ADR-004-kafka-topic-design-and-dlq.md
│   │   ├── ADR-005-rule-engine-strategy-pattern.md
│   │   └── ADR-006-relational-storage-not-json.md
│   │
│   ├── database/
│   │   └── SCHEMA.md                           # Database design
│   │
│   └── design/
│       └── CAPITEC_THEME.md                    # UI design system
│
├── fraud-rule-engine-api/
│   ├── README.md                               # API reference
│   ├── CONFIGURATION.md                        # Config guide
│   └── KAFKA_ERROR_HANDLING.md                 # Kafka DLQ
│
└── fraud-rule-engine-ui/
    └── README.md                               # Frontend setup
```

---

## 🔗 Quick Reference Links

### Access Points
- **Grafana Observability**: http://localhost:3001 (anonymous access enabled) ⚡ NEW
- Keycloak Admin: http://localhost:8180 (admin / admin)
- Frontend UI: http://localhost:3000 (john.smith / FraudDetect123!)
- Backend API: http://localhost:8080 (requires Bearer token)
- Kafka UI: http://localhost:8090
- Loki API: http://localhost:3100 (log aggregation backend)

### Test Users
| Username | Password | Role |
|----------|----------|------|
| john.smith | FraudDetect123! | Fraud Analyst (Full Access) |
| sarah.jones | ViewOnly123! | Fraud Viewer (Read-Only) |
| admin.user | Admin123! | Admin (Full Access) |

### Key Commands
```bash
# Start system
docker-compose up -d

# Setup Keycloak
./setup-keycloak.sh

# Frontend dev
cd fraud-rule-engine-ui && npm run dev

# Backend rebuild
cd fraud-rule-engine-api && mvn clean package -DskipTests
docker-compose build fraud-api && docker-compose up -d fraud-api
```

---

## 📝 Documentation Standards

### When to Create Documentation
- New features that change system behavior
- Configuration changes
- API endpoint changes
- Security or authentication changes
- Complex business logic (like risk scoring)

### Where to Put It
- **README.md** - High-level overview, quick start
- **docs/** - Detailed technical documentation
- **Component folders** - Component-specific docs
- **ADRs** - Architecture decisions
- **Session summaries** - Implementation notes

### Documentation Style
- Start with "Why" (purpose/problem)
- Then "What" (solution/feature)
- Then "How" (implementation/usage)
- Include examples
- Keep updated (add "Last Updated" dates)

---

## 🔄 Maintenance

### Documents to Update Regularly
- **README.md** - When features change
- **TEST_USERS.md** - When users/credentials change
- **docs/DEVELOPMENT.md** - When commands/workflow changes
- **docs/database/SCHEMA.md** - When schema changes (migrations)

### Documents That Are Stable
- **ADRs** - Never change (append new ADRs instead)
- **KEYCLOAK_SETUP.md** - Stable unless Keycloak version changes
- **Session summaries** - Historical, never change

---

## 📚 External Resources

### Technologies
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2)
- [Next.js Documentation](https://nextjs.org/docs)
- [Kafka Documentation](https://kafka.apache.org/documentation/)

### Fraud Detection
- [FICO Fraud Scoring](https://www.fico.com/en/products/fico-falcon-fraud-platform)
- [OWASP API Security](https://owasp.org/www-project-api-security/)

---

## 💡 Tips for Using This Documentation

1. **Start with the index** (this file) to find what you need
2. **README.md** is your quick reference
3. **Quick start guides** get you running fast
4. **Complete guides** explain everything in depth
5. **ADRs** explain "why" decisions were made
6. **Session summaries** show implementation history
7. **Use Ctrl+F** to search within large documents

---

## ✅ Documentation Completeness

| Area | Status | Key Documents |
|------|--------|---------------|
| **System Overview** | ✅ Complete | README.md, ARCHITECTURE.md |
| **Authentication** | ✅ Complete | KEYCLOAK_*.md, TEST_USERS.md |
| **Observability** | ✅ Complete | SESSION_SUMMARY_2026-06-10.md ⚡ NEW |
| **Development** | ✅ Complete | DEVELOPMENT.md |
| **Risk Scoring** | ✅ Complete | RISK_SCORE_CALCULATION.md |
| **Database** | ✅ Complete | database/SCHEMA.md |
| **API** | ✅ Complete | fraud-rule-engine-api/README.md |
| **Frontend** | ✅ Complete | fraud-rule-engine-ui/README.md |
| **Design** | ✅ Complete | design/CAPITEC_THEME.md |
| **ADRs** | ✅ Complete | 5 ADRs documented |
| **Exception Handling** | ✅ Complete | Code examples in SESSION_SUMMARY_2026-06-10.md ⚡ NEW |
| **Troubleshooting** | ✅ Complete | In DEVELOPMENT.md, KEYCLOAK_SETUP.md |

---

## 🎓 Learning Path

**New Developer (Day 1):**
1. README.md
2. KEYCLOAK_QUICKSTART.md
3. TEST_USERS.md
4. Try logging in!

**Understanding the System (Week 1):**
1. ARCHITECTURE.md
2. DEVELOPMENT.md
3. RISK_SCORE_CALCULATION.md
4. Browse ADRs

**Ready to Contribute (Week 2+):**
1. Read all docs/
2. Review code in rule evaluators
3. Understand database schema
4. Start with small changes

---

**Last Updated:** June 9, 2026  
**Maintained By:** Development Team  
**Total Documentation:** ~100KB across 20+ files
