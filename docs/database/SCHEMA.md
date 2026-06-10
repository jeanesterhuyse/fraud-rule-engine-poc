# Database Schema

Complete database schema documentation for the Fraud Rule Engine.

**Schema Version:** v5  
**Migration Tool:** Flyway  
**Database:** PostgreSQL 15+

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

**Rule Types:**
- `AMOUNT_THRESHOLD`
- `VELOCITY`
- `GEOGRAPHIC_ANOMALY`
- `MERCHANT_RISK`
- `AMOUNT_RANGE`
- `RAPID_FIRE`
- `DORMANT_ACCOUNT`

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

## Migration History

### V1: Create Rules Table
- Initial rules table with typed columns
- Indexes for performance
- Audit field support

### V2: Create Triggered Transactions Table
- Transaction storage with denormalized rule info
- Comprehensive indexes
- Foreign key to rules

### V3: Insert Seed Rules
- Sample rules for testing
- Various rule types demonstrated

### V4: Make rule_id Nullable (June 9, 2026)
**Purpose:** Preserve audit trail when rules are deleted

```sql
ALTER TABLE triggered_transactions
    ALTER COLUMN rule_id DROP NOT NULL;
```

**Impact:** Transactions can exist without a rule reference

### V5: Remove Cascade Delete (June 9, 2026)
**Purpose:** Prevent transaction deletion when rules are deleted

```sql
ALTER TABLE triggered_transactions
    DROP CONSTRAINT IF EXISTS fk_triggered_rule;

ALTER TABLE triggered_transactions
    ADD CONSTRAINT fk_triggered_rule
    FOREIGN KEY (rule_id) REFERENCES rules(id)
    ON DELETE SET NULL;
```

**Impact:** When a rule is deleted, `rule_id` becomes NULL (not cascade delete)

---

## Rule Deletion Behavior

### Before (v3)
- Deleting a rule **deleted all triggered transactions** (CASCADE)
- Loss of audit trail
- Compliance issues

### After (v4 + v5)
- Deleting a rule **preserves all transactions**
- `rule_id` becomes NULL
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
