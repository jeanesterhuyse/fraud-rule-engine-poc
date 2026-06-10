package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.enums.TransactionType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Evaluates LARGE_WITHDRAWAL rules.
 *
 * Triggers on large ATM or cash withdrawals exceeding a threshold.
 * Large cash withdrawals require enhanced monitoring as they're often
 * the final stage of account compromise.
 *
 * Configuration:
 * - threshold_amount: Maximum withdrawal amount before triggering
 *
 * Risk Score Calculation:
 * - Base score: 50
 * - Additional points based on how much threshold was exceeded
 * - Formula: 50 + min(30, (excess / threshold) * 30)
 * - Maximum score: 80
 */
@Component
@Slf4j
public class LargeWithdrawalRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.LARGE_WITHDRAWAL;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getThresholdAmount() == null) {
            log.warn("Large withdrawal rule {} has no threshold_amount configured", rule.getId());
            return Optional.empty();
        }

        // Only trigger on withdrawal transactions
        if (transaction.transactionType() != TransactionType.WITHDRAWAL) {
            return Optional.empty();
        }

        BigDecimal amount = transaction.amount();

        if (amount.compareTo(rule.getThresholdAmount()) > 0) {
            String reason = String.format(
                "Large cash withdrawal (%s %s) exceeds threshold of %s",
                amount,
                transaction.currency(),
                rule.getThresholdAmount()
            );

            int riskScore = calculateRiskScore(amount, rule.getThresholdAmount());

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(BigDecimal amount, BigDecimal threshold) {
        BigDecimal excess = amount.subtract(threshold);
        BigDecimal ratio = excess.divide(threshold, 2, RoundingMode.HALF_UP);

        // Base score of 50, plus up to 30 more based on excess ratio
        int additionalScore = Math.min(30, ratio.multiply(new BigDecimal(30)).intValue());
        return Math.min(80, 50 + additionalScore);
    }
}
