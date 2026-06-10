# Development Guide - Fraud Rule Engine POC

**Last Updated:** 2026-06-07

Complete guide for running, developing, and troubleshooting the fraud rule engine.

---

## Quick Start

### Prerequisites
- Docker Desktop running (4GB+ RAM)
- Java 21 (for backend development)
- Node.js 18+ (for frontend development)
- Maven 3.9+

### Start Everything

```bash
cd /Users/ct303856/fraud-rule-engine-poc

# Backend (Docker Compose)
make build          # Build and start all services
make health         # Verify services are healthy

# Frontend (separate terminal)
cd fraud-rule-engine-ui
npm install         # First time only
npm run dev
```

**Access:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Kafka UI: http://localhost:8090
- Credentials: `test` / `test`

### Stop Everything

```bash
# Frontend
pkill -f "next dev"

# Backend
make stop
# OR
docker-compose down
```

---

## Project Status

### Backend: ✅ 100% Complete
- All services running in Docker
- Rule engine processing transactions every 10s
- 17 REST API endpoints working
- JWT authentication functional
- 9 seed rules active
- 218+ transactions processed

### Frontend: 🟡 90% Complete
- Next.js 14 app built
- All pages created (dashboard, rules, transactions)
- Professional UI with Tailwind CSS
- **BLOCKER:** Authentication persistence issue (see Current Issues below)

---

## Architecture

```
Mock Producer → Kafka → Rule Engine → PostgreSQL
                           ↓
                        REST API
                           ↓
                      Next.js UI
```

**5 Docker Services:**
1. **postgres** - PostgreSQL 15 (port 5432)
2. **zookeeper** - Kafka coordination (port 2181)
3. **kafka** - Message broker (port 9092)
4. **fraud-api** - Spring Boot backend (port 8080)
5. **kafka-ui** - Monitoring UI (port 8090)

---

## Backend Development

### Build & Run Locally

```bash
cd fraud-rule-engine-api

# Build JAR
mvn clean package -DskipTests

# Run tests
mvn test

# Run integration tests (requires Docker)
mvn verify

# Run locally (requires Postgres + Kafka running)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker Build Process

The Dockerfile uses **Amazon Corretto 21** (not Alpine) to avoid certificate issues:

```dockerfile
FROM public.ecr.aws/amazoncorretto/amazoncorretto:21
COPY target/fraud-rule-engine-api-1.0.0-SNAPSHOT.jar /
USER 1000
ENTRYPOINT exec java $JAVA_OPTS -jar /fraud-rule-engine-api-1.0.0-SNAPSHOT.jar
```

**Important:** Build the JAR locally first, Docker just copies it.

### Database Access

```bash
# Shell access
make db-shell
# OR
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine

# Useful queries
\dt                                    # List tables
SELECT * FROM rules;                   # View rules
SELECT COUNT(*) FROM triggered_transactions;
SELECT * FROM triggered_transactions ORDER BY triggered_at DESC LIMIT 10;
```

### Kafka Access

```bash
# List topics
make kafka-topics
# OR
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
```

### API Testing

```bash
# Health check
curl http://localhost:8080/actuator/health

# Login and get token
make login
# OR
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# Use token
TOKEN="<your-token>"
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/rules | jq
```

---

## Frontend Development

### Setup & Run

```bash
cd fraud-rule-engine-ui

# Install dependencies
npm install

# Run dev server
npm run dev

# Build production
npm run build

# Lint
npm run lint
```

### Project Structure

```
fraud-rule-engine-ui/
├── app/                    # Pages (Next.js App Router)
│   ├── layout.tsx         # Root layout with AuthProvider
│   ├── page.tsx           # Homepage (redirects)
│   ├── login/             # Login page
│   ├── dashboard/         # Dashboard + Rules + Transactions
│   └── debug/             # Debug auth state
├── components/            # Reusable components
│   ├── ProtectedRoute.tsx
│   └── StatCard.tsx
├── contexts/              # React contexts
│   └── AuthContext.tsx    # Auth state management
├── lib/api/               # API clients
│   ├── client.ts          # Axios + JWT interceptor
│   ├── auth.ts
│   ├── dashboard.ts
│   ├── rules.ts
│   └── transactions.ts
└── types/                 # TypeScript definitions
    └── api.ts
```

### Configuration

- **API URL:** `http://localhost:8080/api/v1` (configured in `next.config.js`)
- **Tailwind:** v3 (converted from v4 to fix module issues)
- **TypeScript:** Strict mode enabled

---

## Docker Commands

### Using Make (Recommended)

