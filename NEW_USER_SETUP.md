# New User Setup Guide - Fraud Rule Engine POC

**Complete step-by-step guide for first-time setup**

---

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
1. **Docker Desktop** (4GB+ RAM recommended)
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version` and `docker-compose --version`

2. **Node.js 18+** (for frontend development)
   - Download: https://nodejs.org/
   - Verify: `node --version` (should be 18.x or higher)

3. **Java 21** (for backend development - optional for running)
   - Download: https://adoptium.net/
   - Verify: `java --version` (should be 21.x)
   - Note: Only needed if you want to rebuild the backend

4. **Git** (to clone the repository)
   - Download: https://git-scm.com/
   - Verify: `git --version`

### System Requirements
- **RAM:** 4GB minimum (8GB recommended)
- **Disk Space:** 5GB free space
- **Ports:** 3000, 3001, 5432, 8080, 8090, 8180, 9092, 2181, 3100 must be available

---

## Step 1: Clone the Repository

```bash
git clone <repository-url>
cd fraud-rule-engine-poc
```

---

## Step 2: Build the Backend (First Time Only)

The Docker image needs the compiled JAR file. Build it once:

```bash
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..
```

**Expected Output:**
```
BUILD SUCCESS
```

**Verify JAR exists:**
```bash
ls -l fraud-rule-engine-api/target/*.jar
```

You should see: `fraud-rule-engine-api-1.0.0-SNAPSHOT.jar`

---

## Step 3: Start All Backend Services

Run the automated startup script:

```bash
./start-dev.sh
```

**What this does:**
- ✅ Starts all Docker containers (PostgreSQL, Kafka, Keycloak, API, Grafana, Loki)
- ✅ Waits for all services to become healthy (60-90 seconds)
- ✅ Automatically configures Keycloak with test users
- ✅ Sets up fraud detection rules and blocklists in database

**Expected Output:**
```
========================================
✅ Fraud Rule Engine is Ready!
========================================

🌐 Access Points:
   Frontend UI:        http://localhost:3000
   Login Page:         http://localhost:3000/login-keycloak
   API:                http://localhost:8080
   Grafana Logs:       http://localhost:3001
   Keycloak Admin:     http://localhost:8180 (admin/admin)

🔐 Test Users:
   Analyst:  john.smith / FraudDetect123!
   Viewer:   sarah.jones / ViewOnly123!
   Admin:    admin.user / Admin123!
```

**Verify Services are Running:**
```bash
docker-compose ps
```

All services should show "healthy" status.

---

## Step 4: Start the Frontend

In a **new terminal window**:

```bash
cd fraud-rule-engine-ui
npm install  # First time only - installs dependencies
npm run dev
```

**Expected Output:**
```
- ready started server on 0.0.0.0:3000, url: http://localhost:3000
```

**Note:** Frontend runs on port 3000 (not in Docker). It will hot-reload when you make changes.

---

## Step 5: Access the Application

Open your browser and navigate to:

**🌐 http://localhost:3000/login-keycloak**

### Test Users

| Role | Username | Password | Permissions |
|------|----------|----------|-------------|
| **Fraud Analyst** | `john.smith` | `FraudDetect123!` | Full access - create/edit/delete rules & blocklists |
| **Fraud Viewer** | `sarah.jones` | `ViewOnly123!` | Read-only access - can only view data |
| **Admin** | `admin.user` | `Admin123!` | Full access - same as Analyst |

**Login with:** `john.smith` / `FraudDetect123!`

---

## Step 6: Explore the Application

### Dashboard
- View summary statistics
- See triggered transactions
- Monitor rule performance

### Rules Page
- View 16 pre-loaded fraud detection rules
- Create new rules (Analyst/Admin only)
- Edit/Delete/Enable/Disable rules (Analyst/Admin only)
- Read-only view for Viewers

### Blocklists Page
- View blocked customers and merchants
- Add new blocks (Analyst/Admin only)
- Unblock customers/merchants (Analyst/Admin only)

### Transactions Page
- View all triggered transactions
- Filter by date range and rule type
- See risk scores and match reasons

### Observability (Grafana)
- Access: http://localhost:3001
- Pre-loaded dashboard for log monitoring
- View real-time logs from all services

---

## Common Commands

### Start Everything (After First Setup)
```bash
./start-dev.sh                    # Start backend services
cd fraud-rule-engine-ui && npm run dev  # Start frontend (new terminal)
```

### Stop Services
```bash
docker-compose down               # Stop all Docker services
# Frontend: Press Ctrl+C in terminal where npm run dev is running
```

### View Logs
```bash
docker-compose logs -f            # All services
docker logs fraud-api -f          # API only
docker logs fraud-postgres -f     # Database only
docker logs fraud-keycloak -f     # Keycloak only
```

### Check Service Health
```bash
docker-compose ps                 # All services status
curl http://localhost:8080/actuator/health  # API health
```

### Reset Everything (Fresh Start)
```bash
docker-compose down -v            # Stop and delete all data
./start-dev.sh                    # Start fresh
```

---

## Troubleshooting

### Port Already in Use

**Problem:** Error "port is already allocated"

**Solution:**
```bash
# Check what's using a port (example: 8080)
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use the check-ports script
./check-ports.sh
```

### API Won't Start

**Problem:** `fraud-api` container keeps restarting

**Solution:**
```bash
# Check logs
docker logs fraud-api --tail 50

# Ensure JAR file exists
ls -l fraud-rule-engine-api/target/*.jar

# If missing, rebuild:
cd fraud-rule-engine-api
mvn clean package -DskipTests
docker-compose restart fraud-api
```

### Keycloak Not Configured

**Problem:** Can't login, realm not found

**Solution:**
```bash
# Run setup script manually
./setup-keycloak.sh

# Or restart Keycloak
docker-compose restart keycloak
sleep 30
./setup-keycloak.sh
```

### UI Won't Start

**Problem:** npm run dev fails

**Solution:**
```bash
cd fraud-rule-engine-ui

# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Try again
npm run dev
```

### Database Connection Failed

**Problem:** API logs show "Connection refused" to PostgreSQL

**Solution:**
```bash
# Check PostgreSQL is healthy
docker exec fraud-postgres pg_isready -U fraud_user -d fraud_rule_engine

# If not healthy, restart
docker-compose restart postgres

# Wait for it to be healthy
./start-dev.sh
```

### Kafka Errors

**Problem:** API logs show Kafka connection issues

**Solution:**
```bash
# Check Kafka is running
docker logs fraud-kafka --tail 20

# Restart Kafka and dependent services
docker-compose restart kafka
docker-compose restart fraud-api
```

---

## Development Workflow

### Making Backend Changes

1. **Edit Java code** in `fraud-rule-engine-api/src/main/java/`

2. **Rebuild and restart:**
```bash
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..
docker-compose restart fraud-api
```

3. **Verify:**
```bash
docker logs fraud-api -f
curl http://localhost:8080/actuator/health
```

### Making Frontend Changes

1. **Edit React/TypeScript code** in `fraud-rule-engine-ui/app/` or `fraud-rule-engine-ui/components/`

2. **Hot reload automatically** - Just save the file!

3. **Verify in browser** - Page reloads automatically

### Running Tests

**Backend:**
```bash
cd fraud-rule-engine-api
mvn test                          # Unit tests
mvn verify                        # Unit + Integration tests
```

**Frontend:**
```bash
cd fraud-rule-engine-ui
npm run build                     # Check for TypeScript errors
```

---

## Architecture Overview

```
┌─────────────────┐
│   React UI      │ http://localhost:3000
│   (Next.js)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring Boot    │ http://localhost:8080
│      API        │
└────────┬────────┘
         │
    ┌────┴────┬─────────────┬──────────┐
    ▼         ▼             ▼          ▼
┌────────┐ ┌──────┐  ┌──────────┐ ┌─────────┐
│Postgres│ │Kafka │  │ Keycloak │ │ Grafana │
│  :5432 │ │:9092 │  │  :8180   │ │  :3001  │
└────────┘ └──────┘  └──────────┘ └─────────┘
```

---

## Docker Services Overview

| Service | Port | Purpose | Required |
|---------|------|---------|----------|
| **fraud-api** | 8080 | Backend API (Spring Boot) | ✅ Yes |
| **postgres** | 5432 | Database (PostgreSQL) | ✅ Yes |
| **kafka** | 9092 | Message broker | ✅ Yes |
| **zookeeper** | 2181 | Kafka coordination | ✅ Yes |
| **keycloak** | 8180 | Authentication (OAuth2) | ✅ Yes |
| **loki** | 3100 | Log aggregation | ⚠️ Optional |
| **promtail** | - | Log collector | ⚠️ Optional |
| **grafana** | 3001 | Observability UI | ⚠️ Optional |
| **kafka-ui** | 8090 | Kafka monitoring | ⚠️ Optional |

---

## Next Steps

1. ✅ **Test Role-Based Access Control**
   - Login as `john.smith` (Analyst) - should see all buttons
   - Login as `sarah.jones` (Viewer) - should see "(Read-only access)"

2. ✅ **Create a Test Rule**
   - Navigate to Rules page
   - Click "Create Rule"
   - Select "Amount Threshold"
   - Set threshold to 50000
   - Save and verify it appears

3. ✅ **View Logs in Grafana**
   - Open http://localhost:3001
   - Click "Explore"
   - Select "Loki" datasource
   - Query: `{container_name="fraud-api"}`

4. ✅ **Explore API Documentation**
   - See `fraud-rule-engine-api/README.md`
   - Try API endpoints with curl

---

## Additional Resources

- **Architecture**: `docs/ARCHITECTURE.md`
- **API Guide**: `fraud-rule-engine-api/README.md`
- **Getting Started**: `GETTING_STARTED.md`
- **RBAC Guide**: `ROLE_BASED_ACCESS_CONTROL.md`
- **Risk Scores**: `docs/RISK_SCORE_CALCULATION.md`

---

## Support

If you encounter issues not covered in this guide:

1. Check `GETTING_STARTED.md` for more troubleshooting
2. Review Docker logs: `docker-compose logs <service-name>`
3. Check service health: `docker-compose ps`
4. Verify ports are free: `./check-ports.sh`

---

**Last Updated:** 2026-06-12  
**Status:** Production-Ready POC  
**Test Coverage:** 62 unit tests passing
