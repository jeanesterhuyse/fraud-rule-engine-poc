-- V2: Create triggered_transactions table
-- This table stores transactions that matched one or more fraud rules

CREATE TABLE triggered_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    rule_id                 BIGINT NOT NULL,

    -- Transaction core information
    transaction_id          VARCHAR(100) NOT NULL,
    account_id              VARCHAR(100) NOT NULL,
    customer_id             VARCHAR(100) NOT NULL,
    amount                  NUMERIC(19,2) NOT NULL,
    currency                VARCHAR(3) NOT NULL,

    -- Transaction details
    merchant_name           VARCHAR(255),
    merchant_category       VARCHAR(100),
    transaction_type        VARCHAR(50) NOT NULL,
    transaction_timestamp   TIMESTAMP NOT NULL,
    country_code            VARCHAR(3),
    device_id               VARCHAR(100),
    ip_address              VARCHAR(45),
    card_last_four          VARCHAR(4),

    -- Rule match information
    match_reason            TEXT NOT NULL,
    rule_name               VARCHAR(255) NOT NULL,
    rule_type               VARCHAR(50) NOT NULL,
    triggered_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    risk_score              INTEGER,

    -- Foreign key constraint with CASCADE delete
    -- If a rule is deleted, all associated triggered transactions are deleted
    CONSTRAINT fk_triggered_rule FOREIGN KEY (rule_id)
        REFERENCES rules(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_risk_score CHECK (risk_score IS NULL OR (risk_score >= 0 AND risk_score <= 100)),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

-- Indexes for query performance
-- These support common query patterns: filtering by rule, customer, time, etc.
CREATE INDEX idx_triggered_rule_id ON triggered_transactions(rule_id);
CREATE INDEX idx_triggered_transaction_id ON triggered_transactions(transaction_id);
CREATE INDEX idx_triggered_customer_id ON triggered_transactions(customer_id);
CREATE INDEX idx_triggered_account_id ON triggered_transactions(account_id);
CREATE INDEX idx_triggered_at ON triggered_transactions(triggered_at DESC);
CREATE INDEX idx_triggered_rule_type ON triggered_transactions(rule_type);
CREATE INDEX idx_triggered_timestamp ON triggered_transactions(transaction_timestamp DESC);

-- Composite index for dashboard queries (most common: rule + time range)
CREATE INDEX idx_triggered_rule_id_at ON triggered_transactions(rule_id, triggered_at DESC);

-- Comments for documentation
COMMENT ON TABLE triggered_transactions IS 'Stores transactions that triggered fraud rules - one record per rule match';
COMMENT ON COLUMN triggered_transactions.rule_id IS 'Foreign key to rules table';
COMMENT ON COLUMN triggered_transactions.transaction_id IS 'Unique identifier from Kafka transaction (may appear multiple times if multiple rules triggered)';
COMMENT ON COLUMN triggered_transactions.match_reason IS 'Human-readable explanation of why this rule triggered for this transaction';
COMMENT ON COLUMN triggered_transactions.rule_name IS 'Denormalized rule name for historical accuracy (preserved even if rule is renamed)';
COMMENT ON COLUMN triggered_transactions.rule_type IS 'Denormalized rule type for query performance and historical accuracy';
COMMENT ON COLUMN triggered_transactions.triggered_at IS 'When the rule engine detected this match (not the transaction timestamp)';
COMMENT ON COLUMN triggered_transactions.risk_score IS 'Calculated risk score (0-100) for this specific rule match';
