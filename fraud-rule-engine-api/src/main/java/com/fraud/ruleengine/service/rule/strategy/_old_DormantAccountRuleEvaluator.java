package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.entity.TriggeredTransaction;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import com.fraud.ruleengine.repository.TriggeredTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Evaluates DORMANT_ACCOUNT rules.
 *
 * Triggers when a transaction occurs on an account after a long period
 * of inactivity. May indicate account takeover or compromise.
 *
 * Configuration:
 * - time_window_minutes: Inactivity period (e.g., 90 days = 129,600 minutes)
 *
 * Example: First transaction after 90 days of inactivity
 *
 * Risk Score Calculation:
 * - 30-60 days: 50
 * - 60-90 days: 65
 * - 90+ days: 80
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DormantAccountRuleEvaluator implements RuleEvaluationStrategy {

    private final TriggeredTransactionRepository triggeredTransactionRepository;

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.DORMANT_ACCOUNT;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getTimeWindowMinutes() == null) {
            log.warn("Dormant account rule {} missing time_window_minutes", rule.getId());
            return Optional.empty();
        }

        LocalDateTime inactivityThreshold = transaction.transactionTimestamp()
            .minusMinutes(rule.getTimeWindowMinutes());

        // Find most recent transaction before this one
        TriggeredTransaction mostRecent = triggeredTransactionRepository
            .findMostRecentBeforeTimestamp(
                transaction.customerId(),
                transaction.transactionTimestamp()
            );

        if (mostRecent == null) {
            // No previous transactions found - this might be a new account
            // or our data doesn't go back far enough
            log.debug("No previous transactions found for customer {}", transaction.customerId());
            return Optional.empty();
        }

        // Check if most recent transaction was before the inactivity threshold
        if (mostRecent.getTransactionTimestamp().isBefore(inactivityThreshold)) {
            long daysSinceLastTransaction = ChronoUnit.DAYS.between(
                mostRecent.getTransactionTimestamp(),
                transaction.transactionTimestamp()
            );

            String reason = String.format(
                "First transaction after %d days of inactivity (last transaction: %s). Possible account takeover.",
                daysSinceLastTransaction,
                mostRecent.getTransactionTimestamp()
            );

            int riskScore = calculateRiskScore(daysSinceLastTransaction);

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(long daysInactive) {
        if (daysInactive >= 90) {
            return 80;
        } else if (daysInactive >= 60) {
            return 65;
        } else if (daysInactive >= 30) {
            return 50;
        } else {
            return 40;
        }
    }
}
