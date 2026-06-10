-- V1: Create rules table
-- This table stores fraud detection rules with typed configuration parameters

CREATE TABLE rules (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL,
    description          TEXT,
    rule_type            VARCHAR(50) NOT NULL,
    enabled              BOOLEAN NOT NULL DEFAULT true,
    priority             INTEGER NOT NULL DEFAULT 100,

    -- Rule-specific thresholds and parameters
    -- These columns are nullable as they only apply to specific rule types
    threshold_amount     NUMERIC(19,2),
    threshold_count      INTEGER,
    time_window_minutes  INTEGER,
    merchant_category    VARCHAR(100),
    country_code         VARCHAR(3),
    min_amount           NUMERIC(19,2),
    max_amount           NUMERIC(19,2),

    -- Audit fields
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),

    -- Constraints
    CONSTRAINT chk_priority CHECK (priority >= 0 AND priority <= 1000),
    CONSTRAINT chk_threshold_count CHECK (threshold_count IS NULL OR threshold_count > 0),
    CONSTRAINT chk_time_window CHECK (time_window_minutes IS NULL OR time_window_minutes > 0),
    CONSTRAINT chk_amounts CHECK (
        (min_amount IS NULL OR max_amount IS NULL) OR
        (min_amount <= max_amount)
    )
);

-- Indexes for performance
CREATE INDEX idx_rules_enabled ON rules(enabled);
CREATE INDEX idx_rules_rule_type ON rules(rule_type);
CREATE INDEX idx_rules_priority ON rules(priority DESC);

-- Comments for documentation
COMMENT ON TABLE rules IS 'Stores fraud detection rules with typed configuration parameters';
COMMENT ON COLUMN rules.rule_type IS 'Type of rule: VELOCITY, AMOUNT_THRESHOLD, GEOGRAPHIC_ANOMALY, MERCHANT_RISK, AMOUNT_RANGE, RAPID_FIRE, DORMANT_ACCOUNT';
COMMENT ON COLUMN rules.enabled IS 'Whether the rule is active and should be evaluated';
COMMENT ON COLUMN rules.priority IS 'Evaluation priority (higher = evaluated first), range 0-1000';
COMMENT ON COLUMN rules.threshold_amount IS 'For AMOUNT_THRESHOLD rules: minimum amount to trigger';
COMMENT ON COLUMN rules.threshold_count IS 'For VELOCITY rules: number of transactions in time window';
COMMENT ON COLUMN rules.time_window_minutes IS 'For VELOCITY and RAPID_FIRE rules: time window in minutes';
COMMENT ON COLUMN rules.merchant_category IS 'For MERCHANT_RISK rules: high-risk merchant category';
COMMENT ON COLUMN rules.country_code IS 'For GEOGRAPHIC_ANOMALY rules: high-risk country code (ISO 3166-1 alpha-3)';
COMMENT ON COLUMN rules.min_amount IS 'For AMOUNT_RANGE rules: minimum amount in suspicious range';
COMMENT ON COLUMN rules.max_amount IS 'For AMOUNT_RANGE rules: maximum amount in suspicious range';
