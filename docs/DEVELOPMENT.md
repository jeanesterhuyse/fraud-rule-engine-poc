# Development Guide - Fraud Rule Engine POC

**Last Updated:** 2026-06-12  
**Status:** Production-Ready POC with RBAC

Complete guide for developing, testing, and troubleshooting the fraud rule engine.

---

## Quick Start

### Prerequisites
- **Docker Desktop** (4GB+ RAM recommended)
- **Java 21** (for backend development)
- **Node.js 18+** (for frontend development)
- **Maven 3.9+** (for backend builds)

### First-Time Setup

```bash
# 1. Clone repository
git clone <repository-url>
cd fraud-rule-engine-poc

# 2. Build backend
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..

# 3. Start all backend services (PostgreSQL, Kafka, Keycloak, API, Grafana)
./start-dev.sh

# 4. Start frontend (in new terminal)
cd fraud-rule-engine-ui
npm install  # First time only
npm run dev
```

**Access:**
- **Frontend UI:** http://localhost:3000/login-keycloak
- **API:** http://localhost:8080
- **Grafana Dashboard:** http://localhost:3001 (no login)
- **Keycloak Admin:** http://localhost:8180 (admin/admin)
- **Kafka UI:** http://localhost:8090

**Test Users:**
- **Analyst:** `john.smith` / `FraudDetect123!` (full access)
- **Viewer:** `sarah.jones` / `ViewOnly123!` (read-only)
- **Admin:** `admin.user` / `Admin123!` (full access)

---

## Project Status

### ✅ Backend: 100% Complete (Production-Ready)
- Spring Boot 3.2.5 + Java 21
- PostgreSQL 15 with Flyway migrations
- Apache Kafka for event processing
- **Keycloak OAuth2/OIDC** authentication
- **Role-based access control (RBAC)**
- 62 unit tests passing, 8 integration tests (optional)
- 12 rule types with 16 active rules
- Async processing with custom thread pool
- Enterprise observability with Grafana + Loki
- Custom exception handling
- Comprehensive logging with MDC context

### ✅ Frontend: 100% Complete (Production-Ready)
- Next.js 14 with App Router
- TypeScript strict mode
- **Keycloak integration** with OAuth2/OIDC
- **Role-based UI** with usePermissions hook
- Tailwind CSS with Capitec branding
- Dashboard with metrics
- Rules management (CRUD)
- Blocklists management
- Transactions viewer
- Professional error handling

### ✅ Infrastructure: 100% Complete
- Docker Compose orchestration
- 9 services (API, DB, Kafka, Keycloak, Grafana, Loki, etc.)
- Automated setup script (`./start-dev.sh`)
- Health checks on all services
- Volume persistence
- Network isolation

---

## Architecture

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   React UI  │─────▶│  Spring Boot │─────▶│ PostgreSQL  │
│  (Next.js)  │◀─────│     API      │◀─────│  Database   │
│  Port 3000  │      │  Port 8080   │      │  Port 5432  │
└─────────────┘      └──────┬───────┘      └─────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │    Kafka     │
                     │  Port 9092   │
                     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │   Keycloak   │
                     │  Port 8180   │
                     └──────────────┘
```

**9 Docker Services:**
1. **fraud-api** - Spring Boot backend (port 8080)
2. **postgres** - PostgreSQL 15 (port 5432)
3. **kafka** - Message broker (port 9092)
4. **zookeeper** - Kafka coordination (port 2181)
5. **keycloak** - OAuth2 authentication (port 8180)
6. **loki** - Log aggregation (port 3100)
7. **promtail** - Log collector
8. **grafana** - Observability UI (port 3001)
9. **kafka-ui** - Kafka monitoring (port 8090)

---

## Backend Development

### Build & Test

```bash
cd fraud-rule-engine-api

# Clean build
mvn clean package -DskipTests

# Run unit tests (62 tests)
mvn test

# Run with integration tests (70 total)
mvn verify

