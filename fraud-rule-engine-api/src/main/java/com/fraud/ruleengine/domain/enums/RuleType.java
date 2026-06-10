package com.fraud.ruleengine.domain.enums;

/**
 * Enumeration of supported fraud rule types.
 *
 * Each rule type has a corresponding RuleEvaluationStrategy implementation
 * that defines the evaluation logic.
 *
 * NOTE: All rule types evaluate single transactions in isolation (no historical queries required).
 */
public enum RuleType {

    /**
     * Amount threshold rule: triggers when transaction amount exceeds threshold.
     *
     * Configuration: threshold_amount
     * Example: Transactions over $50,000
     */
    AMOUNT_THRESHOLD,

    /**
     * Geographic anomaly rule: triggers on transactions from high-risk countries.
     *
     * Configuration: country_code
     * Example: Transactions from Russia, North Korea, etc.
     */
    GEOGRAPHIC_ANOMALY,

    /**
     * Merchant risk rule: triggers on transactions at high-risk merchant categories.
     *
     * Configuration: merchant_category
     * Example: Gambling establishments, cryptocurrency exchanges
     */
    MERCHANT_RISK,

    /**
     * Amount range rule: triggers when transaction amount falls within suspicious range.
     *
     * Configuration: min_amount, max_amount
     * Example: $9,000 - $9,999 (potential structuring)
     */
    AMOUNT_RANGE,

    /**
     * Time-of-day anomaly rule: triggers on transactions during unusual hours.
     *
     * Configuration: start_hour, end_hour (0-23)
     * Example: Transactions between 2 AM and 5 AM
     */
    TIME_OF_DAY_ANOMALY,

    /**
     * Round amount rule: triggers on large round-number amounts (card testing pattern).
     *
     * Configuration: minimum_amount, round_to_nearest
     * Example: R1000, R5000, R10000 (testing stolen cards)
     */
    ROUND_AMOUNT,

    /**
     * Customer blocklist rule: triggers on transactions from blocked customers.
     *
     * Configuration: None (checks blocked_customers table)
     * Risk Score: 100 (critical - instant block)
     */
    CUSTOMER_BLOCKLIST,

    /**
     * Merchant blocklist rule: triggers on transactions at blocked merchants.
     *
     * Configuration: None (checks blocked_merchants table)
     * Risk Score: 95 (critical)
     */
    MERCHANT_BLOCKLIST,

    /**
     * Card-not-present high-risk rule: triggers on online transactions at high-risk merchants.
     *
     * Configuration: merchant_category
     * Example: Online electronics, jewelry, travel bookings
     */
    CNP_HIGH_RISK,

    /**
     * Currency mismatch rule: triggers when transaction currency doesn't match customer's home currency.
     *
     * Configuration: customer_home_country, customer_home_currency
     * Example: ZAF customer using USD outside South Africa
     */
    CURRENCY_MISMATCH,

    /**
     * Cross-border high-risk rule: triggers on cross-border transactions to high-risk countries.
     *
     * Configuration: customer_home_country, country_code
     * Example: ZAF customer transacting in Russia
     */
    CROSS_BORDER_HIGH_RISK,

    /**
     * Large withdrawal rule: triggers on large ATM or cash withdrawals.
     *
     * Configuration: threshold_amount
     * Example: Cash withdrawals over R20,000
     */
    LARGE_WITHDRAWAL
}
