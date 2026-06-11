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

    -- For TIME_OF_DAY_ANOMALY rule type
    start_hour           INTEGER CHECK (start_hour >= 0 AND start_hour <= 23),
    end_hour             INTEGER CHECK (end_hour >= 0 AND end_hour <= 23),

    -- For ROUND_AMOUNT rule type
    minimum_amount       NUMERIC(19,2),
    round_to_nearest     INTEGER CHECK (round_to_nearest IN (10, 50, 100, 500, 1000)),

    -- For cross-border rules (CROSS_BORDER_HIGH_RISK, CURRENCY_MISMATCH)
    customer_home_country VARCHAR(3),  -- ISO 3166-1 alpha-3
    customer_home_currency VARCHAR(3),  -- ISO 4217

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
    ),
    CONSTRAINT check_time_of_day_config
        CHECK (rule_type != 'TIME_OF_DAY_ANOMALY' OR (start_hour IS NOT NULL AND end_hour IS NOT NULL)),
    CONSTRAINT check_round_amount_config
        CHECK (rule_type != 'ROUND_AMOUNT' OR (minimum_amount IS NOT NULL AND round_to_nearest IS NOT NULL)),
    CONSTRAINT check_cross_border_config
        CHECK (rule_type != 'CROSS_BORDER_HIGH_RISK' OR customer_home_country IS NOT NULL),
    CONSTRAINT check_currency_mismatch_config
        CHECK (rule_type != 'CURRENCY_MISMATCH' OR (customer_home_country IS NOT NULL AND customer_home_currency IS NOT NULL)),
    CONSTRAINT check_large_withdrawal_config
        CHECK (rule_type != 'LARGE_WITHDRAWAL' OR threshold_amount IS NOT NULL)
);

-- Indexes for performance
CREATE INDEX idx_rules_enabled ON rules(enabled);
CREATE INDEX idx_rules_rule_type ON rules(rule_type);
CREATE INDEX idx_rules_priority ON rules(priority DESC);

-- Comments for documentation
COMMENT ON TABLE rules IS 'Stores fraud detection rules with typed configuration parameters';
COMMENT ON COLUMN rules.rule_type IS 'Type of rule: AMOUNT_THRESHOLD, GEOGRAPHIC_ANOMALY, MERCHANT_RISK, AMOUNT_RANGE, TIME_OF_DAY_ANOMALY, ROUND_AMOUNT, CUSTOMER_BLOCKLIST, MERCHANT_BLOCKLIST, CNP_HIGH_RISK, CURRENCY_MISMATCH, CROSS_BORDER_HIGH_RISK, LARGE_WITHDRAWAL';
COMMENT ON COLUMN rules.enabled IS 'Whether the rule is active and should be evaluated';
COMMENT ON COLUMN rules.priority IS 'Evaluation priority (higher = evaluated first), range 0-1000';
COMMENT ON COLUMN rules.threshold_amount IS 'For AMOUNT_THRESHOLD and LARGE_WITHDRAWAL rules: minimum amount to trigger';
COMMENT ON COLUMN rules.threshold_count IS 'For VELOCITY rules: number of transactions in time window';
COMMENT ON COLUMN rules.time_window_minutes IS 'For VELOCITY and RAPID_FIRE rules: time window in minutes';
COMMENT ON COLUMN rules.merchant_category IS 'For MERCHANT_RISK and CNP_HIGH_RISK rules: merchant category';
COMMENT ON COLUMN rules.country_code IS 'For GEOGRAPHIC_ANOMALY and CROSS_BORDER_HIGH_RISK rules: country code (ISO 3166-1 alpha-3)';
COMMENT ON COLUMN rules.min_amount IS 'For AMOUNT_RANGE rules: minimum amount in suspicious range';
COMMENT ON COLUMN rules.max_amount IS 'For AMOUNT_RANGE rules: maximum amount in suspicious range';
COMMENT ON COLUMN rules.start_hour IS 'For TIME_OF_DAY_ANOMALY rules: start hour (0-23)';
COMMENT ON COLUMN rules.end_hour IS 'For TIME_OF_DAY_ANOMALY rules: end hour (0-23)';
COMMENT ON COLUMN rules.minimum_amount IS 'For ROUND_AMOUNT rules: minimum amount threshold';
COMMENT ON COLUMN rules.round_to_nearest IS 'For ROUND_AMOUNT rules: rounding increment (10, 50, 100, 500, 1000)';
COMMENT ON COLUMN rules.customer_home_country IS 'For CROSS_BORDER_HIGH_RISK and CURRENCY_MISMATCH rules: customer home country (ISO 3166-1 alpha-3)';
COMMENT ON COLUMN rules.customer_home_currency IS 'For CURRENCY_MISMATCH rules: customer home currency (ISO 4217)';