# Run specific test
mvn test -Dtest=AmountThresholdRuleEvaluatorTest

# Check code coverage
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

### Local Development (Without Docker)

```bash
# Prerequisites: PostgreSQL and Kafka running locally

cd fraud-rule-engine-api

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or with specific config
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fraud_rule_engine
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
mvn spring-boot:run
```

### Docker Development Workflow

```bash
# 1. Make code changes
# ... edit Java files in fraud-rule-engine-api/src/main/java/ ...

# 2. Rebuild JAR
cd fraud-rule-engine-api
mvn clean package -DskipTests

# 3. Restart Docker container
cd ..
docker-compose restart fraud-api

# 4. Verify changes
docker logs fraud-api -f
curl http://localhost:8080/actuator/health
```

### Adding a New Rule Type

See [ARCHITECTURE.md](ARCHITECTURE.md) for complete guide. Quick steps:

1. **Add enum:**
   ```java
   // RuleType.java
   public enum RuleType {
       // ... existing types
       NEW_RULE_TYPE
   }
   ```

2. **Create evaluator:**
   ```java
   @Component
   public class NewRuleEvaluator implements RuleEvaluationStrategy {
       @Override
       public boolean supports(RuleType ruleType) {
           return ruleType == RuleType.NEW_RULE_TYPE;
       }
       
       @Override
       public Optional<RuleMatch> evaluate(Transaction txn, Rule rule) {
           // Evaluation logic
       }
   }
   ```

3. **No orchestrator changes needed!** Spring auto-discovers new evaluators.

### Database Access

```bash
# Connect to PostgreSQL
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine

# Useful queries
\dt                                          # List tables
SELECT * FROM rules ORDER BY priority DESC;  # View rules
SELECT * FROM blocked_customers;             # View blocklist
SELECT COUNT(*) FROM triggered_transactions; # Count triggers
SELECT * FROM triggered_transactions ORDER BY triggered_at DESC LIMIT 10;

# Check Flyway migrations
SELECT * FROM flyway_schema_history;
```

### Kafka Access

```bash
# List topics
docker exec -it fraud-kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume messages
docker exec -it fraud-kafka kafka-console-consumer \
  --topic fraud-transactions-jeanTest \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --max-messages 10

# Check DLQ
docker exec -it fraud-kafka kafka-console-consumer \
  --topic fraud-transactions-dlq-jeanTest \
  --bootstrap-server localhost:9092 \
  --from-beginning

# Use Kafka UI (easier)
open http://localhost:8090
```

### API Testing with cURL

```bash
# Get JWT token from Keycloak
TOKEN=$(curl -s -X POST http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=fraud-ui" \
  -d "username=john.smith" \
  -d "password=FraudDetect123!" \
  -d "grant_type=password" \
  | jq -r '.access_token')

# Test API endpoints
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/rules | jq

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/triggered-transactions | jq

# Health check (no auth required)
curl http://localhost:8080/actuator/health | jq
```

### Testing RBAC

```bash
# As Fraud Analyst (can create/edit)
TOKEN_ANALYST=$(curl -s -X POST http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=fraud-ui&username=john.smith&password=FraudDetect123!&grant_type=password" \
  | jq -r '.access_token')

curl -X POST http://localhost:8080/api/v1/rules \
  -H "Authorization: Bearer $TOKEN_ANALYST" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Rule","ruleType":"AMOUNT_THRESHOLD","enabled":true,"priority":100,"thresholdAmount":10000}'

# As Fraud Viewer (should get 403 Forbidden)
TOKEN_VIEWER=$(curl -s -X POST http://localhost:8180/realms/fraud-detection/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=fraud-ui&username=sarah.jones&password=ViewOnly123!&grant_type=password" \
  | jq -r '.access_token')

curl -X POST http://localhost:8080/api/v1/rules \
  -H "Authorization: Bearer $TOKEN_VIEWER" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Rule","ruleType":"AMOUNT_THRESHOLD","enabled":true,"priority":100,"thresholdAmount":10000}'
# Expected: 403 Forbidden
```

