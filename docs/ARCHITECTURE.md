# AI Context - Fraud Rule Engine POC

**Last Updated:** 2026-06-06  
**Status:** Implementation In Progress

---

## Project Purpose

This is a proof-of-concept fraud detection system that demonstrates a production-quality, extensible rule engine architecture. The system consumes financial transactions from Kafka, evaluates them against configurable fraud rules stored in PostgreSQL, and persists only transactions that trigger rules for investigation.

**Key Goal:** Demonstrate that fraud rule configuration can be changed without code deployments through a flexible, database-driven architecture.

---

## Architecture Overview

```
┌─────────────────┐
│  Mock Producer  │ (Generates test transactions)
└────────┬────────┘
         │
         ▼
    ┌────────┐
    │ Kafka  │ Topic: fraud-transactions-jeanTest
    └───┬────┘ DLQ: fraud-transactions-dlq-jeanTest
        │
        ▼
┌───────────────────────────────────────────┐
│     Fraud Rule Engine API (Java)          │
│                                            │
│  ┌──────────────────────────────────┐    │
│  │   Transaction Consumer           │    │
│  └──────────┬───────────────────────┘    │
│             │                             │
│             ▼                             │
│  ┌──────────────────────────────────┐    │
│  │   Rule Evaluator Orchestrator    │    │
│  │   (Strategy Pattern)             │    │
│  └──────────┬───────────────────────┘    │
│             │                             │
│             ▼                             │
│  ┌──────────────────────────────────┐    │
│  │   Rule Evaluation Strategies     │    │
│  │   (12 types - async processing)  │    │
│  │   - CustomerBlocklist (100)      │    │
│  │   - MerchantBlocklist (95)       │    │
│  │   - AmountThreshold (50-100)     │    │
│  │   - GeographicAnomaly (75)       │    │
│  │   - MerchantRisk (65)            │    │
│  │   - AmountRange (70)             │    │
│  │   - TimeOfDayAnomaly (60)        │    │
│  │   - RoundAmount (55-65)          │    │
│  │   - CnpHighRisk (60-75)          │    │
│  │   - CurrencyMismatch (55)        │    │
│  │   - CrossBorderHighRisk (90)     │    │
│  │   - LargeWithdrawal (50-80)      │    │
│  └──────────┬───────────────────────┘    │
│             │                             │
│             ▼                             │
│  ┌──────────────────────────────────┐    │
│  │   Persist Triggered Txns         │    │
│  └──────────┬───────────────────────┘    │
│             │                             │
│             ▼                             │
│  ┌──────────────────────────────────┐    │
│  │   REST API Controllers           │    │
│  │   - Rules CRUD                   │    │
│  │   - Triggered Transactions       │    │
│  │   - Dashboard Metrics            │    │
│  │   - Authentication (JWT)         │    │
│  └──────────────────────────────────┘    │
└───────────────┬───────────────────────────┘
                │
                ▼
        ┌───────────────┐
        │  PostgreSQL   │
        │  - rules      │
        │  - triggered_ │
        │    transactions│
        │  - blocked_   │
        │    customers  │
        │  - blocked_   │
        │    merchants  │
        └───────────────┘
                │
                │ REST API
                ▼
┌───────────────────────────────────────────┐
│     Fraud Rule Engine UI (Next.js)        │
│                                            │
│  - Dashboard (metrics & charts)           │
│  - Rule Management (CRUD)                 │
│  - Triggered Transactions (search/filter) │
│  - Blocklist Management (CRUD)            │
│  - Authentication (Keycloak OAuth2)       │
└───────────────────────────────────────────┘
```

---

## Technology Stack

### Backend (fraud-rule-engine-api)
- **Language:** Java 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL 15
- **Messaging:** Apache Kafka
- **Persistence:** Spring Data JPA
- **Migrations:** Flyway
- **Security:** Spring Security + Keycloak OAuth2/OIDC
- **Async Processing:** Custom ThreadPoolExecutor (10 threads)
- **Build Tool:** Maven
- **Testing:** JUnit 5, Testcontainers, Mockito (54 unit + 8 integration tests)

### Frontend (fraud-rule-engine-ui)
- **Framework:** Next.js 14 (App Router)
- **Language:** TypeScript
- **Styling:** Tailwind CSS + Capitec branding
- **Authentication:** Keycloak OAuth2/OIDC integration
- **Data Fetching:** React Query (TanStack Query)
- **HTTP Client:** Axios
- **Forms:** React Hook Form + Zod validation
- **Charts:** Recharts
- **Component Library:** Radix UI / shadcn/ui

