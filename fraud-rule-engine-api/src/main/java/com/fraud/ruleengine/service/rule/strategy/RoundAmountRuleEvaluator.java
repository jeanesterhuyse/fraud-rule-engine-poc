package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Evaluates ROUND_AMOUNT rules.
 *
 * Triggers when large round-number amounts are detected (common in card testing).
 * Fraudsters often test stolen cards with round amounts like R1000, R5000, R10000.
 *
 * Configuration:
 * - minimum_amount: Only check amounts above this threshold
 * - round_to_nearest: Check if amount is a multiple of this value (10, 50, 100, 500, 1000)
 *
 * Risk Score Calculation:
 * - 55 for amounts 1000-5000
 * - 65 for amounts 5000+
 */
@Component
@Slf4j
public class RoundAmountRuleEvaluator implements RuleEvaluationStrategy {

    private static final BigDecimal HIGHER_RISK_THRESHOLD = new BigDecimal("5000");

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.ROUND_AMOUNT;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getMinimumAmount() == null || rule.getRoundToNearest() == null) {
            log.warn("Round amount rule {} has no minimum_amount or round_to_nearest configured", rule.getId());
            return Optional.empty();
        }

        BigDecimal amount = transaction.amount();

        // Check if amount is above minimum threshold
        if (amount.compareTo(rule.getMinimumAmount()) < 0) {
            return Optional.empty();
        }

        // Check if amount is a multiple of round_to_nearest
        BigDecimal roundTo = new BigDecimal(rule.getRoundToNearest());
        BigDecimal remainder = amount.remainder(roundTo);

        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            String reason = String.format(
                "Transaction amount (%s %s) is a round number (multiple of %s) - potential card testing",
                amount,
                transaction.currency(),
                rule.getRoundToNearest()
            );

            int riskScore = calculateRiskScore(amount);

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(BigDecimal amount) {
        // Higher risk score for larger round amounts
        if (amount.compareTo(HIGHER_RISK_THRESHOLD) >= 0) {
            return 65;
        } else {
            return 55;
        }
    }
}
