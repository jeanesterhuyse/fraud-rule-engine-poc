# Fraud Rule Engine API

Backend service for the Fraud Rule Engine POC.

## Technology Stack

- **Java:** 21
- **Framework:** Spring Boot 3.2.5
- **Database:** PostgreSQL 15
- **Messaging:** Apache Kafka
- **Security:** JWT (Spring Security)
- **Migrations:** Flyway
- **Build Tool:** Maven 3.9+
- **Testing:** JUnit 5, Testcontainers, AssertJ

## Prerequisites

- Java 21 or later
- Maven 3.9+
- PostgreSQL 15 (for local development)
- Apache Kafka (for local development)
- Docker (for Testcontainers)

## Quick Start

### 1. Start Infrastructure

```bash
# PostgreSQL
docker run -d --name fraud-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=fraud_rule_engine \
  -e POSTGRES_USER=fraud_user \
  -e POSTGRES_PASSWORD=fraud_pass \
  postgres:15-alpine

# Kafka + Zookeeper
docker run -d --name fraud-zookeeper \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:latest \
  -e ZOOKEEPER_CLIENT_PORT=2181

docker run -d --name fraud-kafka \
  -p 9092:9092 \
  --link fraud-zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=fraud-zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

### 2. Build and Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Or with profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API will start on http://localhost:8080

## Running Tests

```bash
# Unit tests only
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify

# Specific test
mvn test -Dtest=AmountThresholdRuleEvaluatorTest
```

## Configuration

This project has **THREE configuration profiles**:

1. **`local`** - Local dev with **real Capitec Kafka brokers** ✅ **RECOMMENDED**
2. **`docker`** - Everything in Docker Compose (fully isolated)
3. **`test`** - Unit/integration tests (H2 + mocked Kafka)

See **[CONFIGURATION.md](./CONFIGURATION.md)** for complete configuration guide.

### Quick Start - Local Profile

**Uses real Capitec Kafka brokers from fraud_config/dev branch:**

```bash
# 1. Start local PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=fraud_rule_engine_local \
  -e POSTGRES_USER=fraud_local \
  -e POSTGRES_PASSWORD=fraud_local_pass \
  postgres:15-alpine

# 2. Run application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Kafka brokers (from fraud_config):**
- b-2.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
- b-3.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098
- b-1.afs1905417999612nprms.k3723w.c2.kafka.af-south-1.amazonaws.com:9098

### Quick Start - Docker Profile

**Everything in Docker (no AWS access needed):**

```bash
docker-compose up --build
```

### Environment Variable Overrides

```bash
export DB_USER=fraud_local
export DB_PASSWORD=fraud_local_pass
export KAFKA_BROKERS=localhost:9092  # Override to use local Kafka
export JWT_SECRET=YourSecretKeyHere
```

## API Endpoints

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# Response: {"token":"eyJhbG...","username":"test","roles":["ROLE_raas_consumer"],"expiresAt":"..."}
```

### Rules API

```bash
# Get all rules
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/rules

# Get rule by ID
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/rules/1

# Create rule
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "High Value Transaction",
    "description": "Triggers on transactions over 100,000",
    "ruleType": "AMOUNT_THRESHOLD",
    "enabled": true,
    "priority": 150,
    "thresholdAmount": 100000
  }'

# Enable rule
curl -X PATCH http://localhost:8080/api/v1/rules/1/enable \
  -H "Authorization: Bearer <token>"

# Disable rule
curl -X PATCH http://localhost:8080/api/v1/rules/1/disable \
  -H "Authorization: Bearer <token>"

# Delete rule
curl -X DELETE http://localhost:8080/api/v1/rules/1 \
  -H "Authorization: Bearer <token>"
```

### Triggered Transactions API

```bash
# Get all triggered transactions
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/triggered-transactions

# Filter by customer
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/triggered-transactions?customerId=CUST-001"

# Search
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/triggered-transactions/search?query=TXN-001"

# Recent (24 hours)
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/triggered-transactions/recent"
```

### Dashboard API

```bash
# Summary statistics
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/dashboard/summary

# Top triggered rules
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/dashboard/top-triggered-rules?days=7&limit=10"

# Customer risk leaderboard
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/dashboard/customer-risk?days=7&limit=10"

# Rule type distribution
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/dashboard/rule-type-distribution

# Trends
curl -H "Authorization: Bearer <token>" \
  "http://localhost:8080/api/v1/dashboard/trends?hours=24"
```

### Health Checks

```bash
# Health
curl http://localhost:8080/actuator/health

# Readiness
curl http://localhost:8080/actuator/health/readiness