### Infrastructure
- **Containerization:** Docker + Docker Compose
- **Database:** PostgreSQL 15 (Docker) - auto-creates Keycloak DB
- **Message Queue:** Kafka + Zookeeper (Docker)
- **Authentication:** Keycloak 24.0 (Docker)
- **Observability:** Grafana + Loki + Promtail (Docker)
- **Local Development:** All services in Docker Compose with automated setup

---

## Database Design

### Tables

#### `rules`
Stores fraud detection rules with typed configuration parameters.

**Key Columns:**
- `id` - Primary key
- `name`, `description` - Human-readable rule information
- `rule_type` - Enum: VELOCITY, AMOUNT_THRESHOLD, GEOGRAPHIC_ANOMALY, etc.
- `enabled` - Boolean flag to activate/deactivate rules
- `priority` - Integer (0-1000) determining evaluation order
- `threshold_amount`, `threshold_count`, `time_window_minutes` - Rule-specific parameters
- `merchant_category`, `country_code`, `min_amount`, `max_amount` - Additional filters
- Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`

**Why relational columns?** Enables querying, filtering, and reporting. No JSON blobs.

#### `triggered_transactions`
Stores transactions that matched one or more rules.

**Key Columns:**
- `id` - Primary key
- `rule_id` - Foreign key to rules (CASCADE delete)
- Transaction data: `transaction_id`, `account_id`, `customer_id`, `amount`, `currency`
- Transaction details: `merchant_name`, `merchant_category`, `transaction_type`, `country_code`
- Device info: `device_id`, `ip_address`, `card_last_four`
- Match data: `match_reason`, `rule_name`, `rule_type`, `triggered_at`, `risk_score`

**Design Decision:** One record per rule match. If a transaction triggers 3 rules, 3 records are created.

**Indexes:** Extensive indexing on `rule_id`, `customer_id`, `account_id`, `transaction_id`, `triggered_at`, `rule_type` for query performance.

#### `blocked_customers`
Stores customers on the blocklist for instant blocking (risk score 100).

**Key Columns:**
- `id` - Primary key
- `customer_id` - Unique customer identifier
- `reason` - Why this customer was blocked
- `blocked_by` - Who added them to the blocklist
- `blocked_at` - Timestamp when blocked
- `is_active` - Boolean flag to enable/disable block

**Index:** Unique index on `customer_id` for instant lookups.

#### `blocked_merchants`
Stores merchants on the blocklist for instant blocking (risk score 95).

**Key Columns:**
- `id` - Primary key
- `merchant_name` - Merchant name (case-insensitive matching)
- `reason` - Why this merchant was blocked
- `blocked_by` - Who added them to the blocklist
- `blocked_at` - Timestamp when blocked
- `is_active` - Boolean flag to enable/disable block

**Index:** Index on `merchant_name` for fast lookups.

---

## Kafka Topics

### Primary Topic: `fraud-transactions-jeanTest`
- Receives all transaction events from mock producer
- Consumed by rule engine
- Single consumer group

### Dead Letter Queue: `fraud-transactions-dlq-jeanTest`
- Failed messages after retry attempts
- Preserves messages for analysis
- Not automatically reprocessed

**Auto-Creation:** Topics are automatically created if they don't exist (dev setting).

---

## Security Approach

### Authentication
- **Method:** JWT (JSON Web Tokens)
- **Hardcoded Credentials (POC only):**
  - Username: `test`
  - Password: `test`
  - Role: `raas_consumer`
- **Token Expiration:** 24 hours
- **Storage:** Frontend stores JWT in localStorage (acceptable for POC)

### Authorization
- **All API endpoints** (except `/api/v1/auth/login`) require authentication
- **Role-based access:** All authenticated users have `raas_consumer` role
- **CORS:** Configured to allow requests from `http://localhost:3000`

### Production Considerations
- Replace hardcoded credentials with OAuth2/OIDC provider
- Use refresh tokens
- Store tokens in HTTP-only secure cookies
- Implement granular role-based permissions

---

## Key Implementation Decisions

### 1. Only Triggered Transactions Persisted
**Decision:** Do not persist all transactions to PostgreSQL. Only persist transactions that trigger rules.

**Rationale:**
- Kafka is the source of truth for all transactions
- PostgreSQL stores only actionable fraud detections
- Reduces database size and query complexity
- Aligns with "fraud detection" focus vs "transaction ledger"

