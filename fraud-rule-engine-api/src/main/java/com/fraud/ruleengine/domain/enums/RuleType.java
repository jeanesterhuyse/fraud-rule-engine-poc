package com.fraud.ruleengine.domain.enums;

/**
 * Enumeration of supported fraud rule types.
 *
 * Each rule type has a corresponding RuleEvaluationStrategy implementation
 * that defines the evaluation logic.
 */
public enum RuleType {

    /**
     * Velocity-based rule: triggers when customer makes multiple transactions
     * within a specified time window.
     *
     * Configuration: threshold_count, time_window_minutes
     * Example: 5 transactions within 10 minutes
     */
    VELOCITY,

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
     * Rapid-fire rule: triggers when transactions occur in very short succession.
     *
     * Configuration: threshold_count, time_window_minutes
     * Example: 2+ transactions within 1 minute
     */
    RAPID_FIRE,

    /**
     * Dormant account rule: triggers on first transaction after long inactivity.
     *
     * Configuration: time_window_minutes (inactivity period)
     * Example: First transaction after 90 days of inactivity
     */
    DORMANT_ACCOUNT
}
