package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Evaluates VELOCITY rules.
 *
 * Triggers when customer makes multiple transactions within a time window.
 *
 * Configuration:
 * - threshold_count: Number of transactions in window
 * - time_window_minutes: Time window duration
 *
 * Example: 5 transactions within 10 minutes
 *
 * Risk Score Calculation:
 * - Base score: 60
 * - Additional points for exceeding threshold
 * - Formula: 60 + min(40, (excess_count) * 10)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VelocityRuleEvaluator implements RuleEvaluationStrategy {

    private final TriggeredTransactionRepository triggeredTransactionRepository;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.VELOCITY;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getThresholdCount() == null || rule.getTimeWindowMinutes() == null) {
            log.warn("Velocity rule {} missing threshold_count or time_window_minutes", rule.getId());
            return Optional.empty();
        }

        LocalDateTime windowStart = transaction.transactionTimestamp()
            .minusMinutes(rule.getTimeWindowMinutes());

        // Count recent transactions for this customer
        long recentCount = triggeredTransactionRepository
            .countByCustomerIdAndTransactionTimestampAfter(
                transaction.customerId(),
                windowStart
            );

        // Add 1 for current transaction
        long totalCount = recentCount + 1;

        if (totalCount > rule.getThresholdCount()) {
            String reason = String.format(
                "Customer made %d transactions within %d minutes (threshold: %d)",
                totalCount,
                rule.getTimeWindowMinutes(),
                rule.getThresholdCount()
            );

            int riskScore = calculateRiskScore(totalCount, rule.getThresholdCount());

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(long actualCount, int threshold) {
        long excess = actualCount - threshold;
        int additionalScore = (int) Math.min(40, excess * 10);
        return Math.min(100, 60 + additionalScore);
    }
}