**Trade-off:** Cannot query historical non-flagged transactions from database.

---

### 2. Strategy Pattern for Rule Engine
**Decision:** Use Strategy Pattern with Spring auto-discovery for rule evaluators.

**Implementation:**
```java
public interface RuleEvaluationStrategy {
    boolean supports(RuleType ruleType);
    Optional<RuleMatch> evaluate(Transaction transaction, Rule rule);
}

@Component
public class AmountThresholdRuleEvaluator implements RuleEvaluationStrategy { ... }
```

**Benefits:**
- New rule types require only new `@Component` class
- No orchestrator changes needed
- Spring auto-discovers implementations
- Testable in isolation

---

### 3. Relational Storage (Not JSON)
**Decision:** Store rule configuration in typed columns, not JSON blobs.

**Why:**
- **Queryable:** `SELECT * FROM rules WHERE threshold_amount > 10000`
- **Type-safe:** Database enforces data types
- **Indexed:** Performance optimization
- **Validated:** Database constraints ensure integrity
- **Future-proof:** Easy to add columns via migrations

**Trade-off:** Less flexible for completely arbitrary rule parameters. Mitigated by nullable columns.

---

### 4. Evaluation of All Rules
**Decision:** Always evaluate all active rules against every transaction (no short-circuit).

**Why:**
- Comprehensive fraud detection
- A transaction may trigger multiple rules (different fraud types)
- Priority determines evaluation order, not whether to evaluate

**Trade-off:** Slightly higher latency. Acceptable for POC volume.

---

### 5. Asynchronous Rule Evaluation
**Decision:** Rule evaluation happens asynchronously using a custom ThreadPoolExecutor.

**Implementation:**
- Custom thread pool with 10 threads (`fraud-async-1` through `fraud-async-10`)
- Configured in `AsyncConfig.java` with `@EnableAsync`
- Rule evaluation service methods annotated with `@Async`

**Why:**
- Improved throughput for high-volume transaction processing
- Non-blocking Kafka consumer
- Scalable architecture for production deployment
- Leverages Java 21's virtual thread capabilities

**Trade-off:** Added complexity in error handling and monitoring. Mitigated by comprehensive logging.

---

### 6. Denormalized Rule Data in triggered_transactions
**Decision:** Store `rule_name` and `rule_type` in `triggered_transactions` even though `rule_id` FK exists.

**Why:**
- Historical accuracy: If rule is renamed/deleted, historical records remain accurate
- Query performance: No JOIN needed for most queries
- Dashboard queries are faster

---

## Key Constraints

1. **No shared enterprise databases** - All data local to this POC
2. **Mock data only** - No real transaction integration
3. **Single instance** - No horizontal scaling
4. **Hardcoded auth** - test/test credentials only
5. **Local only** - Not production-ready
6. **No real-time UI** - Dashboard requires manual refresh
7. **No data archival** - triggered_transactions grows indefinitely

---

## Extension Points

### Adding a New Rule Type

**Example:** Add a `TIME_OF_DAY` rule that flags transactions during unusual hours.

**Steps:**

1. Add enum value:
```java
public enum RuleType {
    // existing...
    TIME_OF_DAY  // NEW
}
```

2. Add columns to `rules` table (if needed):
```sql
ALTER TABLE rules 
ADD COLUMN suspicious_start_hour INTEGER,
ADD COLUMN suspicious_end_hour INTEGER;
```

3. Create evaluator:
```java
@Component
public class TimeOfDayRuleEvaluator implements RuleEvaluationStrategy {
    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.TIME_OF_DAY;
    }
    
    @Override
    public Optional<RuleMatch> evaluate(Transaction txn, Rule rule) {
        // Implementation
    }
}
```

4. **No other changes needed** - orchestrator auto-discovers the new strategy.

---

## Future Enhancements

- Machine learning-based risk scoring
- Real-time alerting (email, SMS, Slack)
- Case management and investigation workflow
- Integration with real transaction streams
- Historical transaction replay
- A/B testing of rule configurations
- Performance optimization (rule caching, async processing)
- Multi-tenancy support
- Data archival and partitioning
- Production-ready security (OAuth2, refresh tokens)

---

## References

- [SCHEMA_REVIEW.md](./SCHEMA_REVIEW.md) - Complete database and API design
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current implementation status
- [BACKLOG.md](./BACKLOG.md) - Prioritized feature backlog
- [docs/adr/](./docs/adr/) - Architecture Decision Records