```bash
make build          # Build and start all services
make start          # Start stopped services
make stop           # Stop all services
make restart        # Restart all services
make health         # Check service health
make logs           # View all logs
make backend-logs   # View backend logs only
make login          # Get JWT token
make db-shell       # PostgreSQL shell
make kafka-topics   # List Kafka topics
make clean          # Remove everything (containers + volumes)
```

### Using Docker Compose Directly

```bash
# Start
docker-compose up -d --build

# Status
docker-compose ps

# Logs
docker-compose logs -f
docker-compose logs -f fraud-api

# Stop
docker-compose stop

# Stop and remove
docker-compose down

# Remove volumes too (deletes data!)
docker-compose down -v
```

### Service Health Checks

All services have health checks that must pass before dependent services start:

- **postgres:** `pg_isready`
- **zookeeper:** `echo srvr | nc localhost 2181`
- **kafka:** `kafka-broker-api-versions`
- **fraud-api:** `curl http://localhost:8080/actuator/health`

Check with:
```bash
docker-compose ps
# All should show "healthy" status
```

---

## Troubleshooting

### Backend Won't Start

**Check logs:**
```bash
make backend-logs
# Look for errors about Kafka, Postgres, or migrations
```

**Common issues:**
1. **Port conflicts:** Another service using 5432, 9092, or 8080
   ```bash
   lsof -i :8080
   lsof -i :5432
   lsof -i :9092
   ```

2. **Kafka not ready:** Wait 30s for Kafka to be healthy
   ```bash
   docker-compose ps kafka
   ```

3. **Database migration failed:** Check flyway logs
   ```bash
   docker-compose logs fraud-api | grep -i flyway
   ```

### Kafka Issues

**Topics not created:**
```bash
# Manually create topic
docker exec -it fraud-kafka kafka-topics \
  --create \
  --topic fraud-transactions-jeanTest \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

**Consumer lag:**
```bash
# Check consumer group
docker exec -it fraud-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group fraud-rule-engine-group \
  --describe
```

### Frontend Issues

**Module errors:**
```bash
cd fraud-rule-engine-ui
rm -rf .next node_modules
npm install
npm run dev
```

**Auth not working:**
- Check `/debug` page at http://localhost:3000/debug
- Check browser console for errors
- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check Network tab for API calls

**CORS errors:**
- Backend CORS configured for `http://localhost:3000`
- Check SecurityConfig.java

### Database Issues

**Cannot connect:**
```bash
# Check if postgres is running
docker-compose ps postgres

# Test connection
docker exec -it fraud-postgres pg_isready -U fraud_user
```

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
# Migrations will run automatically
```

**Force re-run migrations:**
```bash
# Stop backend
docker-compose stop fraud-api

# Clean database (WARNING: deletes all data)
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Restart backend (migrations will run)
docker-compose up -d fraud-api
```

### Docker Resource Issues

**Check resource usage:**
```bash
docker stats
```

**Increase Docker resources:**
1. Open Docker Desktop
2. Settings → Resources
3. Increase Memory to 6-8GB (currently need 4GB minimum)
4. Increase CPUs to 4
5. Apply & Restart

### Service Health Checks

All services have health checks. Check status:
```bash
docker-compose ps
# All should show "healthy" status
```

**Individual health checks:**
```bash
# PostgreSQL
docker exec -it fraud-postgres pg_isready -U fraud_user

# Kafka
docker exec -it fraud-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Backend API
curl http://localhost:8080/actuator/health
```

### Mock Producer Issues

**Check if mock producer is enabled:**
```bash
docker exec -it fraud-api env | grep MOCK_PRODUCER
# Should see: APP_MOCK_PRODUCER_ENABLED=true
```

**Check mock producer logs:**
```bash
docker-compose logs fraud-api | grep -i "mock"
```

---

## Current Issues

### 🔴 CRITICAL: Frontend Authentication Persistence

**Problem:** Token not persisting in localStorage after login

**Symptoms:**
- Login succeeds (backend returns token)
- Console shows "Login successful"
- But token doesn't save to localStorage
- Navigation redirects back to login
- `/debug` page shows "Has Token: No"

**Root Cause (Theory):**
- Next.js SSR/hydration clearing localStorage
- Race condition between setState and router.push
- AuthProvider re-mounting on navigation

**Debug Steps:**
1. Go to http://localhost:3000/debug
2. Check if token is present
3. Open browser console
4. Try login and watch for "AuthContext:" logs
5. Check Network tab for /auth/login response

**Next Fixes to Try:**
1. Add setTimeout delay before redirect (100ms)
2. Force synchronous localStorage verification
3. Try sessionStorage or cookies instead
4. Prevent AuthProvider re-mount
5. Add extensive localStorage operation logging

**Files Involved:**
- `fraud-rule-engine-ui/contexts/AuthContext.tsx`
- `fraud-rule-engine-ui/lib/api/auth.ts`
- `fraud-rule-engine-ui/app/login/page.tsx`

**Workaround:**
Backend APIs work perfectly via curl/Postman.

---

## Testing

### Backend Tests

```bash
cd fraud-rule-engine-api

