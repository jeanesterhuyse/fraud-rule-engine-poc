-- V4: Insert seed rules for all 12 rule types
-- These rules demonstrate different fraud detection patterns

-- Rule 1: CUSTOMER_BLOCKLIST (highest priority - instant block)
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    created_by,
    updated_by
) VALUES (
    'Blocked Customer Check',
    'Instant block for customers on the blocklist. Highest priority rule - always evaluated first.',
    'CUSTOMER_BLOCKLIST',
    true,
    1000,  -- Highest priority
    'system',
    'system'
);

-- Rule 2: MERCHANT_BLOCKLIST (second-highest priority)
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    created_by,
    updated_by
) VALUES (
    'Blocked Merchant Check',
    'Instant block for transactions at blacklisted merchants. Second-highest priority.',
    'MERCHANT_BLOCKLIST',
    true,
    990,
    'system',
    'system'
);

-- Rule 3: AMOUNT_THRESHOLD
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
    200,
    50000.00,
    'system',
    'system'
);

-- Rule 4: GEOGRAPHIC_ANOMALY (Russia)
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

-- Rule 5: GEOGRAPHIC_ANOMALY (North Korea)
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

-- Rule 6: MERCHANT_RISK (Gambling)
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

-- Rule 7: MERCHANT_RISK (Crypto)
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

-- Rule 8: AMOUNT_RANGE (Structuring Detection)
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

-- Rule 9: TIME_OF_DAY_ANOMALY
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    start_hour,
    end_hour,
    created_by,
    updated_by
) VALUES (
    'Late Night Transactions',
    'Triggers on transactions between 2 AM and 5 AM local time. Unusual hours may indicate unauthorized access.',
    'TIME_OF_DAY_ANOMALY',
    true,
    140,
    2,
    5,
    'system',
    'system'
);

-- Rule 10: ROUND_AMOUNT
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    minimum_amount,
    round_to_nearest,
    created_by,
    updated_by
) VALUES (
    'Card Testing Detection',
    'Detects large round-amount transactions (e.g., R1000, R5000) which are common in card testing attacks.',
    'ROUND_AMOUNT',
    true,
    130,
    1000.00,
    100,
    'system',
    'system'
);

-- Rule 11: CNP_HIGH_RISK (Electronics)
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
    'CNP Electronics Risk',
    'Card-not-present transactions at electronics merchants. High fraud risk due to resale value.',
    'CNP_HIGH_RISK',
    true,
    125,
    'ELECTRONICS',
    'system',
    'system'
);

-- Rule 12: CNP_HIGH_RISK (Jewelry)
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
    'CNP Jewelry Risk',
    'Card-not-present transactions at jewelry merchants. Highest CNP risk category.',
    'CNP_HIGH_RISK',
    true,
    127,
    'JEWELRY',
    'system',
    'system'
);

-- Rule 13: CNP_HIGH_RISK (Travel)
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
    'CNP Travel Risk',
    'Card-not-present transactions at travel merchants. Often used for testing stolen cards.',
    'CNP_HIGH_RISK',
    true,
    123,
    'TRAVEL',
    'system',
    'system'
);

-- Rule 14: CURRENCY_MISMATCH
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    customer_home_country,
    customer_home_currency,
    created_by,
    updated_by
) VALUES (
    'Foreign Currency Transaction',
    'South African customers transacting in foreign currency outside South Africa. Potential unauthorized use.',
    'CURRENCY_MISMATCH',
    true,
    135,
    'ZAF',
    'ZAR',
    'system',
    'system'
);

-- Rule 15: CROSS_BORDER_HIGH_RISK
INSERT INTO rules (
    name,
    description,
    rule_type,
    enabled,
    priority,
    customer_home_country,
    country_code,
    created_by,
    updated_by
) VALUES (
    'Cross-Border to Russia',
    'South African customers transacting in Russia. High-risk cross-border transaction.',
    'CROSS_BORDER_HIGH_RISK',
    true,
    185,
    'ZAF',
    'RUS',
    'system',
    'system'
);

-- Rule 16: LARGE_WITHDRAWAL
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
    'Large ATM Withdrawal',
    'Triggers on ATM or cash withdrawals exceeding R20,000. Large cash withdrawals require enhanced monitoring.',
    'LARGE_WITHDRAWAL',
    true,
    160,
    20000.00,
    'system',
    'system'
);

-- Rule 17: Disabled Example (for testing)
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
-- 1. All 12 rule types: CUSTOMER_BLOCKLIST, MERCHANT_BLOCKLIST, AMOUNT_THRESHOLD, GEOGRAPHIC_ANOMALY,
--    MERCHANT_RISK, AMOUNT_RANGE, TIME_OF_DAY_ANOMALY, ROUND_AMOUNT, CNP_HIGH_RISK, CURRENCY_MISMATCH,
--    CROSS_BORDER_HIGH_RISK, LARGE_WITHDRAWAL
-- 2. Priority-based evaluation (rules evaluated in priority DESC order)
-- 3. Enabled vs disabled rules
-- 4. Real-world fraud detection patterns
-- 5. How nullable columns work (only relevant parameters are set for each rule type)
