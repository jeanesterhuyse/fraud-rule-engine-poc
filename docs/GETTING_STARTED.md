# Getting Started - Fraud Rule Engine POC

**Complete setup and reference guide for developers and testers**

---

## 🚀 Quick Start (5 Minutes)

**For returning users who have already completed first-time setup:**

```bash
# Start all backend services
./start-dev.sh

# Start frontend (in new terminal)
cd fraud-rule-engine-ui && npm run dev
```

**Access:** http://localhost:3000/login-keycloak  
**Login:** `john.smith` / `FraudDetect123!`

> **First-time users:** Complete the First-Time Setup section below before running these commands.

---

## 📋 First-Time Setup

**Complete these steps once before your first `./start-dev.sh`**

### Prerequisites

Before you begin, ensure you have:

1. **Docker Desktop** (4GB+ RAM recommended)
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version` and `docker-compose --version`

2. **Node.js 18+** (for frontend)
   - Download: https://nodejs.org/
   - Verify: `node --version` (should be 18.x or higher)

3. **Java 21** (optional - only for backend development)
   - Download: https://adoptium.net/
   - Verify: `java --version` (should be 21.x)

4. **Git** (to clone the repository)
   - Verify: `git --version`

**System Requirements:**
- RAM: 4GB minimum (8GB recommended)
- Disk Space: 5GB free space
- Ports: 3000, 3001, 5050, 5432, 8080, 8090, 8180, 9092, 2181, 3100 must be available

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd fraud-rule-engine-poc
```

### Step 2: Build the Backend (First Time Only)

**Important:** You must build the JAR file before running `start-dev.sh` for the first time.

```bash
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..
```

**Expected Output:** `BUILD SUCCESS`

**Verify JAR exists:**
```bash
ls -l fraud-rule-engine-api/target/*.jar
```

You should see: `fraud-rule-engine-api-1.0.0-SNAPSHOT.jar`

> **Note:** If you skip this step, `start-dev.sh` will exit with a helpful error message telling you to build the JAR first.

### Step 3: Start All Backend Services

Now run the automated startup script:

```bash
./start-dev.sh
```

**What this does:**
- ✅ Starts all Docker services (API, Database, Kafka, Keycloak, Grafana)
- ✅ **Automated Keycloak database creation** (no manual steps)
- ✅ Configures authentication realm and test users
- ✅ Waits for all services to be healthy (60-90 seconds)
- ✅ Loads 16 fraud detection rules (12 rule types)
- ✅ Loads blocklist test data
- ✅ **Async processing** enabled with custom thread pool (fraud-async-1, fraud-async-2)

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

**Verify Services:**
```bash
docker-compose ps
```

All services should show "healthy" status.

### Step 4: Start the Frontend

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

**Note:** Frontend runs on port 3000 (not in Docker) and will hot-reload on changes.

### Step 5: Access the Application

Open your browser: **http://localhost:3000/login-keycloak**

Click "Sign In with Keycloak" and login with test credentials below.

---

## 🔐 Test User Credentials

| Role | Username | Password | Permissions |
|------|----------|----------|-------------|
| **Fraud Analyst** | `john.smith` | `FraudDetect123!` | Full access - create/edit/delete rules and blocklists |
| **Fraud Viewer** | `sarah.jones` | `ViewOnly123!` | **Read-only access** - can view all data but cannot modify |
| **Admin** | `admin.user` | `Admin123!` | Full access - same as Fraud Analyst |

> **Note:** Role-based access control (RBAC) is enforced on both backend API and frontend UI. Fraud Viewers will see "(Read-only access)" indicators and all create/edit/delete buttons are hidden.

---

## 🌐 Access Points

| Service | URL | Credentials | Purpose |
|---------|-----|-------------|---------|
| **Frontend UI** | http://localhost:3000 | See test users above | Main application |
| **Login Page** | http://localhost:3000/login-keycloak | - | Keycloak authentication |
| **API** | http://localhost:8080 | Requires Bearer token | REST API |
| **API Health** | http://localhost:8080/actuator/health | - | Health check endpoint |
| **Grafana Logs** | http://localhost:3001 | Anonymous (Admin role) | Log monitoring & dashboards |
| **pgAdmin** | http://localhost:5050 | admin@admin.com / admin | Database web UI |
| **Keycloak Admin** | http://localhost:8180 | admin / admin | User & role management |
| **Kafka UI** | http://localhost:8090 | - | Kafka topic monitoring |
| **PostgreSQL** | localhost:5432 | fraud_user / fraud_pass | Direct database access (CLI) |

