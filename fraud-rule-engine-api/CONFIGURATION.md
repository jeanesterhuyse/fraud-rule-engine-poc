# Configuration Guide

This document explains the different configuration profiles and how to use them.

## Configuration Profiles

### 1. Default Profile (application.yml)
**Purpose:** Base configuration shared across all environments

**Contains:**
- JPA/Hibernate settings
- Flyway migration settings
- Kafka consumer/producer serializers
- Logging patterns
- Actuator endpoints
- CORS settings

**Does NOT contain:**
- Database connection details
- Kafka broker addresses
- Environment-specific settings

---

### 2. Local Profile (application-local.yml)
**Purpose:** Local development using **REAL Capitec Kafka brokers**

**Run with:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Configuration:**
- **Database:** Local PostgreSQL (`fraud_rule_engine_local`)
- **Kafka:** Real Capitec AWS MSK cluster brokers
  ```
  b-2.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
  b-3.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
  b-1.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
  ```
- **Topics:** `fraud-transactions-jeanTest`, `fraud-transactions-dlq-jeanTest`
- **Auto-create topics:** Disabled (don't create topics on production Kafka)
- **Mock producer:** Enabled (every 5 seconds)
- **Logging:** DEBUG level for fraud-rule-engine

**Prerequisites:**
1. Local PostgreSQL running:
```bash
docker run -d --name fraud-postgres-local \
  -p 5432:5432 \
  -e POSTGRES_DB=fraud_rule_engine_local \
  -e POSTGRES_USER=fraud_local \
  -e POSTGRES_PASSWORD=fraud_local_pass \
  postgres:15-alpine
```

2. **AWS credentials configured** (for Kafka IAM authentication):
```bash
# Check credentials are configured
aws sts get-caller-identity

# Your AWS credentials must have permissions for:
# - kafka:DescribeCluster
# - kafka:GetBootstrapBrokers
# - kafka-cluster:Connect
# - kafka-cluster:DescribeTopic
# - kafka-cluster:ReadData (consumer)
# - kafka-cluster:WriteData (producer)
```

3. VPN/network access to Capitec AWS resources

**Environment Variables (Optional):**
```bash
export DB_USER=fraud_local
export DB_PASSWORD=fraud_local_pass
export KAFKA_BROKERS=localhost:9092  # Override to use local Kafka instead
export JWT_SECRET=your-secret-key
```

---

### 3. Docker Profile (application-docker.yml)
**Purpose:** Running everything in Docker Compose locally

**Run with:**
```bash
docker-compose up
# OR
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

**Configuration:**
- **Database:** Docker PostgreSQL container (`postgres:5432`)
- **Kafka:** Docker Kafka container (`kafka:9092`)
- **Topics:** `fraud-transactions-jeanTest`, `fraud-transactions-dlq-jeanTest`
- **Auto-create topics:** Enabled (local Kafka)
- **Mock producer:** Enabled (every 10 seconds)
- **Logging:** DEBUG level

**Prerequisites:**
- Docker and Docker Compose installed
- Ports available: 5432 (PostgreSQL), 9092 (Kafka), 2181 (Zookeeper), 8080 (API)

**Use this when:**
- You want to run the complete stack locally
- You don't have access to AWS Kafka
- You want a fully isolated environment

---

### 4. Test Profile (application-test.yml)
**Purpose:** Unit and integration tests

**Automatically used by:** `mvn test`

**Configuration:**
- **Database:** H2 in-memory (PostgreSQL mode)
- **Kafka:** Disabled/mocked
- **Auto-create topics:** Enabled
- **Mock producer:** Disabled
- **Logging:** WARN level (quieter tests)

---

## Quick Reference

### Scenario 1: Local Dev with Real Kafka
```bash
# Start local PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=fraud_rule_engine_local \
  -e POSTGRES_USER=fraud_local \
  -e POSTGRES_PASSWORD=fraud_local_pass \
  postgres:15-alpine

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Scenario 2: Local Dev with Docker Kafka
```bash
# Override Kafka brokers
export KAFKA_BROKERS=localhost:9092

# Start local Kafka
docker-compose up kafka zookeeper postgres

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Scenario 3: Everything in Docker
```bash
# Start complete stack
docker-compose up --build

# Application runs with 'docker' profile automatically
```

### Scenario 4: Run Tests
```bash
# Unit tests only
mvn test

# Integration tests (need Docker for Testcontainers)
mvn verify
```

---

## Database Configuration

### Local PostgreSQL Setup

**Option 1: Docker**
```bash
docker run -d --name fraud-postgres-local \
  -p 5432:5432 \
  -e POSTGRES_DB=fraud_rule_engine_local \
  -e POSTGRES_USER=fraud_local \
  -e POSTGRES_PASSWORD=fraud_local_pass \
  postgres:15-alpine
```

**Option 2: Installed PostgreSQL**
```sql
CREATE DATABASE fraud_rule_engine_local;
CREATE USER fraud_local WITH PASSWORD 'fraud_local_pass';
GRANT ALL PRIVILEGES ON DATABASE fraud_rule_engine_local TO fraud_local;
```

**Connect to database:**
```bash
psql -h localhost -U fraud_local -d fraud_rule_engine_local
# Password: fraud_local_pass
```

---

## Kafka Configuration

### Real Capitec Kafka (Default in 'local' profile)

**Brokers:**
- b-2.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
- b-3.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
- b-1.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098

**Authentication:** AWS MSK IAM (Port 9098)
- **Protocol:** SASL_SSL
- **Mechanism:** AWS_MSK_IAM
- **Library:** `aws-msk-iam-auth` (included in pom.xml)
- **Credentials:** Uses AWS credentials from `~/.aws/credentials` or environment variables

**How it works:**
1. Application uses AWS SDK to get temporary credentials
2. IAM credentials are exchanged for Kafka authentication token
3. Token is used to authenticate with MSK brokers
4. **NO username/password or certificates needed!**
5. Authentication is handled automatically by the `aws-msk-iam-auth` library

**Topics:**
- Primary: `fraud-transactions-jeanTest`
- DLQ: `fraud-transactions-dlq-jeanTest`

**Important:**
- Auto-create is DISABLED for real Kafka
- Topics should be created manually or via Terraform
- Requires AWS credentials and network access
- Your IAM role/user must have Kafka permissions (see Prerequisites above)

### Local Docker Kafka (Override)

**Override Kafka brokers:**
```bash
export KAFKA_BROKERS=localhost:9092
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**OR use Docker profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## Security Configuration

### JWT Secret

**Default (DO NOT USE IN PRODUCTION):**
- Local: `LocalDevSecretKeyForJWTTokenGenerationDoNotUseInProduction`
- Docker: `DockerLocalSecretKeyForJWTTokenGeneration`

**Override with environment variable:**
```bash
export JWT_SECRET=your-secure-random-secret-key-here
```

**Generate secure secret:**
```bash
openssl rand -base64 64
```

### Test Credentials

**Username:** `test`  
**Password:** `test`  
**Role:** `raas_consumer`

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

---

## Troubleshooting

### Cannot connect to Kafka

**Symptom:** `Connection refused` or `Broker not available`

**Solutions:**
1. Check if using correct profile: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
2. Verify AWS credentials: `aws sts get-caller-identity`
3. Check VPN/network access to AWS
4. Override to local Kafka: `export KAFKA_BROKERS=localhost:9092`

### Cannot connect to PostgreSQL

**Symptom:** `Connection refused` to localhost:5432

**Solutions:**
1. Check PostgreSQL is running: `docker ps | grep postgres`
2. Check port is correct: `5432`
3. Verify credentials match configuration
4. Check database exists: `psql -h localhost -U fraud_local -l`

### Flyway migration fails

**Symptom:** `Validate failed: Migrations have failed validation`

**Solutions:**
1. Clean database: `mvn flyway:clean` (WARNING: deletes all data)
2. Re-run migrations: `mvn flyway:migrate`
3. Check migration files are not corrupted

### Tests fail

**Symptom:** `ApplicationContext failed to start`

**Solutions:**
1. Pure unit tests should always work: `mvn test -Dtest=AmountThresholdRuleEvaluatorTest`
2. Integration tests need Docker: Check Docker is running
3. Check test profile is active: `@ActiveProfiles("test")`

---

## Configuration Precedence

Spring Boot loads configurations in this order (later overrides earlier):

1. `application.yml` (base)
2. `application-{profile}.yml` (profile-specific)
3. Environment variables
4. Command-line arguments

**Example:**
```bash
# Uses: application.yml + application-local.yml + env vars + CLI args
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.arguments="--server.port=8081" \
  -DDB_USER=custom_user
```

---

## Recommendations

### For Daily Development
✅ Use **local** profile with real Kafka (if you have AWS access)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### For Isolated Testing
✅ Use **docker** profile for complete isolation
```bash
docker-compose up
```

### For CI/CD
✅ Use **test** profile (automatic in `mvn test`)

### For Demo
✅ Use **docker** profile for self-contained demo
```bash
docker-compose up --build
```
