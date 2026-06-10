package com.fraud.ruleengine.domain.model;

import com.fraud.ruleengine.domain.entity.Rule;
import jakarta.validation.constraints.*;

/**
 * Record representing the result of a rule evaluation.
 *
 * When a transaction matches a fraud rule, the RuleEvaluationStrategy
 * returns a RuleMatch containing:
 * - The rule that was triggered
 * - A human-readable explanation of why it triggered
 * - A calculated risk score (0-100)
 *
 * Design: Java record for immutability.
 */
public record RuleMatch(

    @NotNull(message = "Rule is required")
    Rule rule,

    @NotBlank(message = "Match reason is required")
    @Size(max = 2000, message = "Match reason must not exceed 2000 characters")
    String matchReason,

    @Min(value = 0, message = "Risk score must be at least 0")
    @Max(value = 100, message = "Risk score must not exceed 100")
    Integer riskScore

) {

    /**
     * Creates a RuleMatch with a default risk score of 50.
     */
    public static RuleMatch of(Rule rule, String matchReason) {
        return new RuleMatch(rule, matchReason, 50);
    }

    /**
     * Creates a RuleMatch for high-risk scenarios (score 80).
     */
    public static RuleMatch highRisk(Rule rule, String matchReason) {
        return new RuleMatch(rule, matchReason, 80);
    }

    /**
     * Creates a RuleMatch for medium-risk scenarios (score 50).
     */
    public static RuleMatch mediumRisk(Rule rule, String matchReason) {
        return new RuleMatch(rule, matchReason, 50);
    }

    /**
     * Creates a RuleMatch for low-risk scenarios (score 30).
     */
    public static RuleMatch lowRisk(Rule rule, String matchReason) {
        return new RuleMatch(rule, matchReason, 30);
    }

    /**
     * Returns a formatted string representation for logging.
     */
    public String toLogString() {
        return String.format(
            "RuleMatch[rule=%s, type=%s, riskScore=%d, reason=%s]",
            rule.getName(),
            rule.getRuleType(),
            riskScore,
            matchReason.length() > 100 ? matchReason.substring(0, 100) + "..." : matchReason
        );
    }
}