> **Port Management:** Frontend uses port 3000 (explicit), Grafana uses 3001. Run `./check-ports.sh` to verify port availability.

---

## 📊 What's Included

After startup, the system includes:

### Fraud Detection Rules (16 active - 12 rule types)
- **CUSTOMER_BLOCKLIST** - Instant block for blocklisted customers (Risk: 100)
- **MERCHANT_BLOCKLIST** - Instant block for blocklisted merchants (Risk: 95)
- **AMOUNT_THRESHOLD** - Large transaction alerts (Risk: 50-100)
- **GEOGRAPHIC_ANOMALY** - High-risk country detection (Risk: 75)
- **CROSS_BORDER_HIGH_RISK** - Cross-border to high-risk countries (Risk: 90)
- **AMOUNT_RANGE** - Structuring detection (Risk: 70)
- **LARGE_WITHDRAWAL** - Large ATM withdrawals (Risk: 50-80)
- **TIME_OF_DAY_ANOMALY** - Unusual hours (2-5 AM) (Risk: 60)
- **ROUND_AMOUNT** - Card testing detection (Risk: 55-65)
- **CNP_HIGH_RISK** - Card-not-present fraud (Electronics, Jewelry, Travel) (Risk: 60-75)
- **CURRENCY_MISMATCH** - Foreign currency anomalies (Risk: 55)
- **MERCHANT_RISK** - High-risk merchants (Gambling, Crypto) (Risk: 65)

### Blocklist Test Data
- **Blocked Customers**: CUST-BLOCKED-001, CUST-BLOCKED-002
- **Blocked Merchants**: "Suspicious Electronics Ltd", "Fake Travel Agency"

### Database Schema
- Clean V1-V4 migrations with all 12 rule types
- Blocklist tables (blocked_customers, blocked_merchants)
- Audit-safe transaction history

---

## 🛠️ Development Workflow

### Common Commands

```bash
# Start everything (after first-time setup)
./start-dev.sh                              # Backend services
cd fraud-rule-engine-ui && npm run dev      # Frontend (new terminal)

# Stop services
docker-compose down                         # Stop all Docker services
# Frontend: Press Ctrl+C in terminal

# View logs
docker-compose logs -f                      # All services
docker logs fraud-api -f                    # API only
docker logs fraud-postgres -f               # Database only
docker logs fraud-keycloak -f               # Keycloak only

# Check service health
docker-compose ps                           # All services status
curl http://localhost:8080/actuator/health  # API health
```

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

### Reset Everything

**Soft Reset (Keep Data):**
```bash
docker-compose restart
```

**Hard Reset (Fresh Start):**
```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Start from scratch
./start-dev.sh
```

This will:
- Delete all data
- Recreate databases
- Reload seed rules and blocklists
- Reconfigure Keycloak

---

## 📊 Observability with Grafana

**Access:** http://localhost:3001

Grafana starts automatically with `./start-dev.sh` - no additional setup needed!

**Features:**
- ✅ **Anonymous access enabled** - No login required
- ✅ **Pre-configured Loki datasource** - Logs from all Docker containers
- ✅ **Pre-loaded dashboard:** "Fraud Detection - Log Monitoring"
- ✅ **Real-time log streaming** from API, Kafka, PostgreSQL, Keycloak

### How to Use Grafana

1. **Open Grafana:** http://localhost:3001 (opens directly, no login)

2. **View Pre-loaded Dashboard:**
   - Click "Dashboards" (left menu, four squares icon)
   - Click "Fraud Detection - Log Monitoring"
   - See real-time logs with filters and charts

