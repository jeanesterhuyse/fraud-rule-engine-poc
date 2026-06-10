-- V3: Insert seed rules for demonstration
-- These rules demonstrate different fraud detection patterns

-- Rule 1: Large Transaction Alert
-- Triggers on any single transaction over 50,000
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    threshold_amount,
    created_by,
    updated_by
) VALUES (
    'Large Transaction Alert',
    'Triggers on any single transaction over 50,000 in any currency. High-value transactions require additional scrutiny.',
    'AMOUNT_THRESHOLD',
    true,
    200,  -- High priority
    50000.00,
    'system',
    'system'
);

-- Rule 2: Rapid Transaction Velocity
-- Triggers when customer makes more than 5 transactions within 10 minutes
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    threshold_count,
    time_window_minutes,
    created_by,
    updated_by
) VALUES (
    'Rapid Transaction Velocity',
    'Triggers when a customer makes more than 5 transactions within a 10-minute window. May indicate card testing or account takeover.',
    'VELOCITY',
    true,
    150,
    5,
    10,
    'system',
    'system'
);

-- Rule 3: High-Risk Country Transaction
-- Triggers on transactions from designated high-risk countries
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    country_code,
    created_by,
    updated_by
) VALUES (
    'High-Risk Country: Russia',
    'Triggers on transactions originating from Russia (RUS). Geographic anomaly detection.',
    'GEOGRAPHIC_ANOMALY',
    true,
    180,
    'RUS',
    'system',
    'system'
);

INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    country_code,
    created_by,
    updated_by
) VALUES (
    'High-Risk Country: North Korea',
    'Triggers on transactions originating from North Korea (PRK). Geographic anomaly detection for sanctioned countries.',
    'GEOGRAPHIC_ANOMALY',
    true,
    180,
    'PRK',
    'system',
    'system'
);

-- Rule 4: Gambling Merchant Alert
-- Triggers on transactions at gambling establishments
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    merchant_category,
    created_by,
    updated_by
) VALUES (
    'Gambling Merchant Alert',
    'Triggers on transactions at gambling establishments. High-risk merchant category requiring additional monitoring.',
    'MERCHANT_RISK',
    true,
    120,
    'GAMBLING',
    'system',
    'system'
);

-- Rule 5: Structuring Detection (Amount Range)
-- Triggers on transactions just under reporting thresholds (potential structuring)
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    min_amount,
    max_amount,
    created_by,
    updated_by
) VALUES (
    'Structuring Detection: Just Under 10K',
    'Triggers on transactions between 9,000 and 9,999. May indicate structuring to avoid reporting requirements.',
    'AMOUNT_RANGE',
    true,
    170,
    9000.00,
    9999.99,
    'system',
    'system'
);

-- Rule 6: Rapid-Fire Transactions
-- Triggers on transactions occurring within 30 seconds of each other
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    threshold_count,
    time_window_minutes,
    created_by,
    updated_by
) VALUES (
    'Rapid-Fire Transactions',
    'Triggers when 2 or more transactions occur within 1 minute from the same account. May indicate automated attack.',
    'RAPID_FIRE',
    true,
    160,
    2,
    1,
    'system',
    'system'
);

-- Rule 7: Cryptocurrency Exchange (High-Risk Merchant)
-- Triggers on transactions at cryptocurrency exchanges
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    merchant_category,
    created_by,
    updated_by
) VALUES (
    'Cryptocurrency Exchange Transaction',
    'Triggers on transactions at cryptocurrency exchanges. Emerging risk category requiring monitoring.',
    'MERCHANT_RISK',
    true,
    110,
    'CRYPTO_EXCHANGE',
    'system',
    'system'
);

-- Rule 8: Disabled Rule Example
-- Shows that disabled rules are not evaluated
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    threshold_amount,
    created_by,
    updated_by
) VALUES (
    'Very Large Transaction (Disabled)',
    'Triggers on transactions over 100,000. Currently disabled for testing purposes.',
    'AMOUNT_THRESHOLD',
    false,  -- Disabled
    190,
    100000.00,
    'system',
    'system'
);

-- Comments
-- These seed rules demonstrate:
-- 1. Different rule types (VELOCITY, AMOUNT_THRESHOLD, GEOGRAPHIC_ANOMALY, MERCHANT_RISK, AMOUNT_RANGE, RAPID_FIRE)
-- 2. Priority-based evaluation (rules evaluated in priority DESC order)
-- 3. Enabled vs disabled rules
-- 4. Real-world fraud detection patterns
-- 5. How nullable columns work (only relevant parameters are set for each rule type)
