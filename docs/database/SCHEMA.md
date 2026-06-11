# Database Schema

Complete database schema documentation for the Fraud Rule Engine.

**Schema Version:** v4 (with blocklists)  
**Migration Tool:** Flyway  
**Database:** PostgreSQL 15+  
**Last Updated:** June 11, 2026

---

## Overview

The database uses a **relational model** with typed columns (not JSON) for:
- Query performance and indexing
- Type safety and validation
- Clear data contracts
- SQL-native operations

---

## Tables

### `rules`

Stores fraud detection rule configurations.

```sql
CREATE TABLE rules (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    rule_type               VARCHAR(50) NOT NULL,
    enabled                 BOOLEAN NOT NULL DEFAULT TRUE,
    priority                INTEGER NOT NULL DEFAULT 100,
    
    -- Rule-specific parameters (nullable, used by specific rule types)
    threshold_amount        DECIMAL(19,2),
    threshold_count         INTEGER,
    time_window_minutes     INTEGER,
    merchant_category       VARCHAR(100),
    country_code            CHAR(3),
    min_amount              DECIMAL(19,2),
    max_amount              DECIMAL(19,2),
    
    -- Audit fields
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100)
);
```

**Indexes:**
- `idx_rules_enabled` ON (enabled)
- `idx_rules_rule_type` ON (rule_type)
- `idx_rules_priority` ON (priority)

**Rule Types (12 total):**
- `CUSTOMER_BLOCKLIST` - Instant block (Risk: 100)
- `MERCHANT_BLOCKLIST` - Instant block (Risk: 95)
- `AMOUNT_THRESHOLD` - Large transactions (Risk: 50-100)
- `GEOGRAPHIC_ANOMALY` - High-risk countries (Risk: 75)
- `MERCHANT_RISK` - High-risk merchants (Risk: 65)
- `AMOUNT_RANGE` - Structuring detection (Risk: 70)
- `TIME_OF_DAY_ANOMALY` - Unusual hours 2-5 AM (Risk: 60)
- `ROUND_AMOUNT` - Card testing (Risk: 55-65)
- `CNP_HIGH_RISK` - Card-not-present fraud (Risk: 60-75)
- `CURRENCY_MISMATCH` - Foreign currency anomalies (Risk: 55)
- `CROSS_BORDER_HIGH_RISK` - Cross-border high-risk (Risk: 90)
- `LARGE_WITHDRAWAL` - Large ATM withdrawals (Risk: 50-80)

---

### `triggered_transactions`

Stores transactions that matched fraud detection rules.

```sql
CREATE TABLE triggered_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    rule_id                 BIGINT,  -- Nullable (v4: audit trail preservation)
    
    -- Denormalized rule info (for audit/historical purposes)
    rule_name               VARCHAR(255) NOT NULL,
    rule_type               VARCHAR(50) NOT NULL,
    
    -- Transaction core info
    transaction_id          VARCHAR(100) NOT NULL,
    account_id              VARCHAR(100) NOT NULL,
    customer_id             VARCHAR(100) NOT NULL,
    amount                  DECIMAL(19,2) NOT NULL,
    currency                CHAR(3) NOT NULL,
    
    -- Transaction details
    merchant_name           VARCHAR(255),
    merchant_category       VARCHAR(100),
    transaction_type        VARCHAR(50) NOT NULL,
    transaction_timestamp   TIMESTAMP NOT NULL,
    country_code            CHAR(3),
    device_id               VARCHAR(100),
    ip_address              VARCHAR(45),
    card_last_four          CHAR(4),
    
    -- Detection info
    match_reason            TEXT NOT NULL,
    risk_score              INTEGER,
    triggered_at            TIMESTAMP NOT NULL,
    
    FOREIGN KEY (rule_id) REFERENCES rules(id) ON DELETE SET NULL
);
```

**Indexes:**
- `idx_triggered_transaction_id` ON (transaction_id)
- `idx_triggered_account_id` ON (account_id)
- `idx_triggered_customer_id` ON (customer_id)
- `idx_triggered_rule_id` ON (rule_id)
- `idx_triggered_timestamp` ON (transaction_timestamp DESC)
- `idx_triggered_at` ON (triggered_at DESC)
- `idx_triggered_rule_id_at` ON (rule_id, triggered_at DESC)

---

### `blocked_customers`

Stores customers on the blocklist for instant blocking (risk score 100).