3. **Explore Logs Manually:**
   - Click "Explore" (compass icon in left menu)
   - Datasource is already set to "Loki"
   - Try these queries:
     ```
     # All API logs
     {container_name="fraud-api"}
     
     # Only errors
     {container_name="fraud-api"} |= "ERROR"
     
     # Specific transaction
     {container_name="fraud-api"} |= "TXN-12345"
     
     # All errors across all services
     {container_name=~"fraud-.*"} |= "ERROR"
     ```

4. **Filter Logs by Level:**
   - In the dashboard, use the "Log Level" dropdown
   - Options: ALL, ERROR, WARN, INFO, DEBUG, TRACE

**Dashboard Panels:**
- 📊 Log Rate Over Time (requests per second)
- 🔴 Error Count
- ⚠️ Warning Count  
- 📝 Recent Log Entries (live stream)
- 🎯 Logs by Service
- 📈 Transaction Processing Rate

**Useful for:**
- Debugging API errors
- Monitoring transaction processing
- Tracking rule evaluations
- Viewing Kafka message flow
- Database query performance

---

## 🗄️ Database Access

### Option 1: pgAdmin (Web UI - Recommended) 🎯

**Access:** http://localhost:5050

**Login:**
- Email: `admin@admin.com`
- Password: `admin`

**First-Time Setup:**
1. Click "Add New Server"
2. **General Tab:**
   - Name: `Fraud Rule Engine`
3. **Connection Tab:**
   - Host: `postgres` (Docker network name)
   - Port: `5432`
   - Database: `fraud_rule_engine`
   - Username: `fraud_user`
   - Password: `fraud_pass`
4. Click "Save"

**Browse Tables:**
- Navigate: Servers → Fraud Rule Engine → Databases → fraud_rule_engine → Schemas → public → Tables
- Right-click any table → "View/Edit Data" → "All Rows"

**Features:**
- ✅ Visual table browser with grid view
- ✅ SQL query tool with syntax highlighting
- ✅ Export data (CSV, JSON, Excel)
- ✅ Edit records directly
- ✅ View relationships and schema
- ✅ Query history

### Option 2: Command Line (psql)

```bash
# Connect to PostgreSQL
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine

# Useful commands
\dt                          # List all tables
\d rules                     # Describe rules table
\d triggered_transactions    # Describe triggered_transactions

# Sample queries
SELECT id, name, rule_type, enabled, priority FROM rules ORDER BY priority DESC;
SELECT * FROM blocked_customers;
SELECT * FROM blocked_merchants;
SELECT id, transaction_id, rule_name, risk_score FROM triggered_transactions ORDER BY triggered_at DESC LIMIT 10;

# Exit
\q
```

---

## ❌ Troubleshooting

### Port Already in Use

**Symptom**: `Error: bind: address already in use`

**Solution**: Kill process using the port

```bash
# Find what's using port 3000 (UI)
lsof -i :3000
kill -9 <PID>

# Or for port 8080 (API)
lsof -i :8080
kill -9 <PID>

# Or use the check-ports script
./check-ports.sh
```

### API Won't Start

**Symptom**: API container keeps restarting

**Solution**: Check logs and ensure database is healthy

```bash
# Check logs
docker logs fraud-api --tail 50

# Ensure JAR file exists
ls -l fraud-rule-engine-api/target/*.jar

# If missing, rebuild:
cd fraud-rule-engine-api
mvn clean package -DskipTests
cd ..
docker-compose restart fraud-api

# Check database
docker exec fraud-postgres pg_isready -U fraud_user -d fraud_rule_engine
```

### Keycloak Won't Start

**Symptom**: `FATAL: database "keycloak" does not exist`

**Solution**: The postgres-init script should create this automatically. If it doesn't:

```bash
docker exec fraud-postgres psql -U fraud_user -d postgres -c "CREATE DATABASE keycloak;"
docker exec fraud-postgres psql -U fraud_user -d keycloak -c "CREATE SCHEMA keycloak AUTHORIZATION fraud_user;"
docker restart fraud-keycloak
./setup-keycloak.sh
```

### Keycloak Realm Not Configured

**Symptom**: Can't login, realm not found

**Solution**: Run the setup script manually

