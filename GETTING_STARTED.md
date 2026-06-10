# Getting Started - Fraud Rule Engine

**Complete setup guide for new developers and testers.**

---

## 🚀 Quick Start (5 Minutes)

### Prerequisites

- Docker Desktop (4GB+ RAM recommended)
- Node.js 18+ 
- Java 21 (for API development)

### One-Command Startup

```bash
# Clone or navigate to the repository
cd fraud-rule-engine-poc

# Start everything (automated setup)
./start-dev.sh
```

This script automatically:
- ✅ Starts all Docker services (API, Database, Kafka, Keycloak, Grafana)
- ✅ **Automated Keycloak database creation** (no manual steps required)
- ✅ Configures authentication realm and test users
- ✅ Waits for all services to be healthy
- ✅ Loads 16 fraud detection rules (12 rule types)
- ✅ Loads blocklist test data
- ✅ **Async processing** enabled with custom thread pool (fraud-async-1, fraud-async-2)

### Start the Frontend

```bash
cd fraud-rule-engine-ui
npm install  # First time only
npm run dev
```

### Access the Application

Open your browser and navigate to:
- **Login**: http://localhost:3000/login-keycloak
- Click "Sign In with Keycloak"
- Use: `john.smith` / `FraudDetect123!`

---

## 🔐 Test User Credentials

| Role | Username | Password | Permissions |
|------|----------|----------|-------------|
| **Fraud Analyst** | `john.smith` | `FraudDetect123!` | Full access - create/edit/delete rules |
| **Fraud Viewer** | `sarah.jones` | `ViewOnly123!` | Read-only access |
| **Admin** | `admin.user` | `Admin123!` | Full access |

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

## 🌐 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend UI** | http://localhost:3000 | See test users above |
| **Login Page** | http://localhost:3000/login-keycloak | - |
| **API** | http://localhost:8080 | Requires Bearer token |
| **API Health** | http://localhost:8080/actuator/health | - |
| **Grafana Logs** | http://localhost:3001 | Anonymous (Admin role) |
| **Keycloak Admin** | http://localhost:8180 | admin / admin |
| **Kafka UI** | http://localhost:8090 | - |
| **PostgreSQL** | localhost:5432 | fraud_user / fraud_pass |

---

## 🛠️ Development Workflow

### Start Backend Only

```bash
docker-compose up -d
```

### Start Frontend Only

```bash
cd fraud-rule-engine-ui
npm run dev
```

### Rebuild API After Code Changes

```bash
cd fraud-rule-engine-api
mvn clean package -DskipTests
docker-compose restart fraud-api
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker logs fraud-api -f
docker logs fraud-postgres -f
```

### Check Service Health

```bash
docker-compose ps
```

All services should show "healthy" status.

---

## 🗄️ Database Access

### Connect to PostgreSQL

```bash
docker exec -it fraud-postgres psql -U fraud_user -d fraud_rule_engine
```

### Useful Queries

```sql
-- View all rules
SELECT id, name, rule_type, enabled, priority FROM rules ORDER BY priority DESC;

-- View blocked customers
SELECT * FROM blocked_customers;

-- View triggered transactions
SELECT id, transaction_id, rule_name, risk_score FROM triggered_transactions ORDER BY triggered_at DESC LIMIT 10;
```

---

## 🔄 Reset Everything

### Soft Reset (Keep Data)

```bash
docker-compose restart
```

### Hard Reset (Fresh Start)

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

## ❌ Troubleshooting

### Keycloak Won't Start

**Symptom**: `FATAL: database "keycloak" does not exist`

**Solution**: The postgres-init script should create this automatically. If it doesn't:

```bash
docker exec fraud-postgres psql -U fraud_user -d postgres -c "CREATE DATABASE keycloak;"
docker exec fraud-postgres psql -U fraud_user -d keycloak -c "CREATE SCHEMA keycloak AUTHORIZATION fraud_user;"
docker restart fraud-keycloak
./setup-keycloak.sh
```

### API Won't Start

**Symptom**: API container keeps restarting

**Solution**: Check logs and ensure database is healthy

```bash
docker logs fraud-api --tail 50
docker exec fraud-postgres pg_isready -U fraud_user -d fraud_rule_engine
```

### Keycloak Realm Not Configured

**Symptom**: Can't login, realm not found

**Solution**: Run the setup script manually

```bash
./setup-keycloak.sh
```

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
```

### UI Shows TLS Certificate Warning

**Symptom**: Console shows `UNABLE_TO_GET_ISSUER_CERT_LOCALLY`

**Solution**: This is normal for local development with self-signed certificates. The app still works correctly.

---

## 📚 Additional Documentation

- [Architecture Overview](docs/ARCHITECTURE.md)
- [API Documentation](fraud-rule-engine-api/README.md)
- [Keycloak Setup Guide](docs/guides/KEYCLOAK_SETUP.md)
- [Database Schema](docs/database/SCHEMA.md)
- [Risk Score Calculation](docs/RISK_SCORE_CALCULATION.md)

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

---

## 💡 Tips for Testers

1. **Use Multiple Browser Sessions** - Test different user roles in different browsers (Chrome vs Firefox)
2. **Check Grafana** - Visit http://localhost:3001 to see real-time logs
3. **Monitor API Logs** - Run `docker logs fraud-api -f` to see transaction processing
4. **Test Edge Cases** - Try invalid inputs, missing fields, special characters
5. **Check Async Processing** - Logs will show `fraud-async-1`, `fraud-async-2` threads

---

**Questions or Issues?** Check the troubleshooting section above or contact the development team.
