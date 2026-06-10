package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Evaluates AMOUNT_THRESHOLD rules.
 *
 * Triggers when transaction amount exceeds a configured threshold.
 *
 * Configuration:
 * - threshold_amount: Minimum amount to trigger
 *
 * Risk Score Calculation:
 * - Base score: 50
 * - Additional points based on how much threshold was exceeded
 * - Formula: 50 + min(50, (excess / threshold) * 50)
 */
@Component
@Slf4j
public class AmountThresholdRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.AMOUNT_THRESHOLD;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getThresholdAmount() == null) {
            log.warn("Amount threshold rule {} has no threshold_amount configured", rule.getId());
            return Optional.empty();
        }

        if (transaction.amount().compareTo(rule.getThresholdAmount()) > 0) {
            String reason = String.format(
                "Transaction amount (%s %s) exceeds threshold of %s",
                transaction.amount(),
                transaction.currency(),
                rule.getThresholdAmount()
            );

            int riskScore = calculateRiskScore(transaction.amount(), rule.getThresholdAmount());

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            return Optional.of(new RuleMatch(rule, reason, riskScore));
        }

        return Optional.empty();
    }

    private int calculateRiskScore(BigDecimal amount, BigDecimal threshold) {
        BigDecimal excess = amount.subtract(threshold);
        BigDecimal ratio = excess.divide(threshold, 2, RoundingMode.HALF_UP);

        // Base score of 50, plus up to 50 more based on excess ratio
        int additionalScore = Math.min(50, ratio.multiply(new BigDecimal(50)).intValue());
        return Math.min(100, 50 + additionalScore);
    }
}