```bash
./setup-keycloak.sh

# Or restart Keycloak and try again
docker-compose restart keycloak
sleep 30
./setup-keycloak.sh
```

### UI Won't Start

**Symptom**: `npm run dev` fails

**Solution**: Remove and reinstall dependencies

```bash
cd fraud-rule-engine-ui

# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Try again
npm run dev
```

### Database Connection Failed

**Symptom**: API logs show "Connection refused" to PostgreSQL

**Solution**: Check PostgreSQL health

```bash
# Check PostgreSQL is healthy
docker exec fraud-postgres pg_isready -U fraud_user -d fraud_rule_engine

# If not healthy, restart
docker-compose restart postgres

# Wait for it to be healthy
./start-dev.sh
```

### Kafka Errors

**Symptom**: API logs show Kafka connection issues

**Solution**: Restart Kafka and dependent services

```bash
# Check Kafka is running
docker logs fraud-kafka --tail 20

# Restart Kafka and dependent services
docker-compose restart kafka
docker-compose restart fraud-api
```

### UI Shows TLS Certificate Warning

**Symptom**: Console shows `UNABLE_TO_GET_ISSUER_CERT_LOCALLY`

**Solution**: This is normal for local development with self-signed certificates. The app still works correctly.

---

## 🎯 Testing the System

### Test Scenario 1: View Rules

1. Login at http://localhost:3000/login-keycloak
2. Navigate to Rules page
3. See all 16 rules with different types
4. Try enabling/disabling a rule

### Test Scenario 2: Create a New Rule

1. Click "Create Rule"
2. Select rule type (e.g., "Amount Threshold")
3. Fill in parameters
4. Submit and verify it appears in the list

### Test Scenario 3: Manage Blocklists

1. Navigate to Blocklists page
2. View blocked customers and merchants
3. Try adding a new blocked customer
4. Verify it appears in the list

### Test Scenario 4: View Transaction History

1. Navigate to Transactions page
2. See triggered transactions (if any)
3. Filter by date or rule type

### Test Scenario 5: Role-Based Access Control

1. Login as `john.smith` (Analyst) - should see all buttons
2. Logout and login as `sarah.jones` (Viewer) - should see "(Read-only access)"
3. Verify create/edit/delete buttons are hidden for Viewer

### Test Scenario 6: Grafana Dashboard

1. Open http://localhost:3001 (no login required)
2. Click "Dashboards" icon (four squares) in left menu
3. Select "Fraud Detection - Log Monitoring"
4. See real-time logs, error rates, and transaction processing
5. Use "Log Level" dropdown to filter (ALL, ERROR, WARN, INFO, DEBUG)

---

## 💡 Tips for Testers

1. **Use Multiple Browser Sessions** - Test different user roles in different browsers (Chrome vs Firefox)
2. **Check Grafana** - Visit http://localhost:3001 to see real-time logs
3. **Monitor API Logs** - Run `docker logs fraud-api -f` to see transaction processing
4. **Test Edge Cases** - Try invalid inputs, missing fields, special characters
5. **Check Async Processing** - Logs will show `fraud-async-1`, `fraud-async-2` threads

---

## 🏗️ Architecture Overview

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

### Docker Services

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

## 📚 Additional Documentation

- [Architecture Overview](ARCHITECTURE.md)
- [API Documentation](../fraud-rule-engine-api/README.md)
- [Keycloak Setup Guide](guides/KEYCLOAK_SETUP.md)
- [Database Schema](database/SCHEMA.md)
- [Risk Score Calculation](RISK_SCORE_CALCULATION.md)
- [RBAC Guide](ROLE_BASED_ACCESS_CONTROL.md)

---

## 🆘 Support

If you encounter issues not covered in this guide:

1. Check the troubleshooting section above
2. Review Docker logs: `docker-compose logs <service-name>`
3. Check service health: `docker-compose ps`
4. Verify ports are free: `./check-ports.sh`
5. Contact the development team

---

**Last Updated:** 2026-06-12  
**Status:** Production-Ready POC  
**Test Coverage:** 62 unit tests passing

---

**Questions or Issues?** Check the troubleshooting section above or contact the development team.