# Unit tests
mvn test

# Integration tests (Testcontainers)
mvn verify

# Specific test
mvn test -Dtest=RuleEvaluatorOrchestratorTest

# With coverage
mvn test jacoco:report
```

### Frontend Tests

```bash
cd fraud-rule-engine-ui

# (Not yet implemented)
npm test
```

### Manual E2E Testing

**Backend:**
1. `make build` - Start all services
2. `make health` - Verify healthy
3. `make login` - Get token
4. Test API endpoints with curl

**Frontend:**
1. `npm run dev` - Start frontend
2. Open http://localhost:3000
3. Login with test/test
4. Navigate dashboard, rules, transactions
5. Check /debug for auth state

---

## Configuration Files

### Backend

**application.yml** - Base config (shared)
```yaml
spring:
  application:
    name: fraud-rule-engine-api
server:
  port: 8080
```

**application-docker.yml** - Docker profile
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/fraud_rule_engine
  kafka:
    bootstrap-servers: kafka:29092
```

**application-local.yml** - Local dev (uses real AWS MSK)
```yaml
spring:
  kafka:
    bootstrap-servers: b-2.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
    properties:
      security.protocol: SASL_SSL
```

**application-test.yml** - Tests (H2 in-memory)

### Frontend

**next.config.js**
```javascript
env: {
  NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'
}
```

**tailwind.config.js** - Tailwind v3 configuration

---

## Performance Notes

- **Rule Engine:** Processes transaction in ~10-50ms
- **Kafka Consumer:** Consumes every 10 seconds
- **Mock Producer:** Generates 1 transaction per 10 seconds
- **Database:** Indexed queries <10ms
- **API Response:** Most endpoints <100ms

---

## Data

### Seed Rules (9 total)

1. Large Transaction Alert (AMOUNT_THRESHOLD, priority 200)
2. Rapid Transaction Velocity (VELOCITY, priority 150)
3. High-Risk Country: Russia (GEOGRAPHIC_ANOMALY, priority 180)
4. High-Risk Country: North Korea (GEOGRAPHIC_ANOMALY, priority 180)
5. Gambling Merchant Alert (MERCHANT_RISK, priority 120)
6. Structuring Detection (AMOUNT_RANGE, priority 170)
7. Rapid-Fire Transactions (RAPID_FIRE, priority 160)
8. Cryptocurrency Exchange (MERCHANT_RISK, priority 110)
9. Very Large Transaction - Disabled (AMOUNT_THRESHOLD, priority 190, disabled)

### Live Metrics

After ~5 minutes of running:
- 200+ transactions generated
- 218+ triggered transactions
- Average risk score: ~80
- Total flagged amount: 4+ million ZAR

---

## Next Steps

### Critical
1. **Fix frontend authentication persistence** (blocking)

### High Priority
2. **Update styling to match fraud_tyr design system**
   - Extract tyr color palette
   - Update Tailwind config
   - Restyle all components

### Enhancements
3. Add Recharts visualizations to dashboard
4. Create/edit rule forms
5. Advanced filtering and search
6. Real-time updates (WebSockets)

See [BACKLOG.md](./BACKLOG.md) for complete list.

---

## Useful Commands Cheat Sheet

```bash
# Quick status
make health

# View live logs
make logs
make backend-logs

# Database queries
make db-shell
SELECT COUNT(*) FROM triggered_transactions;

# Kafka monitoring
make kafka-topics
open http://localhost:8090

# Get auth token
make login
export TOKEN=$(cat .token)

# Test API
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary | jq

# Clean restart
make clean
make build

# Frontend debug
open http://localhost:3000/debug
```

---

## Resources

- [README.md](./README.md) - Project overview and quick start
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status and known issues
- [SCHEMA_REVIEW.md](./SCHEMA_REVIEW.md) - Database design and API reference
- [BACKLOG.md](./BACKLOG.md) - Feature backlog and priorities
- [docs/adr/](./docs/adr/) - Architecture Decision Records
- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Detailed system architecture