---

## Frontend Development

### Setup & Run

```bash
cd fraud-rule-engine-ui

# Install dependencies
npm install

# Run dev server (hot reload enabled)
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Lint code
npm run lint
```

### Project Structure

```
fraud-rule-engine-ui/
├── app/                           # Pages (Next.js App Router)
│   ├── layout.tsx                # Root layout
│   ├── providers.tsx             # KeycloakAuthProvider wrapper
│   ├── page.tsx                  # Homepage (redirects)
│   ├── login-keycloak/           # Keycloak login page
│   ├── callback/                 # OAuth callback
│   └── dashboard/                # Protected dashboard routes
│       ├── layout.tsx            # Dashboard layout with nav
│       ├── page.tsx              # Dashboard home
│       ├── rules/page.tsx        # Rules management
│       ├── blocklists/page.tsx   # Blocklists management
│       └── transactions/page.tsx # Transactions viewer
├── components/                    # Reusable components
│   ├── ProtectedRoute.tsx        # Route guard
│   ├── RuleEditModal.tsx         # Rule create/edit modal
│   └── StatCard.tsx              # Dashboard metric cards
├── contexts/                      # React contexts
│   └── KeycloakAuthContext.tsx   # Keycloak auth state
├── hooks/                         # Custom hooks
│   └── usePermissions.ts         # Role-based permissions hook
├── lib/                          # Utilities
│   ├── api/                      # API clients
│   │   ├── client.ts             # Axios + JWT interceptor
│   │   ├── rules.ts              # Rules API
│   │   ├── blocklists.ts         # Blocklists API
│   │   └── transactions.ts       # Transactions API
│   └── auth/                     # Auth utilities
│       ├── keycloak.ts           # Keycloak client setup
│       └── token-manager.ts      # Token management
└── types/                        # TypeScript definitions
    ├── api.ts                    # API types
    └── blocklist.ts              # Blocklist types
```

### Adding a New Page

1. **Create page file:**
   ```typescript
   // app/dashboard/new-page/page.tsx
   'use client';
   
   import { usePermissions } from '@/hooks/usePermissions';
   
   export default function NewPage() {
     const { canEdit } = usePermissions();
     
     return (
       <div>
         <h1>New Page</h1>
         {canEdit && <button>Edit</button>}
       </div>
     );
   }
   ```

2. **Add to navigation:**
   ```typescript
   // app/dashboard/layout.tsx
   const navigation = [
     // ... existing items
     { name: 'New Page', href: '/dashboard/new-page', current: pathname?.startsWith('/dashboard/new-page') },
   ];
   ```

### Using the Permissions Hook

```typescript
import { usePermissions } from '@/hooks/usePermissions';

export default function MyComponent() {
  const { canEdit, isFraudViewer, isFraudAnalyst, isAdmin } = usePermissions();
  
  return (
    <div>
      {canEdit && <button>Create</button>}
      {isFraudViewer && <p>Read-only access</p>}
    </div>
  );
}
```

### Environment Variables

Create `.env.local` for local overrides:

```bash
# API URL (default: http://localhost:8080/api/v1)
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

---

## Docker Commands

### Service Management

```bash
# Start all services
./start-dev.sh

# Stop all services
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v

# Restart specific service
docker-compose restart fraud-api
docker-compose restart keycloak

# View logs
docker-compose logs -f                  # All services
docker logs fraud-api -f                # API only
docker logs fraud-keycloak -f           # Keycloak only
docker logs fraud-grafana -f            # Grafana only

# Check service health
docker-compose ps                       # All services
docker inspect fraud-api | grep Health  # Specific service
```

### Rebuilding Services

```bash
# Rebuild API after code changes
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..
docker-compose up -d --build fraud-api