# Liveness
curl http://localhost:8080/actuator/health/liveness
```

## Project Structure

```
src/main/java/com/fraud/ruleengine/
├── config/              # Configuration classes
│   ├── KafkaConfig.java
│   └── SecurityConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   ├── RuleController.java
│   ├── TriggeredTransactionController.java
│   └── DashboardController.java
├── domain/              # Domain model
│   ├── entity/          # JPA entities
│   ├── enums/           # Enums
│   └── model/           # DTOs/records
├── repository/          # Spring Data repositories
├── service/             # Business logic
│   ├── rule/            # Rule engine
│   │   └── strategy/    # Rule evaluators
│   └── ...
├── security/            # Security components
└── kafka/               # Kafka consumers/producers
```

## Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`:

- `V1__create_rules_table.sql` - Rules table
- `V2__create_triggered_transactions_table.sql` - Triggered transactions table
- `V3__insert_seed_rules.sql` - Seed data (8 demonstration rules)

### Running Migrations Manually

```bash
mvn flyway:migrate

# Clean database (WARNING: destroys all data!)
mvn flyway:clean

# Info
mvn flyway:info
```

## Rule Types

The system supports 7 rule types:

1. **AMOUNT_THRESHOLD** - Single transaction exceeds amount
2. **VELOCITY** - Multiple transactions in time window
3. **GEOGRAPHIC_ANOMALY** - Transaction from high-risk country
4. **MERCHANT_RISK** - Transaction at high-risk merchant
5. **AMOUNT_RANGE** - Transaction within suspicious range
6. **RAPID_FIRE** - Very short time between transactions
7. **DORMANT_ACCOUNT** - First transaction after long inactivity

## Adding a New Rule Type

1. Add enum value to `RuleType.java`
2. Add columns to rules table (if needed): `src/main/resources/db/migration/V4__add_new_rule_columns.sql`
3. Create new evaluator: `src/main/java/com/fraud/ruleengine/service/rule/strategy/NewRuleEvaluator.java`

```java
@Component
public class NewRuleEvaluator implements RuleEvaluationStrategy {
    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.NEW_TYPE;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        // Evaluation logic
    }
}
```

4. No changes to orchestrator, controllers, or Kafka consumer needed!

## Mock Transaction Producer

The mock producer generates test transactions automatically:

**Configuration:**
- Enable/disable: `app.mock-producer.enabled=true`
- Interval: `app.mock-producer.interval-ms=10000` (10 seconds)

**Transaction Mix:**
- 10% - Large transactions (trigger AMOUNT_THRESHOLD)
- 5% - High-risk country (trigger GEOGRAPHIC_ANOMALY)
- 5% - Gambling (trigger MERCHANT_RISK)
- 3% - Structuring amounts (trigger AMOUNT_RANGE)
- 2% - Crypto exchange (trigger MERCHANT_RISK)
- 75% - Normal transactions (should not trigger)

## Troubleshooting

### Database Connection Failed

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U fraud_user -d fraud_rule_engine

# View logs
docker logs fraud-postgres
```

### Kafka Connection Failed

```bash
# Check Kafka is running
docker ps | grep kafka

# List topics
docker exec -it fraud-kafka kafka-topics --list --bootstrap-server localhost:9092

# View logs
docker logs fraud-kafka
```

### Tests Failing

```bash
# Ensure Docker is running (for Testcontainers)
docker ps

# Clean and rebuild
mvn clean verify

# Run with debug logging
mvn test -X
```

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Performance

### Optimizations in Place

- **Database indexing** on all query fields
- **JPA lazy loading** for relationships
- **Connection pooling** (HikariCP)
- **Pagination** on all list endpoints
- **@Transactional(readOnly = true)** for read operations

### Production Recommendations

- **Cache rules** in Redis (currently loaded from DB each transaction)
- **Async processing** with virtual threads (Java 21)
- **Horizontal scaling** with multiple API instances
- **Database read replicas** for query load
- **Kafka partitioning** by customer ID

## Security

### POC Security (Current)

- Hardcoded test user (username: `test`, password: `test`)
- JWT secret in application.yml
- Single role: `raas_consumer`
- No token revocation

### Production Security Recommendations

- OAuth2/OIDC integration (Keycloak, Auth0, Azure AD)
- Refresh tokens
- Token blacklist (Redis)
- Granular role-based permissions
- API rate limiting
- Audit logging

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/health/readiness` - Kubernetes readiness probe
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Logging

Logs are written to console with configurable levels in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.fraud.ruleengine: DEBUG
```

## License

POC for demonstration purposes.
