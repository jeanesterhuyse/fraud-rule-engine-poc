package com.fraud.ruleengine.service.rule.strategy;

import com.fraud.ruleengine.domain.entity.Rule;
import com.fraud.ruleengine.domain.enums.RuleType;
import com.fraud.ruleengine.domain.model.RuleMatch;
import com.fraud.ruleengine.domain.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Evaluates AMOUNT_RANGE rules.
 *
 * Triggers when transaction amount falls within a suspicious range.
 * Commonly used to detect structuring (breaking large amounts into smaller
 * amounts to avoid reporting thresholds).
 *
 * Configuration:
 * - min_amount: Minimum amount in range
 * - max_amount: Maximum amount in range
 *
 * Example: 9,000 - 9,999 (just under $10,000 reporting threshold)
 *
 * Risk Score: 70 (structuring is a serious fraud indicator)
 */
@Component
@Slf4j
public class AmountRangeRuleEvaluator implements RuleEvaluationStrategy {

    @Override
    public boolean supports(RuleType ruleType) {
        return ruleType == RuleType.AMOUNT_RANGE;
    }

    @Override
    public Optional<RuleMatch> evaluate(Transaction transaction, Rule rule) {
        if (rule.getMinAmount() == null || rule.getMaxAmount() == null) {
            log.warn("Amount range rule {} missing min_amount or max_amount", rule.getId());
            return Optional.empty();
        }

        if (transaction.amount().compareTo(rule.getMinAmount()) >= 0 &&
            transaction.amount().compareTo(rule.getMaxAmount()) <= 0) {

            String reason = String.format(
                "Transaction amount (%s %s) falls within suspicious range (%s - %s). Possible structuring.",
                transaction.amount(),
                transaction.currency(),
                rule.getMinAmount(),
                rule.getMaxAmount()
            );

            log.debug("Rule {} triggered for transaction {}: {}",
                rule.getName(), transaction.transactionId(), reason);

            // Structuring is a serious indicator
            return Optional.of(new RuleMatch(rule, reason, 70));
        }

        return Optional.empty();
    }
}