# Rebuild all services
docker-compose up -d --build
```

### Resource Management

```bash
# Check resource usage
docker stats

# Clean up unused resources
docker system prune -a

# Remove specific image
docker rmi fraud-rule-engine-poc-fraud-api:latest
```

---

## Troubleshooting

### Backend Won't Start

**Check logs:**
```bash
docker logs fraud-api --tail 50
```

**Common issues:**

1. **JAR file missing:**
   ```bash
   ls -l fraud-rule-engine-api/target/*.jar
   # If missing:
   cd fraud-rule-engine-api
   mvn clean package -DskipTests
   docker-compose restart fraud-api
   ```

2. **Port conflict (8080):**
   ```bash
   lsof -i :8080
   kill -9 <PID>
   ```

3. **Database not ready:**
   ```bash
   docker exec fraud-postgres pg_isready -U fraud_user
   # If not ready, restart:
   docker-compose restart postgres
   sleep 10
   docker-compose restart fraud-api
   ```

4. **Kafka not ready:**
   ```bash
   docker logs fraud-kafka --tail 20
   # Wait 30 seconds, then:
   docker-compose restart fraud-api
   ```

### Frontend Issues

**Module errors:**
```bash
cd fraud-rule-engine-ui
rm -rf .next node_modules package-lock.json
npm install
npm run dev
```

**Keycloak auth not working:**
```bash
# Check Keycloak is running
curl http://localhost:8180/health/ready

# Verify realm exists
curl http://localhost:8180/realms/fraud-detection

# Re-run setup script
./setup-keycloak.sh
```

**Port conflict (3000):**
```bash
lsof -i :3000
kill -9 <PID>
npm run dev
```

### Keycloak Issues

**Realm not configured:**
```bash
# Check if realm exists
curl http://localhost:8180/realms/fraud-detection

# If 404, run setup:
./setup-keycloak.sh
```

**Database error:**
```bash
# Keycloak DB might not exist
docker exec fraud-postgres psql -U fraud_user -d postgres -c "CREATE DATABASE keycloak;"
docker exec fraud-postgres psql -U fraud_user -d keycloak -c "CREATE SCHEMA keycloak AUTHORIZATION fraud_user;"
docker-compose restart keycloak
sleep 30
./setup-keycloak.sh
```

### Grafana Issues

**Dashboard not loading:**
```bash
# Check Grafana is running
curl http://localhost:3001/api/health

# Check Loki datasource
docker logs fraud-loki --tail 20

# Restart Grafana
docker-compose restart grafana
```

**Dashboard missing:**
```bash
# Dashboard should be in grafana-dashboards/
ls -l grafana-dashboards/fraud-detection-logs.json

# Check dashboard provisioning
docker exec fraud-grafana ls -l /etc/grafana/provisioning/dashboards/
```

### Database Issues

**Reset database:**
```bash
docker-compose down -v
./start-dev.sh
```

**Manual migration:**
```bash
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine -c "SELECT * FROM flyway_schema_history;"
```

### Performance Issues

**Check resource usage:**
```bash
docker stats
```

**Increase Docker resources:**
1. Open Docker Desktop
2. Settings → Resources
3. Increase Memory to 6-8GB
4. Increase CPUs to 4
5. Apply & Restart

---

## Testing

### Backend Tests

```bash
cd fraud-rule-engine-api

# Unit tests (62 tests)
mvn test

# Integration tests (8 tests, requires Docker)
mvn verify

# Test coverage report
mvn test jacoco:report
open target/site/jacoco/index.html

# Test specific rule evaluator
mvn test -Dtest=CustomerBlocklistRuleEvaluatorTest
mvn test -Dtest=CrossBorderHighRiskRuleEvaluatorTest
```

**Expected Results:**
```
Tests run: 62, Failures: 0, Errors: 0, Skipped: 8
BUILD SUCCESS
```

### Frontend Tests

Currently no automated tests. Manual testing:

1. Login as each user role
2. Verify RBAC (buttons visible/hidden)
3. Test CRUD operations
4. Check error handling

### End-to-End Testing

**Full stack test:**
```bash
# 1. Start backend
./start-dev.sh

# 2. Verify services
docker-compose ps  # All should be healthy
curl http://localhost:8080/actuator/health  # Should be UP

# 3. Start frontend
cd fraud-rule-engine-ui && npm run dev

# 4. Manual testing
# - Open http://localhost:3000/login-keycloak
# - Login as john.smith / FraudDetect123!
# - Create a new rule
# - Verify in database: docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine -c "SELECT * FROM rules;"
# - Login as sarah.jones / ViewOnly123!
# - Verify buttons are hidden (read-only)
```

---

## Configuration

### Backend Profiles

**docker** (default in Docker Compose):
- PostgreSQL: postgres:5432
- Kafka: kafka:29092
- Keycloak: keycloak:8080

**local** (for local development):
- PostgreSQL: localhost:5432
- Kafka: localhost:9092

**test** (for unit/integration tests):
- H2 in-memory database
- Embedded Kafka

### Environment Variables

**Backend (.env or docker-compose.yml):**
```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/fraud_rule_engine
SPRING_DATASOURCE_USERNAME=fraud_user
SPRING_DATASOURCE_PASSWORD=fraud_pass
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
APP_MOCK_PRODUCER_ENABLED=true
APP_MOCK_PRODUCER_INTERVAL_MS=10000
```

**Frontend (.env.local):**
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

---

## Code Quality Standards

### Backend (Java)

- **Lombok** for boilerplate reduction
- **@RequiredArgsConstructor** for DI
- **Strategy Pattern** for rule evaluators
- **@Async** for async processing
- **@PreAuthorize** for RBAC
- **RoleConstants** for centralized role strings
- **Comprehensive Javadoc** on all public APIs
- **SLF4J** logging with MDC context

### Frontend (TypeScript)

- **TypeScript strict mode** enabled
- **React Hooks** (no class components)
- **Custom hooks** for reusable logic (usePermissions)
- **Memoization** with useMemo for performance
- **Error boundaries** and try-catch
- **Proper types** (no `any` except error handling)
- **ESLint** configuration

---

## Documentation

- **[NEW_USER_SETUP.md](../NEW_USER_SETUP.md)** - Complete first-time setup guide
- **[README.md](../README.md)** - Project overview and features
- **[GETTING_STARTED.md](GETTING_STARTED.md)** - Quick start guide
- **[ROLE_BASED_ACCESS_CONTROL.md](../ROLE_BASED_ACCESS_CONTROL.md)** - RBAC implementation
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design decisions
- **[RISK_SCORE_CALCULATION.md](RISK_SCORE_CALCULATION.md)** - Risk scoring algorithms
- **[API README](../fraud-rule-engine-api/README.md)** - Backend API documentation

---

## Quick Commands Reference

```bash
# Start everything
./start-dev.sh
cd fraud-rule-engine-ui && npm run dev

# Stop everything
docker-compose down
# (Frontend: Ctrl+C)

# Fresh restart
docker-compose down -v
./start-dev.sh

# View logs
docker logs fraud-api -f
docker logs fraud-keycloak -f

# Database shell
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine

# Test API
curl http://localhost:8080/actuator/health

# Rebuild backend
cd fraud-rule-engine-api
mvn clean package -DskipTests
docker-compose restart fraud-api

# Run tests
mvn test                    # Unit tests
mvn verify                  # Unit + Integration

# Access services
open http://localhost:3000/login-keycloak  # Frontend
open http://localhost:3001                  # Grafana
open http://localhost:8090                  # Kafka UI
open http://localhost:8180                  # Keycloak Admin
```

---

**Last Reviewed:** 2026-06-12  
**All Tests:** ✅ 62 passing (0 failures)  
**Status:** Production-ready POC
