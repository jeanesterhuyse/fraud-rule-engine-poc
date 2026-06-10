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
 * Evaluates RAPID_FIRE rules.
 *
 * Triggers when transactions occur in very short succession (e.g., within seconds).
 * May indicate automated attacks, card testing, or account takeover.
 *
 * Configuration:
 * - threshold_count: Number of transactions
 * - time_window_minutes: Very short time window (typically 1-2 minutes)
 *
 * Example: 2+ transactions within 1 minute
 *
 * Risk Score: 75 (rapid-fire is high risk for automation/bots)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RapidFireRuleEvaluator implements RuleEvaluationStrategy {

    private final TriggeredTransactionRepository triggeredTransactionRepository;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.RAPID_FIRE;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getThresholdCount() == null || rule.getTimeWindowMinutes() == null) {
            log.warn("Rapid fire rule {} missing threshold_count or time_window_minutes", rule.getId());
            return Optional.empty();
        }

        LocalDateTime windowStart = transaction.transactionTimestamp()
            .minusMinutes(rule.getTimeWindowMinutes());

        // Count recent transactions for this account (not just customer)
        long recentCount = triggeredTransactionRepository
            .countByAccountIdAndTransactionTimestampAfter(
                transaction.accountId(),
                windowStart
            );

        // Add 1 for current transaction
        long totalCount = recentCount + 1;

        if (totalCount >= rule.getThresholdCount()) {
            String reason = String.format(
                "Rapid-fire detected: %d transactions within %d minute(s). Possible automated attack or card testing.",
                totalCount,
                rule.getTimeWindowMinutes()
            );

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            // Rapid-fire is high risk
            return Optional.of(new RuleMatch(rule, reason, 75));
        }

        return Optional.empty();
    }
}