```sql
CREATE TABLE blocked_customers (
    id                      BIGSERIAL PRIMARY KEY,
    customer_id             VARCHAR(100) NOT NULL UNIQUE,
    reason                  TEXT NOT NULL,
    blocked_by              VARCHAR(100) NOT NULL,
    blocked_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Indexes:**
- `idx_blocked_customers_customer_id` ON (customer_id) - Unique index for instant lookups
- `idx_blocked_customers_active` ON (is_active)

**Usage:**
- Customer blocklist rule (`CUSTOMER_BLOCKLIST`) queries this table
- Any transaction from a blocked customer receives risk score 100
- Can be temporarily disabled by setting `is_active = false`

---

### `blocked_merchants`

Stores merchants on the blocklist for instant blocking (risk score 95).

```sql
CREATE TABLE blocked_merchants (
    id                      BIGSERIAL PRIMARY KEY,
    merchant_name           VARCHAR(255) NOT NULL,
    reason                  TEXT NOT NULL,
    blocked_by              VARCHAR(100) NOT NULL,
    blocked_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Indexes:**
- `idx_blocked_merchants_name` ON (merchant_name) - For fast lookups
- `idx_blocked_merchants_active` ON (is_active)

**Usage:**
- Merchant blocklist rule (`MERCHANT_BLOCKLIST`) queries this table
- Case-insensitive matching on `merchant_name`
- Any transaction at a blocked merchant receives risk score 95
- Can be temporarily disabled by setting `is_active = false`

---

## Migration History

### V1: Create Rules Table
- Initial rules table with typed columns
- Indexes for performance
- Audit field support

### V2: Create Triggered Transactions Table
- Transaction storage with denormalized rule info
- Comprehensive indexes
- Foreign key to rules

### V3: Create Blocklist Tables (June 11, 2026)
**Purpose:** Customer and merchant blocklists for instant blocking

```sql
CREATE TABLE blocked_customers (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL UNIQUE,
    reason TEXT NOT NULL,
    blocked_by VARCHAR(100) NOT NULL,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE blocked_merchants (
    id BIGSERIAL PRIMARY KEY,
    merchant_name VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    blocked_by VARCHAR(100) NOT NULL,
    blocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

**Impact:** 
- New rule types: `CUSTOMER_BLOCKLIST` and `MERCHANT_BLOCKLIST`
- Instant blocking with risk scores 100 and 95 respectively
- Fast lookups with unique indexes

### V4: Insert Seed Rules (June 11, 2026)
**Purpose:** Comprehensive seed data with all 12 rule types

- 16 demonstration rules covering all 12 rule types
- Test data for blocklists (2 customers, 2 merchants)
- Multiple instances of common rule types with different parameters

---

## Rule Deletion Behavior

- Deleting a rule **preserves all transactions**
- `rule_id` becomes NULL (ON DELETE SET NULL)
- Denormalized fields (`rule_name`, `rule_type`) maintain context
- Complete audit trail preserved

**Example:**
```sql
-- Delete a rule
DELETE FROM rules WHERE id = 123;

-- Transactions still exist
SELECT * FROM triggered_transactions WHERE rule_name = 'Large Transaction Alert';
-- Returns all transactions, with rule_id = NULL
```

## Blocklist Management

### Adding to Blocklist
```sql
-- Block a customer
INSERT INTO blocked_customers (customer_id, reason, blocked_by)
VALUES ('CUST-001', 'Suspected fraud', 'john.smith');

-- Block a merchant
INSERT INTO blocked_merchants (merchant_name, reason, blocked_by)
VALUES ('Suspicious Shop', 'High fraud rate', 'john.smith');
```

### Removing from Blocklist
```sql
-- Soft delete (recommended)
UPDATE blocked_customers SET is_active = FALSE WHERE customer_id = 'CUST-001';

-- Hard delete (removes audit trail)
DELETE FROM blocked_customers WHERE customer_id = 'CUST-001';
```

### Checking Blocklist Status
```sql
-- Check if customer is blocked
SELECT * FROM blocked_customers 
WHERE customer_id = 'CUST-001' AND is_active = TRUE;

-- Check if merchant is blocked (case-insensitive)
SELECT * FROM blocked_merchants 
WHERE LOWER(merchant_name) = LOWER('Suspicious Shop') AND is_active = TRUE;
```

---

## Query Patterns

### Get Active Rules by Priority
```sql
SELECT * FROM rules 
WHERE enabled = TRUE 
ORDER BY priority DESC, created_at ASC;
```

### Get Triggered Transactions for a Date Range
```sql
SELECT * FROM triggered_transactions
WHERE triggered_at BETWEEN '2026-01-01' AND '2026-01-31'
ORDER BY triggered_at DESC;
```

### Get Triggers per Rule (Last 24h)
```sql
SELECT rule_id, rule_name, COUNT(*) as trigger_count
FROM triggered_transactions
WHERE triggered_at > NOW() - INTERVAL '24 hours'
GROUP BY rule_id, rule_name
ORDER BY trigger_count DESC;
```

### Get Orphaned Transactions (Deleted Rules)
```sql
SELECT * FROM triggered_transactions
WHERE rule_id IS NULL
ORDER BY triggered_at DESC;
```

---

## Performance Considerations

### Indexes
- All foreign keys indexed
- Timestamp columns indexed (DESC for recent-first queries)
- Composite index on (rule_id, triggered_at) for rule-specific time queries

### Partitioning (Future)
Consider partitioning `triggered_transactions` by month:
```sql
-- Example for future implementation
CREATE TABLE triggered_transactions_2026_01 
PARTITION OF triggered_transactions
FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
```

### Archival Strategy
- Move old transactions (>1 year) to archive table
- Keep indexes lean
- Maintain audit compliance

---

## Data Integrity

### Constraints
- NOT NULL on critical fields
- CHECK constraints on amounts (positive values)
- UNIQUE on rule names (optional, not currently enforced)
- Foreign key with SET NULL (preserves audit trail)

### Validation
- Application-level validation via JPA annotations
- Database-level constraints as backup
- Type safety via ENUM types (rule_type)

---

## Backup & Recovery

**Backup Strategy:**
```bash
# Full database backup
docker exec fraud-postgres pg_dump -U fraud_user fraud_rule_engine > backup.sql

# Restore
docker exec -i fraud-postgres psql -U fraud_user fraud_rule_engine < backup.sql
```

**Point-in-Time Recovery:**
- Enable WAL archiving for production
- Regular automated backups
- Test restoration procedures

---

**Maintained by:** Database Team  
**Last Schema Change:** June 9, 2026 (v5)
